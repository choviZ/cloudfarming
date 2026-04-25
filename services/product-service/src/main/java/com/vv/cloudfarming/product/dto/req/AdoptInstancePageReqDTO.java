package com.vv.cloudfarming.product.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.product.dao.entity.AdoptInstanceDO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 养殖实例查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdoptInstancePageReqDTO extends Page<AdoptInstanceDO> {

    @Min(value = 0, message = "状态不能小于0")
    @Max(value = 3, message = "状态不能大于3")
    private Integer status;

    private Long itemId;

    private Long ownerOrderId;

    @Pattern(regexp = "^(USER|FARMER)$", message = "查询视角只支持 USER 或 FARMER")
    private String viewType;
}
