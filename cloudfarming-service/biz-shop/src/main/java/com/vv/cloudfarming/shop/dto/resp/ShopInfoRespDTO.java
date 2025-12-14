package com.vv.cloudfarming.shop.dto.resp;


import com.vv.cloudfarming.common.database.BaseDO;
import lombok.Data;

import java.io.Serializable;

/**
 * 店铺信息
 */
@Data
public class ShopInfoRespDTO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 农户（用户）ID
     */
    private Long farmerId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺头像
     */
    private String shopAvatar;

    /**
     * 店铺横幅
     */
    private String shopBanner;

    /**
     * 店铺描述
     */
    private String description;

    /**
     * 店铺公告
     */
    private String announcement;

    /**
     * 状态: 1-正常, 2-禁用
     */
    private Integer status;
}
