package com.vv.cloudfarming.framework.storage.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 */
@Data
public class UploadFileRequest implements Serializable {

    /**
     * 业务代码
     */
    private String bizCode;

    private static final long serialVersionUID = 1L;
}