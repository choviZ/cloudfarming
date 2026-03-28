package com.vv.cloudfarming.product.service;

import com.vv.cloudfarming.product.dto.req.CartItemAddReqDTO;
import com.vv.cloudfarming.product.dto.req.CartItemUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.CartCheckoutPreviewRespDTO;
import com.vv.cloudfarming.product.dto.resp.CartRespDTO;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    Boolean addToCart(CartItemAddReqDTO requestParam);

    Boolean updateCartItem(Long skuId, CartItemUpdateReqDTO requestParam);

    Boolean removeFromCart(Long skuId);

    Boolean batchRemoveFromCart(List<Long> skuIds);

    Boolean clearCart();

    CartRespDTO getCart();

    Boolean selectCartItem(Long skuId, Boolean selected);

    Boolean selectAllCartItems(Boolean selected);

    CartCheckoutPreviewRespDTO getCheckoutPreview();
}
