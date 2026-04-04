package com.vv.cloudfarming.user.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.user.dao.entity.FarmerDO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Admin farmer page query.
 */
@Data
public class FarmerAdminPageReqDTO extends Page<FarmerDO> {

    private String farmName;

    private String breedingType;

    @Min(value = 0, message = "审核状态错误")
    @Max(value = 2, message = "审核状态错误")
    private Integer reviewStatus;

    @Min(value = 0, message = "精选状态错误")
    @Max(value = 1, message = "精选状态错误")
    private Integer featuredFlag;
}
