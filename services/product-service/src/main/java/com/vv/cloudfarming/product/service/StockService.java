package com.vv.cloudfarming.product.service;

/**
 * 库存服务
 */
public interface StockService {

    /**
     * 初始化库存缓存
     */
    Long initStock(Long id, int totalStock, int bizType);

    /**
     * 锁定库存
     */
    void lock(Long id, int quantity, int bizType);

    /**
     * 释放库存
     */
    void unlock(Long id, int quantity, int bizType);
}
