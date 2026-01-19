package com.vv.cloudfarming.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.shop.dao.entity.SkuDO;
import com.vv.cloudfarming.shop.dto.req.SkuCreateReqDTO;
import com.vv.cloudfarming.shop.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.shop.dto.SpuPriceSummaryDTO;

import java.util.List;

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

    /**
     * 获取 SKU 详情（包含 SPU 信息）
     *
     * @param id SKU ID
     * @return SKU 详情
     */
    SkuRespDTO getSkuDetail(Long id);

    /**
     * 根据 SPU ID 获取该商品下的所有 SKU
     *
     * @param spuId SPU ID
     * @return SKU 列表
     */
    List<SkuRespDTO> getSkusBySpuId(Long spuId);

    /**
     * 批量获取 SKU 详情
     * @param ids SKU ID 列表
     * @return 详情列表
     */
    List<SkuRespDTO> listSkuDetailsByIds(List<Long> ids);

    /**
     * 批量获取 SPU 的价格摘要
     * @param spuIds SPU ID 列表
     * @return 价格摘要列表
     */
    List<SpuPriceSummaryDTO> listPriceSummaryBySpuIds(List<Long> spuIds);

    /**
     * 锁定库存（扣减库存）
     *
     * @param skuId SKU ID
     * @param count 数量
     * @return 是否成功
     */
    boolean lockStock(Long skuId, Integer count);

    /**
     * 解锁库存（回滚库存）
     *
     * @param skuId SKU ID
     * @param count 数量
     * @return 是否成功
     */
    boolean unlockStock(Long skuId, Integer count);
}
