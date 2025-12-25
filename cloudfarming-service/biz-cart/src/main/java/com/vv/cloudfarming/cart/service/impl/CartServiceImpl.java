package com.vv.cloudfarming.cart.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.vv.cloudfarming.cart.dao.entity.CartItemDO;
import com.vv.cloudfarming.cart.dto.req.CartItemAddReqDTO;
import com.vv.cloudfarming.cart.dto.req.CartItemUpdateReqDTO;
import com.vv.cloudfarming.cart.dto.resp.CartItemRespDTO;
import com.vv.cloudfarming.cart.dto.resp.CartRespDTO;
import com.vv.cloudfarming.cart.service.CartService;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.shop.dto.resp.ProductRespDTO;
import com.vv.cloudfarming.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ProductService productService;
    // 购物车Redis Key前缀
    private static final String CART_KEY_PREFIX = "cart:user:";
    // 购物车TTL时间 14天 1209600秒
    private static final long CART_TTL_SECONDS = 14 * 24 * 60 * 60L;

    @Override
    public Boolean addToCart(CartItemAddReqDTO requestParam) {
        String cartKey = getCartKey();
        Long skuId = requestParam.getSkuId();
        Integer quantity = requestParam.getQuantity();
        Boolean selected = requestParam.getSelected();
        // 检查商品是否存在
        ProductRespDTO product = productService.getProductById(skuId);
        if (product == null) {
            throw new ClientException("商品不存在");
        }
        try {
            // 获取当前购物车项
            String cartItemJson = (String) stringRedisTemplate.opsForHash().get(cartKey, skuId.toString());
            CartItemDO cartItem;
            if (cartItemJson != null) {
                // 商品已在购物车中，更新数量
                cartItem = JSON.parseObject(cartItemJson, CartItemDO.class);
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                if (selected != null) {
                    cartItem.setSelected(selected);
                }
            } else {
                // 新增购物车项
                cartItem = new CartItemDO();
                cartItem.setSkuId(skuId);
                cartItem.setQuantity(quantity);
                cartItem.setSelected(selected != null ? selected : true);
                cartItem.setHasStock(product.getStock() > quantity);
            }
            // 保存到Redis
            stringRedisTemplate.opsForHash().put(cartKey, skuId.toString(), JSON.toJSONString(cartItem));
            // 刷新TTL
            refreshCartTTL();
            return true;
        } catch (Exception e) {
            log.error("添加购物车失败", e.getMessage());
            throw new ServiceException("添加购物车失败");
        }
    }

    @Override
    public Boolean updateCartItem(CartItemUpdateReqDTO requestParam) {
        try {
            String cartKey = getCartKey();
            Long skuId = requestParam.getSkuId();

            // 检查购物车项是否存在
            String cartItemJson = (String) stringRedisTemplate.opsForHash().get(cartKey, skuId.toString());
            if (cartItemJson == null) {
                throw new ClientException("购物车中不存在该商品");
            }

            // 更新购物车项
            CartItemDO cartItem = JSON.parseObject(cartItemJson, CartItemDO.class);
            cartItem.setQuantity(requestParam.getQuantity());
            cartItem.setSelected(requestParam.getSelected());
            // 保存到Redis
            stringRedisTemplate.opsForHash().put(cartKey, skuId.toString(), JSON.toJSONString(cartItem));
            // 刷新TTL
            refreshCartTTL();
            return true;
        } catch (Exception e) {
            log.error("更新购物车项失败", e.getMessage());
            throw new ServiceException("更新购物车项失败");
        }
    }

    @Override
    public Boolean removeFromCart(String skuId) {
        try {
            String cartKey = getCartKey();
            // 删除购物车项
            Long result = stringRedisTemplate.opsForHash().delete(cartKey, skuId.toString());
            // 如果购物车不为空，刷新TTL
            if (result > 0 && stringRedisTemplate.opsForHash().size(cartKey) > 0) {
                refreshCartTTL();
            }
            return result > 0;
        } catch (Exception e) {
            log.error("删除购物车项失败", e.getMessage());
            throw new ServiceException("删除购物车项失败");
        }
    }

    @Override
    public Boolean batchRemoveFromCart(List<String> skuIds) {
        try {
            String cartKey = getCartKey();
            // 批量删除购物车项
            stringRedisTemplate.opsForHash().delete(cartKey, skuIds.toArray());
            // 如果购物车不为空，刷新TTL
            if (stringRedisTemplate.opsForHash().size(cartKey) > 0) {
                refreshCartTTL();
            }
            return true;
        } catch (Exception e) {
            log.error("批量删除购物车项失败", e.getMessage());
            throw new ServiceException("批量删除购物车项失败");
        }
    }

    @Override
    public Boolean clearCart() {
        try {
            String cartKey = getCartKey();
            // 删除整个购物车
            return stringRedisTemplate.delete(cartKey);
        } catch (Exception e) {
            log.error("清空购物车失败", e.getMessage());
            throw new ServiceException("清空购物车失败");
        }
    }

    @Override
    public CartRespDTO getCart() {
        try {
            String cartKey = getCartKey();
            // 获取购物车所有项
            Map<Object, Object> cartItemsMap = stringRedisTemplate.opsForHash().entries(cartKey);
            if (cartItemsMap.isEmpty()) {
                return new CartRespDTO();
            }
            // 转换为CartItemDO列表
            List<CartItemDO> cartItems = cartItemsMap.values().stream()
                    .map(obj -> JSON.parseObject(obj.toString(), CartItemDO.class))
                    .collect(Collectors.toList());
            // 获取商品信息
            List<Long> skuIds = cartItems.stream()
                    .map(CartItemDO::getSkuId)
                    .collect(Collectors.toList());

            Map<Long, ProductRespDTO> productInfoMap = productService.batchProductsByIds(skuIds)
                    .stream()
                    .collect(Collectors.toMap(ProductRespDTO::getId, product -> product));

            // 转换为响应DTO
            List<CartItemRespDTO> cartItemRespList = new ArrayList<>();
            Integer totalQuantity = 0;
            BigDecimal totalAmount = BigDecimal.ZERO;
            Boolean allHasStock = true;

            for (CartItemDO cartItem : cartItems) {
                ProductRespDTO productInfo = productInfoMap.get(cartItem.getSkuId());
                if (productInfo == null) {
                    continue;
                }
                CartItemRespDTO cartItemResp = new CartItemRespDTO();
                cartItemResp.setProductId(cartItem.getSkuId());
                cartItemResp.setQuantity(cartItem.getQuantity());
                cartItemResp.setSelected(cartItem.getSelected());
                cartItemResp.setHasStock(productInfo.getStock() > 0);

                // 设置商品信息
                cartItemResp.setProductName(productInfo.getProductName());
                cartItemResp.setProductImage(productInfo.getProductImg());
                cartItemResp.setPrice(productInfo.getPrice());
                cartItemResp.setShopId(productInfo.getShopId());

                // 计算总价
                BigDecimal totalPrice = productInfo.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));
                cartItemResp.setTotalPrice(totalPrice);

                cartItemRespList.add(cartItemResp);

                // 累计选中商品数量和金额
                if (cartItem.getSelected()) {
                    totalQuantity += cartItem.getQuantity();
                    totalAmount = totalAmount.add(totalPrice);

                    if (productInfo.getStock() <= 0) {
                        allHasStock = false;
                    }
                }
            }

            // 构建响应
            CartRespDTO cartResp = new CartRespDTO();
            cartResp.setCartItems(cartItemRespList);
            cartResp.setTotalQuantity(totalQuantity);
            cartResp.setTotalAmount(totalAmount);
            cartResp.setAllHasStock(allHasStock);

            // 刷新TTL
            refreshCartTTL();

            return cartResp;
        } catch (Exception e) {
            log.error("获取购物车失败", e.getMessage());
            throw new ServiceException("获取购物车失败");
        }
    }

    @Override
    public Boolean selectCartItem(String skuId, Boolean selected) {
        try {
            String cartKey = getCartKey();
            // 检查购物车项是否存在
            String cartItemJson = (String) stringRedisTemplate.opsForHash().get(cartKey, skuId.toString());
            if (cartItemJson == null) {
                throw new ClientException("购物车中不存在该商品");
            }

            // 更新选中状态
            CartItemDO cartItem = JSON.parseObject(cartItemJson, CartItemDO.class);
            cartItem.setSelected(selected);
            // 保存到Redis
            stringRedisTemplate.opsForHash().put(cartKey, skuId.toString(), JSON.toJSONString(cartItem));
            // 刷新TTL
            refreshCartTTL();
            return true;
        } catch (Exception e) {
            log.error("更新购物车项选中状态失败", e.getMessage());
            throw new ServiceException("更新购物车项选中状态失败");
        }
    }

    @Override
    public Boolean selectAllCartItems(Boolean selected) {
        try {
            String cartKey = getCartKey();

            // 获取购物车所有项
            Map<Object, Object> cartItemsMap = stringRedisTemplate.opsForHash().entries(cartKey);

            if (cartItemsMap.isEmpty()) {
                return true;
            }

            // 批量更新选中状态
            Map<String, String> updates = new HashMap<>();
            for (Map.Entry<Object, Object> entry : cartItemsMap.entrySet()) {
                String skuId = entry.getKey().toString();
                CartItemDO cartItem = JSON.parseObject(entry.getValue().toString(), CartItemDO.class);
                cartItem.setSelected(selected);
                updates.put(skuId, JSON.toJSONString(cartItem));
            }

            // 批量保存到Redis
            stringRedisTemplate.opsForHash().putAll(cartKey, updates);

            // 刷新TTL
            refreshCartTTL();

            return true;
        } catch (Exception e) {
            log.error("全选/取消全选购物车项失败", e.getMessage());
            throw new ServiceException("全选/取消全选购物车项失败");
        }
    }

    /**
     * 获取当前用户的购物车Key
     */
    private String getCartKey() {
        long userId = StpUtil.getLoginIdAsLong();
        return CART_KEY_PREFIX + userId;
    }

    /**
     * 刷新购物车TTL
     */
    private void refreshCartTTL() {
        String cartKey = getCartKey();
        stringRedisTemplate.expire(cartKey, CART_TTL_SECONDS, TimeUnit.SECONDS);
    }
}