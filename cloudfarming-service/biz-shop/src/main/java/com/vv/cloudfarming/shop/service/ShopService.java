package com.vv.cloudfarming.shop.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.shop.dao.entity.Shop;
import com.vv.cloudfarming.shop.dto.resp.ShopInfoRespDTO;

/**
 * 店铺服务接口层
 */
public interface ShopService extends IService<Shop> {

    /**
     * 查询指定店铺信息
     */
    ShopInfoRespDTO getShopInfo(Long shopId);

    /**
     * 查询“我的店铺”信息
     */
    ShopInfoRespDTO getMyShopInfo();
}
