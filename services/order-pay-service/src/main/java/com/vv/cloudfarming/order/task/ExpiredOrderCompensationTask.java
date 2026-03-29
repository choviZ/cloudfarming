package com.vv.cloudfarming.order.task;

import com.vv.cloudfarming.order.service.OrderExpireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 过期未支付订单兜底扫描任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredOrderCompensationTask {

    private final OrderExpireService orderExpireService;

    @Scheduled(initialDelay = 300000, fixedDelay = 300000)
    public void closeExpiredOrders() {
        int closedCount = orderExpireService.closeExpiredPayOrders(100);
        if (closedCount > 0) {
            log.info("过期未支付订单兜底关闭完成，本次关闭 {} 个支付单", closedCount);
        }
    }
}
