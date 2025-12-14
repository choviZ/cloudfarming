package com.vv.cloudfarming.user.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.cloudfarming.common.database.BaseDO;
import lombok.Data;

import java.io.Serializable;

/**
 * 农户表实体
 */
@TableName("t_farmer")
@Data
public class FarmerDO extends BaseDO implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 姓名
     */
    private String realName;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 户籍地
     */
    private String houseAddress;

    /**
     * 养殖场名称
     */
    private String farmName;

    /**
     * 养殖场地址
     */
    private String farmAddress;

    /**
     * 养殖场面积
     */
    private Double farmArea;

    /**
     * 养殖品类
     */
    private String breedingType;

    /**
     * 营业执照编号
     */
    private Long businessLicenseNo;

    /**
     * 营业执照图片
     */
    private String businessLicensePic;

    /**
     * 申请审核备注
     */
    private String remark;

    /**
     * 审核状态 0-待审核 1-已通过 2-未通过
     */
    private Integer reviewStatus;

    /**
     *  审核人id
     */
    private Long reviewUserId;

    /**
     * 审核员审核的备注
     */
    private String reviewRemark;
}
