package com.vv.cloudfarming.order.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.order.dao.entity.OrderSkuReviewDO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SpuReviewPageReqDTO extends Page<OrderSkuReviewDO> {

    @NotNull(message = "商品ID不能为空")
    private Long spuId;
}
