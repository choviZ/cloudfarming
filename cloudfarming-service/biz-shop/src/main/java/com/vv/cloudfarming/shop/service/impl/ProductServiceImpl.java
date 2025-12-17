package com.vv.cloudfarming.shop.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.shop.dao.entity.ProductDO;
import com.vv.cloudfarming.shop.dao.mapper.ProductMapper;
import com.vv.cloudfarming.shop.dto.req.ProductCreateReqDTO;
import com.vv.cloudfarming.shop.dto.req.ProductPageQueryReqDTO;
import com.vv.cloudfarming.shop.dto.req.ProductUpdateReqDTO;
import com.vv.cloudfarming.shop.dto.req.ProductUpdateShelfStatusRequestDTO;
import com.vv.cloudfarming.shop.dto.resp.ProductRespDTO;
import com.vv.cloudfarming.shop.dto.resp.ShopInfoRespDTO;
import com.vv.cloudfarming.shop.service.ProductService;
import com.vv.cloudfarming.shop.service.ShopService;
import com.vv.cloudfarming.user.dto.resp.UserRespDTO;
import com.vv.cloudfarming.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


/**
 * 商品服务实现层
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductDO> implements ProductService {

    private final ShopService shopService;
    private final UserService userService;

    @Override
    public void createProduct(ProductCreateReqDTO requestParam) {
        long loginId = StpUtil.getLoginIdAsLong();
        ShopInfoRespDTO myShopInfo = shopService.getMyShopInfo();

        ProductDO product = BeanUtil.toBean(requestParam, ProductDO.class);
        product.setCreatorId(loginId);
        product.setShopId(myShopInfo.getId());
        product.setStatus(2);
        int inserted = baseMapper.insert(product);
        if (inserted < 0) {
            throw new ServiceException("商品创建失败");
        }
    }

    @Override
    public ProductRespDTO getProductById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        ProductDO productDO = baseMapper.selectById(id);
        ProductRespDTO productRespDTO = BeanUtil.toBean(productDO, ProductRespDTO.class);
        return productRespDTO;
    }

    @Override
    public boolean updateProduct(ProductUpdateReqDTO requestParam) {
        Long id = requestParam.getId();
        String productName = requestParam.getProductName();
        String productCategory = requestParam.getProductCategory();
        String originPlace = requestParam.getOriginPlace();
        String specification = requestParam.getSpecification();
        BigDecimal price = requestParam.getPrice();
        Integer stock = requestParam.getStock();
        String description = requestParam.getDescription();
        String productImg = requestParam.getProductImg();

        LambdaUpdateWrapper<ProductDO> wrapper = Wrappers.lambdaUpdate(ProductDO.class)
                .eq(ProductDO::getId, id)
                .set(StrUtil.isNotBlank(productName), ProductDO::getProductName, productName)
                .set(StrUtil.isNotBlank(productCategory), ProductDO::getProductCategory, productCategory)
                .set(StrUtil.isNotBlank(originPlace), ProductDO::getOriginPlace, originPlace)
                .set(StrUtil.isNotBlank(specification), ProductDO::getSpecification, specification)
                .set(ObjectUtil.isNotNull(price), ProductDO::getPrice, price)
                .set(ObjectUtil.isNotNull(stock), ProductDO::getStock, stock)
                .set(StrUtil.isNotBlank(description), ProductDO::getDescription, description)
                .set(StrUtil.isNotBlank(productImg), ProductDO::getProductImg, productImg);
        int updated = baseMapper.update(wrapper);
        if (updated < 0) {
            throw new ServiceException("更新商品信息失败");
        }
        return true;
    }

    @Override
    public boolean deleteProduct(Long id) {
        ProductDO productDO = baseMapper.selectById(id);
        long loginId = StpUtil.getLoginIdAsLong();
        if (!productDO.getCreatorId().equals(loginId)) {
            throw new ClientException("无权删除该商品！");
        }
        int deleted = baseMapper.deleteById(id);
        if (deleted < 0){
            throw new ServiceException("删除商品失败");
        }
        return true;
    }

    @Override
    public IPage<ProductRespDTO> PageProductByShopId(ProductPageQueryReqDTO requestParam) {
        Long id = requestParam.getId();
        Long shopId = requestParam.getShopId();
        String productName = requestParam.getProductName();
        String productCategory = requestParam.getProductCategory();
        String originPlace = requestParam.getOriginPlace();
        String specification = requestParam.getSpecification();
        BigDecimal price = requestParam.getPrice();
        Integer stock = requestParam.getStock();
        String description = requestParam.getDescription();

        LambdaQueryWrapper<ProductDO> wrapper = Wrappers.lambdaQuery(ProductDO.class)
                .eq(ObjectUtil.isNotNull(id), ProductDO::getId, id)
                .eq(ObjectUtil.isNotNull(shopId), ProductDO::getShopId, shopId)
                .like(StrUtil.isNotBlank(productName), ProductDO::getProductName, productName)
                .eq(StrUtil.isNotBlank(productCategory), ProductDO::getProductCategory, productCategory)
                .eq(StrUtil.isNotBlank(originPlace), ProductDO::getOriginPlace, originPlace)
                .eq(StrUtil.isNotBlank(specification), ProductDO::getSpecification, specification)
                .eq(ObjectUtil.isNotNull(price), ProductDO::getPrice, price)
                .gt(ObjectUtil.isNotNull(stock), ProductDO::getStock, stock)
                .like(StrUtil.isNotBlank(description), ProductDO::getDescription, description);
        IPage<ProductDO> page = baseMapper.selectPage(requestParam, wrapper);
        return page.convert(each -> {
            ProductRespDTO productRespDTO = BeanUtil.toBean(each, ProductRespDTO.class);
            UserRespDTO user = userService.getUserById(each.getCreatorId());
            productRespDTO.setCreateUser(user);
            return productRespDTO;
        });
    }

    @Override
    public void updateShelfStatus(ProductUpdateShelfStatusRequestDTO requestParam) {
        Long productId = requestParam.getId();
        Long shopId = requestParam.getShopId();
        Boolean onShelf = requestParam.getOnShelf();
        // 参数校验
        if (productId == null || productId < 0 || shopId == null || shopId < 0 || onShelf == null) {
            throw new ClientException("商品ID、店铺ID和操作类型不能为空");
        }
        // 查询商品是否存在，并校验是否属于该店铺
        ProductDO product = baseMapper.selectById(productId);
        if (product == null) {
            throw new ClientException("商品不存在");
        }
        if (!product.getShopId().equals(shopId)) {
            throw new RuntimeException("无权操作该商品");
        }
        // 设置状态：上架=1，下架=0
        int targetStatus = onShelf ? 1 : 0;
        // 如果已经是目标状态，无需更新
        if (product.getStatus() != null && product.getStatus() == targetStatus) {
            return;
        } else if (2 == product.getStatus()){
            throw new ClientException("请等待管理员审核");
        }
        // 更新状态
        ProductDO update = new ProductDO();
        update.setId(productId);
        update.setStatus(targetStatus);
        boolean updated = this.updateById(update);
        if (!updated) {
            throw new RuntimeException("商品上下架失败");
        }
    }
}
