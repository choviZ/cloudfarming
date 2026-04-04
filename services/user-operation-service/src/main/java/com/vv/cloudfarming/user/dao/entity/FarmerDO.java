package com.vv.cloudfarming.user.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.Data;

import java.io.Serializable;

/**
 * Farmer entity.
 */
@TableName("t_farmer")
@Data
public class FarmerDO extends BaseDO implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String realName;

    private String idCard;

    private String houseAddress;

    private String farmName;

    private String farmAddress;

    private Double farmArea;

    private String breedingType;

    private Long businessLicenseNo;

    private String businessLicensePic;

    /**
     * Comma-separated image urls.
     */
    private String environmentImages;

    /**
     * 0-not featured, 1-featured.
     */
    private Integer featuredFlag;

    private String remark;

    private Integer reviewStatus;

    private Long reviewUserId;

    private String reviewRemark;
}
