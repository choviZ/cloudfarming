package com.vv.cloudfarming.user.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 设置默认地址参数对象
 */
@Data
public class ReceiveAddressSetDefaultReqDTO implements Serializable {

    /**
     * 主键ID
     */
    @NotNull(message = "地址ID不能为空")
    private Long id;
}