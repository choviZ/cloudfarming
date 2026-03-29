package com.vv.cloudfarming.order.service.basics.chain;

import org.springframework.beans.BeansException;
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
 * 责任链上下文容器
 */
@Component
public class OrderChainContext<T> implements ApplicationContextAware, CommandLineRunner {

    private ApplicationContext applicationContext;
    private final List<OrderAbstractChainHandler> abstractChainHandlerContainer = new ArrayList<>();

    public void handler(T requestParam) {
        initChainHandlersIfNecessary();
        abstractChainHandlerContainer.forEach(handler -> handler.handler(requestParam));
    }

    @Override
    public void run(String... args) {
        initChainHandlersIfNecessary();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private synchronized void initChainHandlersIfNecessary() {
        if (!abstractChainHandlerContainer.isEmpty()) {
            return;
        }
        Map<String, OrderAbstractChainHandler> chainFilterMap = applicationContext.getBeansOfType(OrderAbstractChainHandler.class);
        chainFilterMap.forEach((beanName, bean) -> abstractChainHandlerContainer.add(bean));
        abstractChainHandlerContainer.sort(Comparator.comparing(Ordered::getOrder));
    }
}
