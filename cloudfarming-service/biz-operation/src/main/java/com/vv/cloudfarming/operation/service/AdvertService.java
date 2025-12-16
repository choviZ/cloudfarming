package com.vv.cloudfarming.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.operation.dto.req.AdvertCreateReqDTO;
import com.vv.cloudfarming.operation.dto.req.AdvertPageQueryReqDTO;
import com.vv.cloudfarming.operation.dto.req.AdvertUpdateReqDTO;
import com.vv.cloudfarming.operation.dao.entity.Advert;
import com.vv.cloudfarming.operation.dto.resp.AdvertRespDTO;

import java.util.List;

/**
 * 广告服务接口层
 */
public interface AdvertService extends IService<Advert> {

    /**
     * 创建广告
     *
     * @param requestParam 请求参数
     */
    void createAdvert(AdvertCreateReqDTO requestParam);

    /**
     * 根据id查询广告详情
     *
     * @param id 广告id
     * @return 广告详情
     */
    AdvertRespDTO getAdvertById(Integer id);

    /**
     * 修改广告信息
     *
     * @param requestParam 请求参数
     * @return 是否成功
     */
    boolean updateAdvert(AdvertUpdateReqDTO requestParam);

    /**
     * 删除广告
     *
     * @param id 广告id
     * @return 是否成功
     */
    boolean deleteAdvert(Integer id);

    /**
     * 分页查询广告
     *
     * @param requestParam 分页查询参数
     * @return 广告列表
     */
    IPage<AdvertRespDTO> pageAdvert(AdvertPageQueryReqDTO requestParam);

    /**
     * 获取展示的广告
     * @return 广告列表
     */
    List<AdvertRespDTO> getShowAdverts();
}
