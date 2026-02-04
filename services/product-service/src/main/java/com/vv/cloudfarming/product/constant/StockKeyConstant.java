package com.vv.cloudfarming.product.constant;

/**
 * 库存相关key
 */
public interface StockKeyConstant {

    String STOCK_AVAILABLE_CACHE_KEY = "cloudfarming:stock:available:%s:%s";

    String STOCK_LOCK_CACHE_KEY = "cloudfarming:stock:lock:%s:%s";
}