package com.vv.cloudfarming.product.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新购物车选中状态请求
 */
@Data
public class CartItemSelectedReqDTO implements Serializable {

    @NotNull(message = "是否选中不能为空")
    private Boolean selected;
}
