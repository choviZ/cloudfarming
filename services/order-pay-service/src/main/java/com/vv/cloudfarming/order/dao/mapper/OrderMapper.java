package com.vv.cloudfarming.order.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dto.common.FarmerOrderStatisticsAggDTO;
import com.vv.cloudfarming.order.dto.common.FarmerOrderTrendAggDTO;
import com.vv.cloudfarming.order.dto.common.PayOrderOrderRelationDTO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderMapper extends BaseMapper<OrderDO> {

    List<PayOrderOrderRelationDTO> listPayOrderRelations(@Param("payOrderNos") List<String> payOrderNos);

    int updatePayNoByOrderNo(@Param("payOrderNo") String payOrderNo,
                             @Param("userId") Long userId,
                             @Param("orderNos") List<String> orderNos);

    FarmerOrderStatisticsAggDTO selectFarmerOrderStatistics(@Param("shopId") Long shopId,
                                                            @Param("excludedStatuses") List<Integer> excludedStatuses,
                                                            @Param("todayStart") LocalDateTime todayStart,
                                                            @Param("todayEnd") LocalDateTime todayEnd,
                                                            @Param("last7DaysStart") LocalDateTime last7DaysStart,
                                                            @Param("last30DaysStart") LocalDateTime last30DaysStart);

    List<FarmerOrderTrendAggDTO> selectFarmerOrderTrend(@Param("shopId") Long shopId,
                                                        @Param("excludedStatuses") List<Integer> excludedStatuses,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);
}
