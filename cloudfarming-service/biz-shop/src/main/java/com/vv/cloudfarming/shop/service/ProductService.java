package com.vv.cloudfarming.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.shop.dao.entity.ProductDO;
import com.vv.cloudfarming.shop.dto.req.ProductCreateReqDTO;
import com.vv.cloudfarming.shop.dto.req.ProductPageQueryReqDTO;
import com.vv.cloudfarming.shop.dto.req.ProductUpdateReqDTO;
import com.vv.cloudfarming.shop.dto.req.ProductUpdateShelfStatusRequestDTO;
import com.vv.cloudfarming.shop.dto.resp.ProductRespDTO;

/**
 * 商品服务接口层
 */
public interface ProductService extends IService<ProductDO> {

    /**
     * 创建商品
     *
     * @param requestParam 请求参数
     */
    void createProduct(ProductCreateReqDTO requestParam);

    /**
     * 根据id查询商品详情
     *
     * @param id 商品id
     * @return 商品详情
     */
    ProductRespDTO getProductById(Long id);

    /**
     * 修改商品信息
     *
     * @param requestParam 请求参数
     * @return 是否成功
     */
    boolean updateProduct(ProductUpdateReqDTO requestParam);

    /**
     * 删除商品
     *
     * @param id 商品id
     * @return 是否成功
     */
    boolean deleteProduct(Long id);

    /**
     * 根据店铺id分页查询
     *
     * @param requestParam 分页查询参数
     * @return 商品列表
     */
    IPage<ProductRespDTO> PageProductByShopId(ProductPageQueryReqDTO requestParam);

    /**
     * 修改商品上/下架状态
     * @param requestParam 请求参数
     */
    void updateShelfStatus(ProductUpdateShelfStatusRequestDTO requestParam);
}
