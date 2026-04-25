package com.vv.cloudfarming.product.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.cloudfarming.product.dao.entity.AdoptLogDO;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 养殖日志分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdoptLogPageReqDTO extends Page<AdoptLogDO> {

    /**
     * 养殖实例ID
     */
    private Long instanceId;

    /**
     * 日志类型
     */
    private Integer logType;

    @Pattern(regexp = "^(USER|FARMER)$", message = "查询视角只支持 USER 或 FARMER")
    private String viewType;
}
