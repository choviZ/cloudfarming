package com.vv.cloudfarming.order.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.order.dao.entity.AdoptOrderDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认养订单分页查询请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdoptOrderPageReqDTO extends Page<AdoptOrderDO> {

    private Integer orderStatus;

    private Integer payStatus;

    private Long adoptItemId;
}
