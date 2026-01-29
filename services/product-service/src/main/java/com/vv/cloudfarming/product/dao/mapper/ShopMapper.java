package com.vv.cloudfarming.product.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.cloudfarming.product.dao.entity.Shop;
import org.apache.ibatis.annotations.Select;

/**
 * 店铺持久层
 */
public interface ShopMapper extends BaseMapper<Shop> {

    @Select("SELECT id FROM t_shops WHERE farmer_id = #{farmerId} AND del_flag = 0")
    Long getIdByFarmerId(Long farmerId);
}
