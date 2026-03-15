package com.vv.cloudfarming.product.dao.mapper;

import com.vv.cloudfarming.product.dao.entity.SkuDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * SKU表 Mapper 接口
 */
@Mapper
public interface SkuMapper extends BaseMapper<SkuDO> {

    @Select("select min(price) from t_sku where spu_id = #{spuId} and del_flag = 0")
    double queryLowestPrice(@Param("spuId") Long spuId);

    int lockSkuStock(@Param("quantity") Integer quantity, @Param("id") Long id);

    int unlockSkuStock(@Param("quantity") Integer quantity, @Param("id") Long id);

    int deductSkuStock(@Param("quantity") Integer quantity, @Param("id") Long id);
}
