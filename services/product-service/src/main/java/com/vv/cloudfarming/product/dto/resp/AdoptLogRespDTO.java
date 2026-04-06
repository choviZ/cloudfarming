package com.vv.cloudfarming.product.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 养殖日志响应
 */
@Data
@Builder
public class AdoptLogRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 养殖实例ID
     */
    private Long instanceId;

    /**
     * 日志类型
     */
    private Integer logType;

    /**
     * 日志类型描述
     */
    private String logTypeDesc;

    /**
     * 文字描述
     */
    private String content;

    /**
     * 图片列表
     */
    private List<String> imageUrls;

    /**
     * 视频地址
     */
    private String videoUrl;

    /**
     * 体重（kg）
     */
    private Double weight;

    /**
     * 创建时间
     */
    private Date createTime;
}
