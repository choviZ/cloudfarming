package com.vv.cloudfarming.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 添加收货地址参数对象
 */
@Data
public class ReceiveAddressAddReqDTO implements Serializable {

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    /**
     * 收货人手机号
     */
    @NotBlank(message = "收货人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String receiverPhone;

    /**
     * 省份名称
     */
    @NotBlank(message = "省份名称不能为空")
    private String provinceName;

    /**
     * 城市名称
     */
    @NotBlank(message = "城市名称不能为空")
    private String cityName;

    /**
     * 区县名称
     */
    @NotBlank(message = "区县名称不能为空")
    private String districtName;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;

    /**
     * 是否默认地址：0=否，1=是
     */
    @NotNull(message = "是否默认地址不能为空")
    private Integer isDefault;

    /**
     * 地址备注
     */
    private String remark;
}