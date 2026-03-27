package com.vv.cloudfarming.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.product.dao.entity.Shop;
import com.vv.cloudfarming.product.dto.req.ShopUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.ShopRespDTO;

/**
 * 店铺服务接口
 */
public interface ShopService extends IService<Shop> {

    /**
     * 根据店铺 ID 查询店铺信息
     */
    ShopRespDTO getShopInfo(Long shopId);

    /**
     * 查询当前农户的店铺信息，不存在则自动创建默认店铺
     */
    ShopRespDTO getMyShopInfo(Long farmerId);

    /**
     * 创建默认店铺
     */
    void saveShop();

    /**
     * 更新店铺信息
     */
    void updateShop(ShopUpdateReqDTO requestParam);
}
