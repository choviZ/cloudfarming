package com.vv.cloudfarming.product.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.cloudfarming.product.dao.entity.AuditRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核记录 Mapper 接口
 */
@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecordDO> {
}
