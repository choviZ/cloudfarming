package com.vv.cloudfarming.product.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.product.dao.entity.AdoptInstanceDO;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 养殖实例查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdoptInstancePageReqDTO extends Page<AdoptInstanceDO> {

    @Pattern(regexp = "^[012]$", message = "状态只能是0、1、2")
    private Integer status;

    private Long itemId;

    private Long ownerOrderId;
}
