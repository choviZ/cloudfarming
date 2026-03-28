package com.vv.cloudfarming.user.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 反馈类型选项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackTypeRespDTO implements Serializable {

    /**
     * 类型编码
     */
    private String code;

    /**
     * 类型名称
     */
    private String name;
}
