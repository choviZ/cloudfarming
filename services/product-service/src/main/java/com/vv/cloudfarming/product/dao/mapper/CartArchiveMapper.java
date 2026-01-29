package com.vv.cloudfarming.product.dao.mapper;

import com.vv.cloudfarming.product.dao.entity.CartArchiveDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 购物车数据归档持久层
 */
public interface CartArchiveMapper {

    @Insert("INSERT INTO t_cart_archive (user_id,cart_items) VALUES (#{userId},#{cartItems})")
    int cartArchive(@Param("userId") Long userId, @Param("cartItems") String cartItems);

    @Select("SELECT * FROM t_cart_archive WHERE user_id = #{userId} ORDER BY archive_time DESC LIMIT 1")
    CartArchiveDO getCartArchive(@Param("userId") Long userId);
}
