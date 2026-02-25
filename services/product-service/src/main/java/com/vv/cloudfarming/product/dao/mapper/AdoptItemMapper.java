package com.vv.cloudfarming.product.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.cloudfarming.product.dao.entity.AdoptItemDO;
import org.apache.ibatis.annotations.Param;

/**
 * 认养项目Mapper接口
 */
public interface AdoptItemMapper extends BaseMapper<AdoptItemDO> {

    int lockStock(@Param("quantity") Integer quantity, @Param("id") Long id);

    int unlockStock(@Param("quantity") Integer quantity, @Param("id") Long id);

    int deductStock(@Param("quantity") Integer quantity, @Param("id") Long id);
}
