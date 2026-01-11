package com.vv.cloudfarming.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.shop.dao.entity.SkuDO;
import com.vv.cloudfarming.shop.dto.req.SkuCreateReqDTO;

/**
 * SKU服务接口层
 */
public interface SkuService extends IService<SkuDO> {

    /**
     * 创建商品sku
     *
     * @param requestParam 请求参数
     */
    void createSku(SkuCreateReqDTO requestParam);
}
