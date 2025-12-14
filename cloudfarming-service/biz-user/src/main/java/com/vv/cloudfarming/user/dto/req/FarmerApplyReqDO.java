package com.vv.cloudfarming.user.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 申请农户参数对象
 */
@Data
public class FarmerApplyReqDO implements Serializable {

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
}
