package com.vv.cloudfarming.order.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dto.common.OrderIdAndTypeDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderMapper extends BaseMapper<OrderDO> {

    /**
     * 根据支付单号获取订单号列表
     */
    @Select("SELECT `id`,`order_type` FROM t_order where `pay_order_no` = #{payNo} AND del_flag = 0")
    List<OrderIdAndTypeDTO> getOrderIdByPayNo(String payNo);

    int updatePayNoByOrderNo(@Param("payOrderNo") String payOrderNo,
                             @Param("userId") Long userId,
                             @Param("orderNos") List<String> orderNos);
}
