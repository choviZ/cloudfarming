package com.vv.cloudfarming.shop.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.vv.cloudfarming.common.database.BaseDO;
import lombok.Data;

import java.io.Serializable;

/**
 * 属性表 DO
 */
@Data
@TableName("t_attribute")
public class AttributeDO extends BaseDO implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的分类ID
     */
    private Long categoryId;

    /**
     * 属性名
     */
    private String name;

    /**
     * 属性类型：0-基本 / 1-销售
     */
    private Integer attrType;

    /**
     * 排序权重
     */
    private Integer sort = 0;
}