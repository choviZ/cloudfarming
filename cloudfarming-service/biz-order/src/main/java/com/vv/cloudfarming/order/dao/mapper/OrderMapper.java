package com.vv.cloudfarming.order.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import org.apache.ibatis.annotations.Select;

/**
 * 主订单表持久层
 */
public interface OrderMapper extends BaseMapper<OrderDO> {

    @Select("SELECT pay_status from t_order_main where id = #{id} AND del_flag = 0")
    int queryOrderPayStatusById(Long id);

    @Select("SELECT order_status from t_order_main where id = #{id} AND del_flag = 0")
    int queryOrderStatusById(Long id);
}
