package com.vv.cloudfarming.product.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.cloudfarming.product.dao.entity.SeckillActivityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivityDO> {

    int lockSkuStock(@Param("quantity") Integer quantity, @Param("id") Long id);

    int unlockSkuStock(@Param("quantity") Integer quantity, @Param("id") Long id);

    int deductSkuStock(@Param("quantity") Integer quantity, @Param("id") Long id);
}
