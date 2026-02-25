package com.vv.cloudfarming.product.dao.mapper;

import com.vv.cloudfarming.product.dao.entity.SkuDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * SKU表 Mapper 接口
 */
@Mapper
public interface SkuMapper extends BaseMapper<SkuDO> {

    int lockSkuStock(@Param("quantity") Integer quantity, @Param("id") Long id);

    int unlockSkuStock(@Param("quantity") Integer quantity, @Param("id") Long id);
}
