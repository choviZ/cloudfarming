package com.vv.cloudfarming.product.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 全选或取消全选请求
 */
@Data
public class CartSelectAllReqDTO implements Serializable {

    @NotNull(message = "是否选中不能为空")
    private Boolean selected;
}
