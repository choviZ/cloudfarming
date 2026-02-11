package com.vv.cloudfarming.product.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.cloudfarming.starter.database.base.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 审核记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("t_audit_record")
public class AuditRecordDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务类型：0-云养殖项目，1-农产品
     */
    private Integer bizType;

    /**
     * 业务ID（商品ID）
     */
    private Long bizId;

    /**
     * 审核状态：0-待审核，1-通过，2-拒绝
     */
    private Integer auditStatus;

    /**
     * 提交人ID
     */
    private Long submitterId;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 拒绝原因
     */
    private String rejectReason;
}
