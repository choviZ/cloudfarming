package com.vv.cloudfarming.order.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dao.entity.PayOrderDO;
import com.vv.cloudfarming.order.dao.mapper.OrderMapper;
import com.vv.cloudfarming.order.dto.common.ProductSummaryDTO;
import com.vv.cloudfarming.order.dto.req.OrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.req.OrderPageReqDTO;
import com.vv.cloudfarming.order.dto.req.PayOrderCreateReqDTO;
import com.vv.cloudfarming.order.dto.resp.OrderCreateRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderPageRespDTO;
import com.vv.cloudfarming.order.service.OrderService;
import com.vv.cloudfarming.order.service.PayService;
import com.vv.cloudfarming.order.strategy.AbstractOrderCreateTemplate;
import com.vv.cloudfarming.order.strategy.OrderTemplateFactory;
import com.vv.cloudfarming.order.utils.ProductUtil;
import com.vv.cloudfarming.order.utils.RedisIdWorker;
import com.vv.cloudfarming.product.dao.entity.Shop;
import com.vv.cloudfarming.product.dao.mapper.ShopMapper;
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
    private final ShopMapper shopMapper;
    private final ProductUtil productUtil;
    private final RedisIdWorker redisIdWorker;

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
    public IPage<OrderPageRespDTO> listOrder(OrderPageReqDTO requestParam) {
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
            Shop shop = shopMapper.selectById(each.getShopId());
            ArrayList<ProductSummaryDTO> summaryList = new ArrayList<>();
            productUtil.buildProductSummary(each.getId(),each.getOrderType(),summaryList);
            return OrderPageRespDTO.builder()
                    .id(each.getId())
                    .shopName(shop.getShopName())
                    .items(summaryList)
                    .totalPrice(each.getTotalAmount())
                    .build();
        });
    }

    private String generatePayOrderNo(Long userId) {
        return redisIdWorker.generateId("paySN").toString();
    }
}
