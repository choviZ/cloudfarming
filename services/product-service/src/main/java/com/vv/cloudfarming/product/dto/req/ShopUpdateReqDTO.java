package com.vv.cloudfarming.product.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShopUpdateReqDTO {

    /**
     * 主键ID
     */
    @NotNull(message = "店铺ID不能为空")
    private Long id;

    /**
     * 店铺名称
     */
    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 50, message = "店铺名称长度不能超过50个字符")
    private String shopName;

    /**
     * 店铺头像
     */
    private String shopAvatar;

    /**
     * 店铺横幅
     */
    private String shopBanner;

    /**
     * 店铺描述
     */
    @Size(max = 500, message = "店铺描述长度不能超过500个字符")
    private String description;

    /**
     * 店铺公告
     */
    @Size(max = 200, message = "店铺公告长度不能超过200个字符")
    private String announcement;
}
