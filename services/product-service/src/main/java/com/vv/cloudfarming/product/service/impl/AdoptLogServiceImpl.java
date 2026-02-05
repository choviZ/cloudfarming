package com.vv.cloudfarming.product.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.product.dao.entity.AdoptInstanceDO;
import com.vv.cloudfarming.product.dao.entity.AdoptLogDO;
import com.vv.cloudfarming.product.dao.mapper.AdoptInstanceMapper;
import com.vv.cloudfarming.product.dao.mapper.AdoptLogMapper;
import com.vv.cloudfarming.product.dto.req.AdoptLogCreateReqDTO;
import com.vv.cloudfarming.product.enums.AdoptLogTypeEnum;
import com.vv.cloudfarming.product.service.AdoptLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdoptLogServiceImpl extends ServiceImpl<AdoptLogMapper, AdoptLogDO> implements AdoptLogService {

    private final AdoptInstanceMapper adoptInstanceMapper;

    @Override
    public void createAdoptLog(AdoptLogCreateReqDTO requestParam) {
        Long instanceId = requestParam.getInstanceId();
        Integer logType = requestParam.getLogType();
        AdoptLogTypeEnum typeEnum = AdoptLogTypeEnum.getByCode(logType);
        if (typeEnum == null){
            throw new ClientException("不存在的认养订单状态码：" + logType);
        }
        boolean exists = adoptInstanceMapper.exists(
                Wrappers.lambdaQuery(AdoptInstanceDO.class)
                        .eq(AdoptInstanceDO::getId, instanceId)
        );
        if (!exists) {
            throw new ClientException("认养订单不存在");
        }
        long userId = StpUtil.getLoginIdAsLong();
        AdoptLogDO adoptLogDO = BeanUtil.toBean(requestParam, AdoptLogDO.class);
        adoptLogDO.setOperatorId(userId);
        int result = baseMapper.insert(adoptLogDO);
        if (result < 1) {
            throw new RuntimeException("创建养殖日志失败");
        }
    }
}
