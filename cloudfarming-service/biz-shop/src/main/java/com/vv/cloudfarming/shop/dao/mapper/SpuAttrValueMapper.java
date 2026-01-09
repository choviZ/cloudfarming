package com.vv.cloudfarming.shop.dao.mapper;

import com.vv.cloudfarming.shop.dao.entity.SpuAttrValueDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * SPU属性值关联表 Mapper 接口
 */
@Mapper
public interface SpuAttrValueMapper extends BaseMapper<SpuAttrValueDO> {

    /**
     * 校验属性是否存在
     *
     * @param attrId 属性ID
     * @return 存在数量
     */
    @Select("SELECT COUNT(1) FROM t_attribute WHERE id = #{attrId} AND del_flag = 0")
    int checkAttributeExists(@Param("attrId") Long attrId);

    /***
     * 根据spuId查询相关的属性及属性值
     * @param spuId
     * @return
     */
    @Select("SELECT attr_id,attr_value FROM t_spu_attr_value where spu_id = #{spuId}")
    List<SpuAttrValueDO> getAttrValueBySpuId(@Param("spuId") Long spuId);
}
