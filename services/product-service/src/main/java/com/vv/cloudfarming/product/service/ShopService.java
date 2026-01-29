package com.vv.cloudfarming.product.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.product.dao.entity.Shop;
import com.vv.cloudfarming.product.dto.resp.ShopInfoRespDTO;

/**
 * 店铺服务接口层
 */
public interface ShopService extends IService<Shop> {

    /**
     * 查询指定店铺信息
     */
    ShopInfoRespDTO getShopInfo(Long shopId);

    /**
     * 创建店铺
     */
    void saveShop();
}
