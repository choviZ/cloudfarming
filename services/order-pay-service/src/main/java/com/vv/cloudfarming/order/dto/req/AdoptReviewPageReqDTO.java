package com.vv.cloudfarming.order.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.order.dao.entity.OrderSkuReviewDO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdoptReviewPageReqDTO extends Page<OrderSkuReviewDO> {

    @NotNull(message = "adoptItemId 不能为 null")
    private Long adoptItemId;
}
