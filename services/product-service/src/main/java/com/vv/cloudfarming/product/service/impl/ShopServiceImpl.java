package com.vv.cloudfarming.product.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.dao.entity.Shop;
import com.vv.cloudfarming.product.dao.mapper.ShopMapper;
import com.vv.cloudfarming.product.dto.resp.ShopInfoRespDTO;
import com.vv.cloudfarming.product.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 店铺服务实现层
 */
@Service
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Override
    public ShopInfoRespDTO getShopInfo(Long shopId) {
        if (shopId == null || shopId <= 0) {
            throw new ClientException("店铺编号不合法");
        }
        Shop shop = baseMapper.selectById(shopId);
        return BeanUtil.toBean(shop,ShopInfoRespDTO.class);
    }

    @Override
    public void saveShop() {
        long loginId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<Shop> wrapper = Wrappers.lambdaQuery(Shop.class)
                .eq(Shop::getFarmerId, loginId);
        Shop shop = baseMapper.selectOne(wrapper);
        // 不存在，创建默认店铺
        if (shop == null) {
            shop = Shop.builder()
                    .shopName("默认店铺")
                    .farmerId(loginId)
                    .status(1)
                    .build();
            int inserted = baseMapper.insert(shop);
            if (inserted != 1) {
                throw new ServiceException("创建默认店铺失败");
            }
        }
    }
}
