package com.vv.cloudfarming.order.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.cloudfarming.order.dao.entity.PayDO;
import org.apache.ibatis.annotations.Select;

/**
 * 支付单持久层
 */
public interface PayOrderMapper extends BaseMapper<PayDO> {

    @Select("SELECT * FROM t_pay WHERE pay_order_no = #{payOrderNo} AND del_flag = 0")
    PayDO selectPayOrderByNo(String payOrderNo);
}
