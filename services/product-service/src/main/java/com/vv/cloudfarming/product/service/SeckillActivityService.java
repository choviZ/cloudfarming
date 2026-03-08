package com.vv.cloudfarming.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.product.dao.entity.SeckillActivityDO;
import com.vv.cloudfarming.product.dto.req.SeckillActivityCreateReqDTO;

/**
 * 秒杀活动服务接口
 */
public interface SeckillActivityService extends IService<SeckillActivityDO> {

    /**
     * 创建秒杀活动并预热缓存
     *
     * @param requestParam 请求参数
     * @return 秒杀活动 ID
     */
    Long createSeckillActivity(SeckillActivityCreateReqDTO requestParam);
}
