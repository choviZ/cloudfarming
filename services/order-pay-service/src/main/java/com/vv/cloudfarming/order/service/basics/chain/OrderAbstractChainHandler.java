package com.vv.cloudfarming.order.service.basics.chain;

import org.springframework.core.Ordered;

public interface OrderAbstractChainHandler<T> extends Ordered {

    /**
     * 执行责任链逻辑
     *
     * @param requestPram 入参
     */
    void handler(T requestPram);
}
