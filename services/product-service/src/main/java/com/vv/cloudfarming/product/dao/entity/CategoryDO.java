package com.vv.cloudfarming.product.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.Data;

/**
 * 商品分类表
 */
@TableName(value ="t_category")
@Data
public class CategoryDO extends BaseDO implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父级分类ID，顶级分类为NULL
     */
    private Long parentId;

    /**
     * 排序权重
     */
    private Integer sortOrder;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}