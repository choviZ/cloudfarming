package com.vv.cloudfarming.shop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.shop.dao.entity.LivestockDO;
import com.vv.cloudfarming.shop.dao.mapper.LiveStockMapper;
import com.vv.cloudfarming.shop.service.LiveStockService;
import org.springframework.stereotype.Service;

@Service
public class LiveStockServiceImpl extends ServiceImpl<LiveStockMapper, LivestockDO> implements LiveStockService {
}
