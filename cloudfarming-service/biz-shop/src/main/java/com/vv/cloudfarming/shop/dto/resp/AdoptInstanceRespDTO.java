package com.vv.cloudfarming.shop.dto.resp;

import com.vv.cloudfarming.common.database.BaseDO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 认养实例响应
 */
@Data
@Builder
public class AdoptInstanceRespDTO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Integer id;

    /**
     * 认养项目id
     */
    private Long itemId;

    /**
     * 耳标号 / 动物的唯一标识
     */
    private Long earTagNo;

    /**
     * 图片
     */
    private String image;

    /**
     * 可认养 / 已认养 / 已履约完成
     */
    private Integer status;

    /**
     * 绑定订单id
     */
    private Long ownerOrderId;
}
