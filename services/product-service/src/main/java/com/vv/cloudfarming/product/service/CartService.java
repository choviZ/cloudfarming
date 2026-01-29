package com.vv.cloudfarming.product.service;

import com.vv.cloudfarming.product.dto.req.CartItemAddReqDTO;
import com.vv.cloudfarming.product.dto.req.CartItemUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.CartRespDTO;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 添加商品到购物车
     *
     * @param requestParam 添加购物车项参数
     * @return 是否成功
     */
    Boolean addToCart(CartItemAddReqDTO requestParam);

    /**
     * 更新购物车项
     *
     * @param requestParam 更新购物车项参数
     * @return 是否成功
     */
    Boolean updateCartItem(CartItemUpdateReqDTO requestParam);

    /**
     * 删除购物车项
     *
     * @param skuId 商品SKU ID
     * @return 是否成功
     */
    Boolean removeFromCart(String skuId);

    /**
     * 批量删除购物车项
     *
     * @param skuIds 商品SKU ID列表
     * @return 是否成功
     */
    Boolean batchRemoveFromCart(List<String> skuIds);

    /**
     * 清空购物车
     *
     * @return 是否成功
     */
    Boolean clearCart();

    /**
     * 获取当前用户的购物车
     *
     * @return 购物车信息
     */
    CartRespDTO getCart();

    /**
     * 选中/取消选中购物车项
     *
     * @param skuId 商品SKU ID
     * @param selected 是否选中
     * @return 是否成功
     */
    Boolean selectCartItem(String skuId, Boolean selected);

    /**
     * 全选/取消全选购物车项
     *
     * @param selected 是否全选
     * @return 是否成功
     */
    Boolean selectAllCartItems(Boolean selected);
}