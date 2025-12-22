package com.vv.cloudfarming.user.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收货地址响应对象
 */
@Data
public class ReceiveAddressRespDTO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人手机号
     */
    private String receiverPhone;

    /**
     * 省份名称
     */
    private String provinceName;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 区县名称
     */
    private String districtName;

    /**
     * 乡镇/街道名称
     */
    private String townName;

    /**
     * 详细地址（如XX小区XX栋XX单元XX室）
     */
    private String detailAddress;

    /**
     * 是否默认地址：0=否，1=是
     */
    private Integer isDefault;

    /**
     * 地址备注（如"公司地址""家里地址"）
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}