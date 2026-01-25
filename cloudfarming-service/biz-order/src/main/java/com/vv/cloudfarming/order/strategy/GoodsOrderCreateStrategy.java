package com.vv.cloudfarming.order.strategy;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dto.req.GoodsOrderCreateReqDTO;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderItemDO;
import com.vv.cloudfarming.order.dao.mapper.OrderItemMapper;
import com.vv.cloudfarming.order.dto.OrderItemDTO;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.shop.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.shop.service.SkuService;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import com.vv.cloudfarming.user.service.ReceiveAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GoodsOrderCreateStrategy extends AbstractOrderCreateTemplate<GoodsOrderCreateReqDTO> implements OrderCreateStrategy {

    private final SkuService skuService;
    private final ReceiveAddressService receiveAddressService;
    private final OrderItemMapper orderItemMapper;

    public Integer getOrderType() {
        return OrderTypeConstant.GOODS;
    }

    @Override
    protected GoodsOrderCreateReqDTO parseBizData(JSONObject bizData) {
        return bizData.toBean(GoodsOrderCreateReqDTO.class);
    }

    @Override
    protected void validate(Long userId, GoodsOrderCreateReqDTO data) {
        if (data == null || data.getItems() == null || data.getItems().isEmpty()) {
            throw new ClientException("商品列表不能为空");
        }
        if (data.getReceiveId() == null) {
            throw new ClientException("收货信息不能为空");
        }
        Map<Long, SkuRespDTO> skuMap = getSkuMap(data.getItems());
        for (OrderItemDTO item : data.getItems()) {
            SkuRespDTO sku = skuMap.get(item.getSkuId());
            if (sku == null) {
                throw new ClientException("包含不存在的商品");
            }
            boolean locked = skuService.lockStock(item.getSkuId(), item.getQuantity());
            if (!locked) {
                throw new ClientException("商品库存不足或未上架: " + sku.getSpuTitle());
            }
        }
    }

    @Override
    protected OrderDO buildMainOrder(Long userId, GoodsOrderCreateReqDTO data, Integer orderType) {
        Map<Long, SkuRespDTO> skuMap = getSkuMap(data.getItems());
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO item : data.getItems()) {
            SkuRespDTO sku = skuMap.get(item.getSkuId());
            BigDecimal itemAmount = sku.getPrice().multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(itemAmount);
        }
        ReceiveAddressRespDTO receiveAddress = receiveAddressService.getReceiveAddressById(data.getReceiveId());
        if (receiveAddress == null){
            throw new ClientException("收货地址不存在");
        }
        OrderDO orderDO = OrderDO.builder()
                // 基础信息
                .orderNo(generateOrderNo(userId))
                .userId(userId)
                .orderType(orderType)
                // 金额与支付
                .totalAmount(totalAmount)
                .payType(0)
                .payStatus(PayStatusEnum.UNPAID.getCode())
                .orderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode())
                // 收货地址信息
                .receiveName(receiveAddress.getReceiverName())
                .receivePhone(receiveAddress.getReceiverPhone())
                .receiveProvince(receiveAddress.getProvinceName())
                .receiveCity(receiveAddress.getCityName())
                .receiveDistrict(receiveAddress.getDistrictName())
                .receiveDetail(receiveAddress.getDetailAddress())
                .build();
        return orderDO;
    }

    @Override
    protected Long createSubOrder(Long userId, OrderDO mainOrder, GoodsOrderCreateReqDTO data) {
        // 1. 数据准备：获取商品详情并按店铺分组
        Map<Long, SkuRespDTO> skuMap = getSkuMap(data.getItems());

        // 用于存放每个店铺对应的购物项列表 (Key: ShopId, Value: Items)
        Map<Long, List<OrderItemDTO>> shopItemsMap = new HashMap<>();

        for (OrderItemDTO item : data.getItems()) {
            SkuRespDTO sku = skuMap.get(item.getSkuId());
            Long shopId = sku.getShopId();
            // 数据校验：确保商品关联了店铺
            if (shopId == null) {
                throw new ServiceException("商品数据异常：缺少店铺信息");
            }
            shopItemsMap.computeIfAbsent(shopId, k -> new ArrayList<>()).add(item);
        }

        // 2. 遍历每个店铺，生成对应的子订单 (OrderItemDO)
        Long lastSubOrderId = null;

        for (Map.Entry<Long, List<OrderItemDTO>> entry : shopItemsMap.entrySet()) {
            Long shopId = entry.getKey();
            List<OrderItemDTO> currentShopItems = entry.getValue();

            // 2.1 计算当前子订单的 总金额、总数量 以及 生成商品快照
            BigDecimal subTotalAmount = BigDecimal.ZERO;
            int subTotalQuantity = 0;
            List<Map<String, Object>> snapshots = new ArrayList<>();

            for (OrderItemDTO item : currentShopItems) {
                SkuRespDTO sku = skuMap.get(item.getSkuId());
                // 计算单项总价 (单价 * 数量)
                BigDecimal itemAmount = sku.getPrice().multiply(new BigDecimal(item.getQuantity()));
                // 累加子订单总额和总数
                subTotalAmount = subTotalAmount.add(itemAmount);
                subTotalQuantity += item.getQuantity();
                // 生成商品快照 (SKU信息 + 购买数量)
                Map<String, Object> map = BeanUtil.beanToMap(sku);
                map.put("buyQuantity", item.getQuantity());
                snapshots.add(map);
            }

            // 2.2 构建子订单对象 (使用 Builder 模式)
            OrderItemDO orderItemDO = OrderItemDO.builder()
                    // 关联信息
                    .mainOrderId(mainOrder.getId())
                    .mainOrderNo(mainOrder.getOrderNo())
                    .userId(userId)
                    .shopId(shopId)
                    .itemOrderNo(generateOrderNo(shopId)) // 生成子订单号
                    // 商品数据信息
                    .productJson(JSONUtil.toJsonStr(snapshots)) // 存入快照JSON
                    .productTotalQuantity(subTotalQuantity)
                    // 金额信息
                    .productTotalAmount(subTotalAmount)
                    .actualPayAmount(subTotalAmount) // 初始实付等于总额(未算优惠)
                    // 状态信息
                    .orderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode())
                    .build();

            // 2.3 入库保存
            int insertedRows = orderItemMapper.insert(orderItemDO);
            if (insertedRows != 1) {
                throw new ServiceException("子订单创建失败，ShopId: " + shopId);
            }
            // 更新最后生成的子订单ID
            lastSubOrderId = orderItemDO.getId();
        }

        // 返回最后一个子订单ID (注：如果业务需要返回所有ID，建议修改此处返回List)
        return lastSubOrderId;
    }


    private Map<Long, SkuRespDTO> getSkuMap(List<OrderItemDTO> items) {
        List<Long> skuIds = items.stream().map(OrderItemDTO::getSkuId).collect(Collectors.toList());
        List<SkuRespDTO> skuDetails = skuService.listSkuDetailsByIds(skuIds);
        if (skuDetails.size() != skuIds.size()) {
            throw new ClientException("包含不存在的商品");
        }
        return skuDetails.stream().collect(Collectors.toMap(SkuRespDTO::getId, sku -> sku));
    }
}
