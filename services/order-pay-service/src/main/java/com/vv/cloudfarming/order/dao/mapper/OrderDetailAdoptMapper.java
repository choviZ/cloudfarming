package com.vv.cloudfarming.order.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.cloudfarming.order.dao.entity.OrderDetailAdoptDO;
import com.vv.cloudfarming.order.dto.common.ProductSummaryDTO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderDetailAdoptMapper extends BaseMapper<OrderDetailAdoptDO> {

    @Select("""
                SELECT
                	item_name as productName,item_image as coverImage,price,quantity
                FROM
                	t_order_detail_adopt
                where order_no = #{orderNo}
        """)
    List<ProductSummaryDTO> selectProductSummaryByOrderNo(String orderNo);
}
