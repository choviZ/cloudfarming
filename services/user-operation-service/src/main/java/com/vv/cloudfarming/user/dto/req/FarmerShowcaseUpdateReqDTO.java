package com.vv.cloudfarming.user.dto.req;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Farmer showcase update request.
 */
@Data
public class FarmerShowcaseUpdateReqDTO {

    @Size(max = 6, message = "环境图片最多上传 6 张")
    private List<String> environmentImages;
}
