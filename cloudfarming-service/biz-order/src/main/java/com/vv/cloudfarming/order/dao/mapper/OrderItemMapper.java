package com.vv.cloudfarming.order.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.cloudfarming.order.dao.entity.OrderItemDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 子订单表持久层
 */
public interface OrderItemMapper extends BaseMapper<OrderItemDO> {

    /**
     * 根据主订单id查询子订单列表
     */
    List<OrderItemDO> selectByMainOrderId(@Param("id") Long id);
}
