package com.vv.cloudfarming.order.strategy;

import cn.hutool.json.JSONObject;
import com.vv.cloudfarming.common.enums.ReviewStatusEnum;
import com.vv.cloudfarming.common.enums.ShelfStatusEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.constant.OrderTypeConstant;
import com.vv.cloudfarming.order.dto.req.AdoptOrderCreateReqDTO;
import com.vv.cloudfarming.order.dao.entity.AdoptOrderDO;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.mapper.AdoptOrderMapper;
import com.vv.cloudfarming.order.enums.OrderStatusEnum;
import com.vv.cloudfarming.order.enums.PayStatusEnum;
import com.vv.cloudfarming.shop.dao.entity.AdoptInstanceDO;
import com.vv.cloudfarming.shop.dao.entity.AdoptItemDO;
import com.vv.cloudfarming.shop.dao.mapper.AdoptItemMapper;
import com.vv.cloudfarming.shop.enums.AdoptOrderStatusEnum;
import com.vv.cloudfarming.shop.enums.LivestockStatusEnum;
import com.vv.cloudfarming.shop.service.AdoptInstanceService;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import com.vv.cloudfarming.user.service.ReceiveAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * 创建认养项目订单
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdoptOrderCreateStrategy extends AbstractOrderCreateTemplate<AdoptOrderCreateReqDTO> implements OrderCreateStrategy {

    private final AdoptItemMapper adoptItemMapper;
    private final AdoptOrderMapper adoptOrderMapper;
    private final AdoptInstanceService adoptInstanceService;
    private final ReceiveAddressService receiveAddressService;

    public Integer getOrderType() {
        return OrderTypeConstant.ADOPT;
    }

    @Override
    protected AdoptOrderCreateReqDTO parseBizData(JSONObject bizData) {
        return bizData.toBean(AdoptOrderCreateReqDTO.class);
    }

    @Override
    protected void validate(Long userId, AdoptOrderCreateReqDTO data) {
        if (data == null) {
            throw new ClientException("请求参数不能为空");
        }
        if (data.getAdoptItemId() == null) {
            throw new ClientException("认养项目不能为空");
        }
        if (data.getQuantity() == null || data.getQuantity() <= 0) {
            throw new ClientException("下单数量必须大于0");
        }
        if (data.getReceiveId() == null) {
            throw new ClientException("收货信息不能为空");
        }
        AdoptItemDO adoptItem = getAdoptItem(data.getAdoptItemId());
        if (!ReviewStatusEnum.APPROVED.getStatus().equals(adoptItem.getReviewStatus())) {
            throw new ClientException("认养项目未审核通过，无法领养");
        }
        if (!ShelfStatusEnum.ONLINE.getCode().equals(adoptItem.getStatus())) {
            throw new ClientException("认养项目未上架，无法领养");
        }
        if (adoptItem.getFarmerId() != null && adoptItem.getFarmerId().equals(userId)) {
            throw new ClientException("不能领养自己发布的认养项目");
        }
    }

    @Override
    protected OrderDO buildMainOrder(Long userId, AdoptOrderCreateReqDTO data, Integer orderType) {
        AdoptItemDO adoptItem = getAdoptItem(data.getAdoptItemId());
        BigDecimal totalAmount = adoptItem.getPrice().multiply(new BigDecimal(data.getQuantity()));
        ReceiveAddressRespDTO receiveAddress = receiveAddressService.getReceiveAddressById(data.getReceiveId());

        OrderDO order = OrderDO.builder()
                .orderNo(generateOrderNo(userId))
                .userId(userId)
                .totalAmount(totalAmount)
                .payType(0)
                .payStatus(PayStatusEnum.UNPAID.getCode())
                .receiveName(receiveAddress.getReceiverName())
                .receivePhone(receiveAddress.getReceiverPhone())
                .receiveProvince(receiveAddress.getProvinceName())
                .receiveCity(receiveAddress.getCityName())
                .receiveDistrict(receiveAddress.getDistrictName())
                .receiveDetail(receiveAddress.getDetailAddress())
                .orderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode())
                .orderType(orderType).build();
        return order;
    }

    @Override
    protected Long createSubOrder(Long userId, OrderDO mainOrder, AdoptOrderCreateReqDTO data) {
        AdoptItemDO adoptItem = getAdoptItem(data.getAdoptItemId());
        // 先准备好时间数据
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_MONTH, adoptItem.getAdoptDays());
        Date endDate = calendar.getTime();

        // 构建订单对象
        AdoptOrderDO adoptOrder = AdoptOrderDO.builder()
                .buyerId(userId)
                .adoptItemId(data.getAdoptItemId())
                .price(adoptItem.getPrice())
                .startDate(now)
                .endDate(endDate)
                .orderStatus(AdoptOrderStatusEnum.IN_PROGRESS.getCode())
                .receiveId(data.getReceiveId())
                .payStatus(PayStatusEnum.UNPAID.getCode())
                .build();
        // 持久化
        int inserted = adoptOrderMapper.insert(adoptOrder);
        if (inserted != 1) {
            throw new ServiceException("创建认养订单失败");
        }
        // 创建养殖实例
        ArrayList<AdoptInstanceDO> instances = new ArrayList<>(data.getQuantity());
        for (int i = 0; i < data.getQuantity(); i++) {
            AdoptInstanceDO adoptInstance = AdoptInstanceDO.builder()
                    .itemId(adoptItem.getId())
                    .farmerId(adoptItem.getFarmerId())
                    .ownerOrderId(adoptOrder.getId())
                    .ownerUserId(userId)
                    .status(LivestockStatusEnum.AVAILABLE.getCode())
                    .build();
            instances.add(adoptInstance);
        }
        boolean saved = adoptInstanceService.saveBatch(instances);
        if (!saved) {
            log.error("批量创建牲畜记录失败，认养项目id：{}", adoptItem.getId());
        }
        return adoptOrder.getId();
    }

    private AdoptItemDO getAdoptItem(Long adoptItemId) {
        AdoptItemDO adoptItem = adoptItemMapper.selectById(adoptItemId);
        if (adoptItem == null) {
            throw new ClientException("认养项目不存在");
        }
        return adoptItem;
    }
}
