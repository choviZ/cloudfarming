package com.vv.cloudfarming.product.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vv.cloudfarming.common.enums.ShelfStatusEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.dao.entity.CartItemDO;
import com.vv.cloudfarming.product.dao.entity.Shop;
import com.vv.cloudfarming.product.dao.entity.SpuDO;
import com.vv.cloudfarming.product.dao.mapper.CartItemMapper;
import com.vv.cloudfarming.product.dao.mapper.ShopMapper;
import com.vv.cloudfarming.product.dao.mapper.SpuMapper;
import com.vv.cloudfarming.product.dto.req.CartItemAddReqDTO;
import com.vv.cloudfarming.product.dto.req.CartItemUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.CartCheckoutGroupRespDTO;
import com.vv.cloudfarming.product.dto.resp.CartCheckoutPreviewRespDTO;
import com.vv.cloudfarming.product.dto.resp.CartItemRespDTO;
import com.vv.cloudfarming.product.dto.resp.CartRespDTO;
import com.vv.cloudfarming.product.dto.resp.SkuRespDTO;
import com.vv.cloudfarming.product.enums.AuditStatusEnum;
import com.vv.cloudfarming.product.service.CartService;
import com.vv.cloudfarming.product.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final Integer SHOP_STATUS_NORMAL = 1;
    private static final Integer DEL_FLAG_DELETED = 1;

    private final CartItemMapper cartItemMapper;
    private final SkuService skuService;
    private final SpuMapper spuMapper;
    private final ShopMapper shopMapper;

    @Override
    public Boolean addToCart(CartItemAddReqDTO requestParam) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long skuId = requestParam.getSkuId();
        Boolean selected = requestParam.getSelected() == null ? Boolean.TRUE : requestParam.getSelected();

        CartItemDO existed = getCartItem(userId, skuId);
        if (existed != null) {
            int targetQuantity = requestParam.getQuantity() + existed.getQuantity();
            validateCartItemForWrite(skuId, targetQuantity);
            existed.setQuantity(targetQuantity);
            existed.setSelected(selected);
            if (cartItemMapper.updateById(existed) != 1) {
                throw new ServiceException("加入购物车失败");
            }
            return true;
        }

        CartItemDO deleted = getDeletedCartItem(userId, skuId);
        if (deleted != null) {
            validateCartItemForWrite(skuId, requestParam.getQuantity());
            if (cartItemMapper.restoreDeletedById(deleted.getId(), requestParam.getQuantity(), selected) != 1) {
                throw new ServiceException("加入购物车失败");
            }
            return true;
        }

        int targetQuantity = requestParam.getQuantity();
        validateCartItemForWrite(skuId, targetQuantity);
        CartItemDO cartItem = new CartItemDO();
        cartItem.setUserId(userId);
        cartItem.setSkuId(skuId);
        cartItem.setQuantity(targetQuantity);
        cartItem.setSelected(selected);
        if (cartItemMapper.insert(cartItem) != 1) {
            throw new ServiceException("加入购物车失败");
        }
        return true;
    }

    @Override
    public Boolean updateCartItem(Long skuId, CartItemUpdateReqDTO requestParam) {
        Long userId = StpUtil.getLoginIdAsLong();
        CartItemDO existed = getCartItem(userId, skuId);
        if (existed == null) {
            throw new ClientException("购物车中不存在该商品");
        }

        validateCartItemForWrite(skuId, requestParam.getQuantity());
        existed.setQuantity(requestParam.getQuantity());
        if (cartItemMapper.updateById(existed) != 1) {
            throw new ServiceException("更新购物车数量失败");
        }
        return true;
    }

    @Override
    public Boolean removeFromCart(Long skuId) {
        Long userId = StpUtil.getLoginIdAsLong();
        int deleted = cartItemMapper.delete(Wrappers.lambdaQuery(CartItemDO.class)
            .eq(CartItemDO::getUserId, userId)
            .eq(CartItemDO::getSkuId, skuId));
        return deleted > 0;
    }

    @Override
    public Boolean batchRemoveFromCart(List<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return true;
        }
        Long userId = StpUtil.getLoginIdAsLong();
        cartItemMapper.delete(Wrappers.lambdaQuery(CartItemDO.class)
            .eq(CartItemDO::getUserId, userId)
            .in(CartItemDO::getSkuId, skuIds));
        return true;
    }

    @Override
    public Boolean clearCart() {
        Long userId = StpUtil.getLoginIdAsLong();
        cartItemMapper.delete(Wrappers.lambdaQuery(CartItemDO.class)
            .eq(CartItemDO::getUserId, userId));
        return true;
    }

    @Override
    public CartRespDTO getCart() {
        List<CartItemDO> cartItems = listCartItems(StpUtil.getLoginIdAsLong(), null);
        if (CollUtil.isEmpty(cartItems)) {
            return emptyCartResp();
        }

        List<CartItemRespDTO> itemRespList = buildCartItemRespList(cartItems);
        CartRespDTO respDTO = new CartRespDTO();
        respDTO.setCartItems(itemRespList);
        respDTO.setTotalQuantity(itemRespList.stream()
            .filter(item -> Boolean.TRUE.equals(item.getSelected()) && Boolean.TRUE.equals(item.getCanCheckout()))
            .map(CartItemRespDTO::getQuantity)
            .reduce(0, Integer::sum));
        respDTO.setTotalAmount(itemRespList.stream()
            .filter(item -> Boolean.TRUE.equals(item.getSelected()) && Boolean.TRUE.equals(item.getCanCheckout()))
            .map(CartItemRespDTO::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        respDTO.setHasInvalidItems(itemRespList.stream().anyMatch(item -> !Boolean.TRUE.equals(item.getCanCheckout())));
        return respDTO;
    }

    @Override
    public Boolean selectCartItem(Long skuId, Boolean selected) {
        Long userId = StpUtil.getLoginIdAsLong();
        CartItemDO existed = getCartItem(userId, skuId);
        if (existed == null) {
            throw new ClientException("购物车中不存在该商品");
        }
        existed.setSelected(selected);
        if (cartItemMapper.updateById(existed) != 1) {
            throw new ServiceException("更新购物车选中状态失败");
        }
        return true;
    }

    @Override
    public Boolean selectAllCartItems(Boolean selected) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<CartItemDO> cartItems = listCartItems(userId, null);
        if (CollUtil.isEmpty(cartItems)) {
            return true;
        }

        if (!Boolean.TRUE.equals(selected)) {
            cartItemMapper.update(null, Wrappers.lambdaUpdate(CartItemDO.class)
                .eq(CartItemDO::getUserId, userId)
                .set(CartItemDO::getSelected, false));
            return true;
        }

        List<CartItemRespDTO> itemRespList = buildCartItemRespList(cartItems);
        Map<Long, Boolean> selectableMap = itemRespList.stream()
            .collect(Collectors.toMap(CartItemRespDTO::getSkuId, CartItemRespDTO::getCanCheckout));
        for (CartItemDO cartItem : cartItems) {
            cartItem.setSelected(Boolean.TRUE.equals(selectableMap.get(cartItem.getSkuId())));
            if (cartItemMapper.updateById(cartItem) != 1) {
                throw new ServiceException("更新购物车选中状态失败");
            }
        }
        return true;
    }

    @Override
    public CartCheckoutPreviewRespDTO getCheckoutPreview() {
        List<CartItemDO> selectedItems = listCartItems(StpUtil.getLoginIdAsLong(), true);
        if (CollUtil.isEmpty(selectedItems)) {
            return emptyCheckoutPreview();
        }

        List<CartItemRespDTO> itemRespList = buildCartItemRespList(selectedItems);
        List<CartItemRespDTO> invalidItems = itemRespList.stream()
            .filter(item -> !Boolean.TRUE.equals(item.getCanCheckout()))
            .collect(Collectors.toList());
        List<CartItemRespDTO> validItems = itemRespList.stream()
            .filter(item -> Boolean.TRUE.equals(item.getCanCheckout()))
            .collect(Collectors.toList());

        Map<Long, CartCheckoutGroupRespDTO> groupMap = new LinkedHashMap<>();
        for (CartItemRespDTO item : validItems) {
            Long shopId = item.getShopId();
            CartCheckoutGroupRespDTO group = groupMap.computeIfAbsent(shopId, key -> {
                CartCheckoutGroupRespDTO groupRespDTO = new CartCheckoutGroupRespDTO();
                groupRespDTO.setShopId(item.getShopId());
                groupRespDTO.setShopName(item.getShopName());
                groupRespDTO.setItems(new ArrayList<>());
                groupRespDTO.setTotalQuantity(0);
                groupRespDTO.setTotalAmount(BigDecimal.ZERO);
                return groupRespDTO;
            });
            group.getItems().add(item);
            group.setTotalQuantity(group.getTotalQuantity() + item.getQuantity());
            group.setTotalAmount(group.getTotalAmount().add(item.getTotalPrice()));
        }

        CartCheckoutPreviewRespDTO respDTO = new CartCheckoutPreviewRespDTO();
        respDTO.setGroups(new ArrayList<>(groupMap.values()));
        respDTO.setInvalidItems(invalidItems);
        respDTO.setTotalQuantity(validItems.stream()
            .map(CartItemRespDTO::getQuantity)
            .reduce(0, Integer::sum));
        respDTO.setTotalAmount(validItems.stream()
            .map(CartItemRespDTO::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        respDTO.setCanSubmit(CollUtil.isNotEmpty(validItems) && CollUtil.isEmpty(invalidItems));
        return respDTO;
    }

    private List<CartItemDO> listCartItems(Long userId, Boolean selected) {
        LambdaQueryWrapper<CartItemDO> queryWrapper = Wrappers.lambdaQuery(CartItemDO.class)
            .eq(CartItemDO::getUserId, userId)
            .eq(selected != null, CartItemDO::getSelected, selected)
            .orderByDesc(CartItemDO::getUpdateTime)
            .orderByDesc(CartItemDO::getId);
        return cartItemMapper.selectList(queryWrapper);
    }

    private CartItemDO getCartItem(Long userId, Long skuId) {
        return cartItemMapper.selectOne(Wrappers.lambdaQuery(CartItemDO.class)
            .eq(CartItemDO::getUserId, userId)
            .eq(CartItemDO::getSkuId, skuId)
            .last("limit 1"));
    }

    private CartItemDO getDeletedCartItem(Long userId, Long skuId) {
        CartItemDO cartItem = cartItemMapper.selectAnyByUserIdAndSkuId(userId, skuId);
        if (cartItem == null || !Objects.equals(cartItem.getDelFlag(), DEL_FLAG_DELETED)) {
            return null;
        }
        return cartItem;
    }

    private void validateCartItemForWrite(Long skuId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ClientException("商品数量至少为 1");
        }

        SkuRespDTO sku = skuService.getSkuDetail(skuId);
        if (sku == null) {
            throw new ClientException("商品不存在");
        }
        SpuDO spu = spuMapper.selectById(sku.getSpuId());
        if (spu == null) {
            throw new ClientException("商品不存在");
        }
        Shop shop = shopMapper.selectById(spu.getShopId());
        if (shop == null) {
            throw new ClientException("店铺不存在");
        }
        if (!AuditStatusEnum.APPROVED.getCode().equals(spu.getAuditStatus())) {
            throw new ClientException("商品暂不可购买");
        }
        if (!ShelfStatusEnum.ONLINE.getCode().equals(spu.getStatus())
            || !ShelfStatusEnum.ONLINE.getCode().equals(sku.getStatus())) {
            throw new ClientException("商品已下架");
        }
        if (!SHOP_STATUS_NORMAL.equals(shop.getStatus())) {
            throw new ClientException("店铺已停用");
        }
        if (sku.getStock() == null || quantity > sku.getStock()) {
            throw new ClientException("库存不足");
        }
    }

    private List<CartItemRespDTO> buildCartItemRespList(List<CartItemDO> cartItems) {
        if (CollUtil.isEmpty(cartItems)) {
            return Collections.emptyList();
        }

        List<Long> skuIds = cartItems.stream()
            .map(CartItemDO::getSkuId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, SkuRespDTO> skuMap = skuService.listSkuDetailsByIds(skuIds).stream()
            .collect(Collectors.toMap(SkuRespDTO::getId, item -> item));

        List<Long> spuIds = skuMap.values().stream()
            .map(SkuRespDTO::getSpuId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, SpuDO> spuMap = CollUtil.isEmpty(spuIds)
            ? Collections.emptyMap()
            : spuMapper.selectBatchIds(spuIds).stream()
                .collect(Collectors.toMap(SpuDO::getId, item -> item));

        List<Long> shopIds = spuMap.values().stream()
            .map(SpuDO::getShopId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, Shop> shopMap = CollUtil.isEmpty(shopIds)
            ? Collections.emptyMap()
            : shopMapper.selectBatchIds(shopIds).stream()
                .collect(Collectors.toMap(Shop::getId, item -> item));

        List<CartItemRespDTO> result = new ArrayList<>(cartItems.size());
        for (CartItemDO cartItem : cartItems) {
            result.add(buildCartItemResp(cartItem, skuMap, spuMap, shopMap));
        }
        return result;
    }

    private CartItemRespDTO buildCartItemResp(CartItemDO cartItem,
                                              Map<Long, SkuRespDTO> skuMap,
                                              Map<Long, SpuDO> spuMap,
                                              Map<Long, Shop> shopMap) {
        CartItemRespDTO respDTO = new CartItemRespDTO();
        respDTO.setSkuId(cartItem.getSkuId());
        respDTO.setQuantity(cartItem.getQuantity());
        respDTO.setSelected(Boolean.TRUE.equals(cartItem.getSelected()));

        SkuRespDTO sku = skuMap.get(cartItem.getSkuId());
        SpuDO spu = sku == null ? null : spuMap.get(sku.getSpuId());
        Shop shop = spu == null ? null : shopMap.get(spu.getShopId());
        BigDecimal price = sku == null || sku.getPrice() == null ? BigDecimal.ZERO : sku.getPrice();

        respDTO.setPrice(price);
        respDTO.setTotalPrice(price.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        if (sku != null) {
            respDTO.setSpuId(sku.getSpuId());
            respDTO.setProductImage(StrUtil.isNotBlank(sku.getSkuImage()) ? sku.getSkuImage() : null);
        }
        if (spu != null) {
            respDTO.setShopId(spu.getShopId());
            respDTO.setProductName(StrUtil.blankToDefault(spu.getTitle(), "商品信息已失效"));
            if (StrUtil.isBlank(respDTO.getProductImage())) {
                respDTO.setProductImage(resolveSpuCover(spu.getImages()));
            }
        } else {
            respDTO.setProductName("商品信息已失效");
        }
        if (shop != null) {
            respDTO.setShopName(StrUtil.blankToDefault(shop.getShopName(), "店铺信息已失效"));
        } else {
            respDTO.setShopName("店铺信息已失效");
        }

        String invalidReason = resolveInvalidReason(cartItem, sku, spu, shop);
        respDTO.setInvalidReason(invalidReason);
        respDTO.setCanCheckout(invalidReason == null);
        return respDTO;
    }

    private String resolveInvalidReason(CartItemDO cartItem, SkuRespDTO sku, SpuDO spu, Shop shop) {
        if (sku == null || spu == null) {
            return "商品已失效";
        }
        if (shop == null) {
            return "店铺信息已失效";
        }
        if (!SHOP_STATUS_NORMAL.equals(shop.getStatus())) {
            return "店铺已停用";
        }
        if (!AuditStatusEnum.APPROVED.getCode().equals(spu.getAuditStatus())) {
            return AuditStatusEnum.PENDING.getCode().equals(spu.getAuditStatus()) ? "商品审核中" : "商品审核未通过";
        }
        if (!ShelfStatusEnum.ONLINE.getCode().equals(spu.getStatus())
            || !ShelfStatusEnum.ONLINE.getCode().equals(sku.getStatus())) {
            return "商品已下架";
        }
        if (sku.getStock() == null || sku.getStock() < cartItem.getQuantity()) {
            return "库存不足";
        }
        return null;
    }

    private String resolveSpuCover(String images) {
        if (StrUtil.isBlank(images)) {
            return null;
        }
        return StrUtil.splitTrim(images, ',').stream().findFirst().orElse(null);
    }

    private CartRespDTO emptyCartResp() {
        CartRespDTO respDTO = new CartRespDTO();
        respDTO.setCartItems(Collections.emptyList());
        respDTO.setTotalQuantity(0);
        respDTO.setTotalAmount(BigDecimal.ZERO);
        respDTO.setHasInvalidItems(false);
        return respDTO;
    }

    private CartCheckoutPreviewRespDTO emptyCheckoutPreview() {
        CartCheckoutPreviewRespDTO respDTO = new CartCheckoutPreviewRespDTO();
        respDTO.setGroups(Collections.emptyList());
        respDTO.setInvalidItems(Collections.emptyList());
        respDTO.setTotalQuantity(0);
        respDTO.setTotalAmount(BigDecimal.ZERO);
        respDTO.setCanSubmit(false);
        return respDTO;
    }
}
