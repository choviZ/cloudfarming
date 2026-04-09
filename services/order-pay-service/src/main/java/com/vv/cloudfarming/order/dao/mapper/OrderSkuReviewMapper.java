package com.vv.cloudfarming.order.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.order.dao.entity.OrderSkuReviewDO;
import com.vv.cloudfarming.order.dto.common.OrderReviewAggDTO;
import com.vv.cloudfarming.order.dto.common.SpuReviewSummaryAggDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderSkuReviewMapper extends BaseMapper<OrderSkuReviewDO> {

    List<OrderReviewAggDTO> selectOrderReviewAggByOrderNos(@Param("orderNos") List<String> orderNos);

    SpuReviewSummaryAggDTO selectSpuReviewSummary(@Param("spuId") Long spuId);

    IPage<OrderSkuReviewDO> selectSpuReviewPage(IPage<OrderSkuReviewDO> page, @Param("spuId") Long spuId);
}
