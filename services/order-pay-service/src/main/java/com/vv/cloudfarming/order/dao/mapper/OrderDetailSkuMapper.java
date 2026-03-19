package com.vv.cloudfarming.order.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.cloudfarming.order.dao.entity.OrderDetailSkuDO;
import com.vv.cloudfarming.order.dto.common.ProductSummaryDTO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderDetailSkuMapper extends BaseMapper<OrderDetailSkuDO> {

    @Select("""
                SELECT
                	sku_name as productName,sku_image as coverImage,price,quantity
                FROM
                	t_order_detail_sku
                where order_no = #{orderNo}
        """)
    List<ProductSummaryDTO> selectProductSummaryByOrderNo(String orderNo);

}
