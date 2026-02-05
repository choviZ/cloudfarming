package com.vv.cloudfarming.product.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建养殖日志请求参数
 */
@Data
public class AdoptLogCreateReqDTO {

    /**
     * 认养实例id
     */
    @NotNull
    private Long instanceId;

    /**
     * 日志类型: 1-喂食 2-体重记录 3-防疫 4-日常记录 5-异常
     */
    @NotNull
    private Integer logType;

    /**
     * 文字描述
     */
    private String content;

    /**
     * 图片地址，多个用逗号分隔
     */
    private String imageUrls;

    /**
     * 视频地址
     */
    private String videoUrl;

    /**
     * 本次记录体重（kg）
     */
    private Double weight;
}
