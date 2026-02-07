package com.vv.cloudfarming.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dao.entity.PayOrderDO;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailAdoptMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderDetailSkuMapper;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dto.common.ProductSummaryDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderPageReqDTO;
import com.vv.cloudfarming.order.dto.req.PayOrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.*;
import com.vv.cloudfarming.order.remote.ShopRemoteService;
import com.vv.cloudfarming.order.service.OrderService;
import com.vv.cloudfarming.order.service.PayService;
import com.vv.cloudfarming.order.strategy.AbstractOrderCreateTemplate;
import com.vv.cloudfarming.order.strategy.OrderTemplateFactory;
import com.vv.cloudfarming.order.utils.ProductUtil;
import com.vv.cloudfarming.order.utils.RedisIdWorker;
import com.vv.cloudfarming.product.dto.resp.ShopRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务实现类 (Refactored)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderDO> implements OrderService {

    private final OrderTemplateFactory templateFactory;
    private final PayService payService;
    private final ShopRemoteService shopRemoteService;
    private final ProductUtil productUtil;
    private final RedisIdWorker redisIdWorker;
    private final OrderDetailAdoptMapper orderDetailAdoptMapper;
    private final OrderDetailSkuMapper orderDetailSkuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateRespDTO createOrder(Long userId, OrderCreateReqDTO requestParam) {
        // 1. 根据订单类型获取对应的模板
        AbstractOrderCreateTemplate<?, ?> template = templateFactory.getTemplate(requestParam.getOrderType());

        // 2. 生成全局唯一的支付单号
        String payOrderNo = generatePayOrderNo(userId);
        
        // 3. 执行模板方法，生成业务订单列表
        List<OrderDO> orders = template.createOrder(userId, payOrderNo, requestParam);
        if (orders.isEmpty()) {
            throw new ServiceException("订单创建失败：未生成有效订单");
        }
        
        // 4. 计算总金额并生成支付单
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderDO order : orders) {
            if (order.getActualPayAmount() != null) {
                totalAmount = totalAmount.add(order.getActualPayAmount());
            }
        }

        PayOrderCreateReqDTO payOrderCreateReq = PayOrderCreateReqDTO.builder()
                .buyerId(userId)
                .payOrderNo(payOrderNo)
                .totalAmount(totalAmount)
                .build();
        PayOrderDO payOrder = payService.createPayOrder(payOrderCreateReq);

        // 5. 构建响应
        OrderCreateRespDTO response = OrderCreateRespDTO.builder()
                .payAmount(totalAmount)
                .payOrderNo(payOrderNo)
                .expireTime(payOrder.getExpireTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();
        return response;
    }

    @Override
    public IPage<OrderPageWithProductInfoRespDTO> listOrderWithProductInfo(OrderPageReqDTO requestParam) {
        Long id = requestParam.getId();
        Integer orderStatus = requestParam.getOrderStatus();
        Long userId = requestParam.getUserId();

        LambdaQueryWrapper<OrderDO> wrapper = Wrappers.lambdaQuery(OrderDO.class)
                .eq(ObjectUtil.isNotNull(id), OrderDO::getId, id)
                .eq(ObjectUtil.isNotNull(orderStatus), OrderDO::getOrderStatus, orderStatus)
                .eq(ObjectUtil.isNotNull(userId), OrderDO::getUserId, userId);
        IPage<OrderDO> orderPage = baseMapper.selectPage(requestParam, wrapper);
        // 转换
        return orderPage.convert(each -> {
            ShopRespDTO shop = shopRemoteService.getShopById(each.getShopId()).getData();
            ArrayList<ProductSummaryDTO> summaryList = new ArrayList<>();
            productUtil.buildProductSummary(each.getId(),each.getOrderType(),summaryList);
            return OrderPageWithProductInfoRespDTO.builder()
                    .id(each.getId())
                    .shopName(shop.getShopName())
                    .items(summaryList)
                    .totalPrice(each.getTotalAmount())
                    .build();
        });
    }

    @Override
    public IPage<OrderPageRespDTO> listOrders(OrderPageReqDTO requestParam) {
        Long id = requestParam.getId();
        String payOrderNo = requestParam.getOrderNo();
        Integer orderStatus = requestParam.getOrderStatus();
        Long userId = requestParam.getUserId();
        Long shopId = requestParam.getShopId();

        LambdaQueryWrapper<OrderDO> wrapper = Wrappers.lambdaQuery(OrderDO.class)
                .eq(ObjectUtil.isNotNull(id), OrderDO::getId, id)
                .eq(ObjectUtil.isNotNull(payOrderNo), OrderDO::getPayOrderNo, payOrderNo)
                .eq(ObjectUtil.isNotNull(orderStatus), OrderDO::getOrderStatus, orderStatus)
                .eq(ObjectUtil.isNotNull(userId), OrderDO::getUserId, userId)
                .eq(ObjectUtil.isNotNull(shopId), OrderDO::getShopId, shopId);
        IPage<OrderDO> orderPage = baseMapper.selectPage(requestParam, wrapper);
        return orderPage.convert(each -> {
            return BeanUtil.toBean(each, OrderPageRespDTO.class);
        });
    }

    @Override
    public List<AdoptOrderDetailRespDTO> getAdoptOrderDetail(String orderNo) {
        LambdaQueryWrapper<OrderDetailAdoptDO> wrapper = Wrappers.lambdaQuery(OrderDetailAdoptDO.class)
                .eq(OrderDetailAdoptDO::getOrderNo, orderNo);
        List<OrderDetailAdoptDO> orderDetails = orderDetailAdoptMapper.selectList(wrapper);
        return orderDetails.stream().map(each -> {
            return BeanUtil.toBean(each, AdoptOrderDetailRespDTO.class);
        }).toList();
    }

    @Override
    public List<SkuOrderDetailRespDTO> getSkuOrderDetail(String orderNo) {
        LambdaQueryWrapper<OrderDetailSkuDO> wrapper = Wrappers.lambdaQuery(OrderDetailSkuDO.class)
                .eq(OrderDetailSkuDO::getOrderNo, orderNo);
        List<OrderDetailSkuDO> orderDetailSkus = orderDetailSkuMapper.selectList(wrapper);
        return orderDetailSkus.stream().map(each -> {
            return BeanUtil.toBean(each, SkuOrderDetailRespDTO.class);
        }).toList();
    }

    private String generatePayOrderNo(Long userId) {
        return redisIdWorker.generateId("paySN").toString();
    }
}
