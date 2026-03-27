package com.vv.cloudfarming.product.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.dao.entity.Shop;
import com.vv.cloudfarming.product.dao.mapper.ShopMapper;
import com.vv.cloudfarming.product.dto.req.ShopUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.ShopRespDTO;
import com.vv.cloudfarming.product.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 店铺服务实现
 */
@Service
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Override
    public ShopRespDTO getShopInfo(Long shopId) {
        if (shopId == null || shopId <= 0) {
            throw new ClientException("店铺编号不合法");
        }
        Shop shop = baseMapper.selectById(shopId);
        if (shop == null) {
            throw new ClientException("店铺不存在");
        }
        return BeanUtil.toBean(shop, ShopRespDTO.class);
    }

    @Override
    public ShopRespDTO getMyShopInfo(Long farmerId) {
        Shop shop = getOrCreateShopByFarmerId(farmerId);
        return BeanUtil.toBean(shop, ShopRespDTO.class);
    }

    @Override
    public void saveShop() {
        getOrCreateShopByFarmerId(StpUtil.getLoginIdAsLong());
    }

    @Override
    public void updateShop(ShopUpdateReqDTO requestParam) {
        Long operateShopId = resolveOperateShopId(requestParam.getId());
        Shop shop = baseMapper.selectById(operateShopId);
        if (shop == null) {
            throw new ClientException("店铺不存在");
        }

        LambdaUpdateWrapper<Shop> updateWrapper = Wrappers.lambdaUpdate(Shop.class)
                .eq(Shop::getId, operateShopId)
                .set(Shop::getShopName, requestParam.getShopName().trim())
                .set(Shop::getShopAvatar, StrUtil.emptyToNull(requestParam.getShopAvatar()))
                .set(Shop::getShopBanner, StrUtil.emptyToNull(requestParam.getShopBanner()))
                .set(Shop::getDescription, StrUtil.emptyToNull(requestParam.getDescription()))
                .set(Shop::getAnnouncement, StrUtil.emptyToNull(requestParam.getAnnouncement()));

        int updated = baseMapper.update(null, updateWrapper);
        if (updated != 1) {
            throw new ServiceException("更新店铺信息失败");
        }
    }

    private Long resolveOperateShopId(Long requestShopId) {
        if (requestShopId == null || requestShopId <= 0) {
            throw new ClientException("店铺编号不合法");
        }

        if (StpUtil.hasRole(UserRoleConstant.ADMIN_DESC)) {
            return requestShopId;
        }

        Long farmerId = StpUtil.getLoginIdAsLong();
        Shop myShop = getOrCreateShopByFarmerId(farmerId);
        if (!Objects.equals(myShop.getId(), requestShopId)) {
            throw new ClientException("没有权限修改该店铺");
        }
        return myShop.getId();
    }

    private Shop getOrCreateShopByFarmerId(Long farmerId) {
        if (farmerId == null || farmerId <= 0) {
            throw new ClientException("农户编号不合法");
        }

        Shop shop = baseMapper.getByFarmerId(farmerId);
        if (shop != null) {
            return shop;
        }

        Shop newShop = Shop.builder()
                .shopName("默认店铺")
                .farmerId(farmerId)
                .status(1)
                .build();
        int inserted = baseMapper.insert(newShop);
        if (inserted != 1) {
            throw new ServiceException("创建默认店铺失败");
        }

        Shop createdShop = baseMapper.getByFarmerId(farmerId);
        if (createdShop == null) {
            throw new ServiceException("创建默认店铺失败");
        }
        return createdShop;
    }
}
