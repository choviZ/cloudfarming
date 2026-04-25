package com.vv.cloudfarming.order.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLogisticsRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 物流单号
     */
    private String logisticsNo;

    /**
     * 系统内填写的物流公司
     */
    private String logisticsCompany;

    /**
     * 外层查询状态码
     */
    private String queryStatus;

    /**
     * 外层查询消息
     */
    private String queryMessage;

    /**
     * 快递公司编码
     */
    private String companyCode;

    /**
     * 快递公司名称
     */
    private String companyName;

    /**
     * 快递公司电话
     */
    private String companyPhone;

    /**
     * 快递公司官网
     */
    private String companyWebsite;

    /**
     * 快递员或网点
     */
    private String courierName;

    /**
     * 快递员电话
     */
    private String courierPhone;

    /**
     * 快递公司 LOGO
     */
    private String logo;

    /**
     * 最新轨迹时间
     */
    private String latestTrackTime;

    /**
     * 发货到收货耗时
     */
    private String takeTime;

    /**
     * 投递状态码
     */
    private Integer deliveryStatus;

    /**
     * 投递状态文案
     */
    private String deliveryStatusText;

    /**
     * 是否签收
     */
    private Boolean signed;

    /**
     * 物流轨迹
     */
    private List<OrderLogisticsTraceRespDTO> traces;
}
