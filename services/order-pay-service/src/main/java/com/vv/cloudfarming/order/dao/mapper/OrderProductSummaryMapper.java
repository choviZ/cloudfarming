package com.vv.cloudfarming.order.dao.mapper;

import com.vv.cloudfarming.order.dto.common.OrderProductSummaryRecordDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderProductSummaryMapper {

    List<OrderProductSummaryRecordDTO> selectAdoptByOrderNos(@Param("orderNos") List<String> orderNos);

    List<OrderProductSummaryRecordDTO> selectSkuByOrderNos(@Param("orderNos") List<String> orderNos);
}
