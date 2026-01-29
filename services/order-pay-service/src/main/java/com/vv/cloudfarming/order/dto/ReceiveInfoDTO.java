package com.vv.cloudfarming.order.dto;

import lombok.Data;

/**
 * 收货信息
 */
@Data
public class ReceiveInfoDTO {

    /**
     * 收件人姓名
     */
    private String receiveName;

    /**
     * 收件人手机号
     */
    private String receivePhone;

    /**
     * 省份
     */
    private String receiveProvince;

    /**
     * 城市
     */
    private String receiveCity;

    /**
     * 区县
     */
    private String receiveDistrict;

    /**
     * 详细收货地址
     */
    private String receiveDetail;
}
