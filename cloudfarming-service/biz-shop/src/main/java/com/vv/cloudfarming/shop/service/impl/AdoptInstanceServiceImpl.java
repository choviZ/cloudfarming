package com.vv.cloudfarming.shop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.shop.dao.entity.AdoptInstanceDO;
import com.vv.cloudfarming.shop.dao.mapper.AdoptInstanceMapper;
import com.vv.cloudfarming.shop.service.AdoptInstanceService;
import org.springframework.stereotype.Service;

@Service
public class AdoptInstanceServiceImpl extends ServiceImpl<AdoptInstanceMapper, AdoptInstanceDO> implements AdoptInstanceService {
}
