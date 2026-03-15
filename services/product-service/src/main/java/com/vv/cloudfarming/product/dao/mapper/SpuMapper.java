package com.vv.cloudfarming.product.dao.mapper;

import com.vv.cloudfarming.product.dao.entity.SpuDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * SPU表 Mapper 接口
 */
@Mapper
public interface SpuMapper extends BaseMapper<SpuDO> {

    @Select("""
            SELECT
                a.name,
                s.attr_value
            FROM t_attribute a
            INNER JOIN t_spu_attr_value s ON a.id = s.attr_id
            WHERE s.spu_id = #{spuId};
        """)
    Map<String, String> querySpuAttr(@Param("spuId") Long spuId);
}
