package com.vv.cloudfarming.product.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 体重趋势点响应
 */
@Data
@Builder
public class AdoptWeightPointRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录时间
     */
    private Date recordTime;

    /**
     * 体重（kg）
     */
    private Double weight;
}
