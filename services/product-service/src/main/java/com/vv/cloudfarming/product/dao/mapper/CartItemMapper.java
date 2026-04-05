package com.vv.cloudfarming.product.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.cloudfarming.product.dao.entity.CartItemDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 购物车持久层
 */
public interface CartItemMapper extends BaseMapper<CartItemDO> {

    @Select("""
            SELECT id, user_id, sku_id, quantity, selected, create_time, update_time, del_flag
            FROM t_cart_item
            WHERE user_id = #{userId}
              AND sku_id = #{skuId}
            LIMIT 1
            """)
    CartItemDO selectAnyByUserIdAndSkuId(@Param("userId") Long userId, @Param("skuId") Long skuId);

    @Update("""
            UPDATE t_cart_item
            SET quantity = #{quantity},
                selected = #{selected},
                del_flag = 0,
                update_time = NOW()
            WHERE id = #{id}
            """)
    int restoreDeletedById(@Param("id") Long id, @Param("quantity") Integer quantity, @Param("selected") Boolean selected);
}
