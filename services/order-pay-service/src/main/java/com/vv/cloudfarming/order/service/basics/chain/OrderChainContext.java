package com.vv.cloudfarming.order.service.basics.chain;

import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 上下文容器
 */
@Component
public class OrderChainContext<T> implements ApplicationContextAware, CommandLineRunner {

    private ApplicationContext applicationContext;
    private final List<OrderAbstractChainHandler> abstractChainHandlerContainer = new ArrayList<>();

    /**
     * 责任链组件执行
     *
     * @param requestParam 请求参数
     */
    public void handler(T requestParam) {
        abstractChainHandlerContainer.forEach(handler -> handler.handler(requestParam));
    }

    @Override
    public void run(String... args) throws Exception {
        // 从 Spring IOC 容器中获取指定接口 Spring Bean 集合
        Map<String, OrderAbstractChainHandler> chainFilterMap = applicationContext.getBeansOfType(OrderAbstractChainHandler.class);
        chainFilterMap.forEach((beanName, bean) -> {
            abstractChainHandlerContainer.add(bean);
        });
        // 排序
        abstractChainHandlerContainer.sort(Comparator.comparing(Ordered::getOrder));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
