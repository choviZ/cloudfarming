package com.vv.cloudfarming.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.vv.cloudfarming.user.dao.entity.AdvertDO;
import com.vv.cloudfarming.user.dao.mapper.AdvertMapper;
import com.vv.cloudfarming.user.dto.req.AdvertCreateReqDTO;
import com.vv.cloudfarming.user.dto.req.AdvertPageQueryReqDTO;
import com.vv.cloudfarming.user.dto.req.AdvertUpdateReqDTO;
import com.vv.cloudfarming.user.dto.resp.AdvertRespDTO;
import com.vv.cloudfarming.user.service.AdvertService;
import org.springframework.stereotype.Service;

/**
 * 广告服务实现层
 */
@Service
public class AdvertServiceImpl extends ServiceImpl<AdvertMapper, AdvertDO> implements AdvertService {

    @Override
    public void createAdvert(AdvertCreateReqDTO requestParam) {
        AdvertDO advert = BeanUtil.toBean(requestParam, AdvertDO.class);
        int inserted = baseMapper.insert(advert);
        if (inserted < 0) {
            throw new ServiceException("广告创建失败");
        }
    }

    @Override
    public AdvertRespDTO getAdvertById(Integer id) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        AdvertDO advert = baseMapper.selectById(id);
        if (advert == null) {
            throw new ClientException("广告不存在");
        }
        return BeanUtil.toBean(advert, AdvertRespDTO.class);
    }

    @Override
    public boolean updateAdvert(AdvertUpdateReqDTO requestParam) {
        Integer id = requestParam.getId();
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }

        String imageUrl = requestParam.getImageUrl();
        String linkUrl = requestParam.getLinkUrl();
        String altText = requestParam.getAltText();
        Integer displayOrder = requestParam.getDisplayOrder();
        Boolean isActive = requestParam.getIsActive();
        LocalDateTime startDate = requestParam.getStartDate();
        LocalDateTime endDate = requestParam.getEndDate();

        LambdaUpdateWrapper<AdvertDO> wrapper = Wrappers.lambdaUpdate(AdvertDO.class)
                .eq(AdvertDO::getId, id)
                .set(StrUtil.isNotBlank(imageUrl), AdvertDO::getImageUrl, imageUrl)
                .set(StrUtil.isNotBlank(linkUrl), AdvertDO::getLinkUrl, linkUrl)
                .set(StrUtil.isNotBlank(altText), AdvertDO::getAltText, altText)
                .set(ObjectUtil.isNotNull(displayOrder), AdvertDO::getDisplayOrder, displayOrder)
                .set(ObjectUtil.isNotNull(isActive), AdvertDO::getIsActive, isActive)
                .set(ObjectUtil.isNotNull(startDate), AdvertDO::getStartDate, startDate)
                .set(ObjectUtil.isNotNull(endDate), AdvertDO::getEndDate, endDate);
        int updated = baseMapper.update(wrapper);
        if (updated < 0) {
            throw new ServiceException("广告更新失败");
        }
        return true;
    }

    @Override
    public boolean deleteAdvert(Integer id) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        // 检查广告是否存在
        AdvertDO advert = baseMapper.selectById(id);
        if (advert == null) {
            throw new ClientException("广告不存在");
        }
        int deleted = baseMapper.deleteById(id);
        if (deleted < 0) {
            throw new ServiceException("广告删除失败");
        }
        return true;
    }

    @Override
    public IPage<AdvertRespDTO> pageAdvert(AdvertPageQueryReqDTO requestParam) {
        Integer id = requestParam.getId();
        String imageUrl = requestParam.getImageUrl();
        String linkUrl = requestParam.getLinkUrl();
        Boolean isActive = requestParam.getIsActive();
        LocalDateTime startDate = requestParam.getStartDate();
        LocalDateTime endDate = requestParam.getEndDate();

        LambdaQueryWrapper<AdvertDO> wrapper = Wrappers.lambdaQuery(AdvertDO.class)
                .eq(ObjectUtil.isNotNull(id), AdvertDO::getId, id)
                .like(StrUtil.isNotBlank(imageUrl), AdvertDO::getImageUrl, imageUrl)
                .like(StrUtil.isNotBlank(linkUrl), AdvertDO::getLinkUrl, linkUrl)
                .eq(ObjectUtil.isNotNull(isActive), AdvertDO::getIsActive, isActive)
                .eq(ObjectUtil.isNotNull(startDate), AdvertDO::getStartDate, startDate)
                .eq(ObjectUtil.isNotNull(endDate), AdvertDO::getEndDate, endDate)
                .orderByDesc(AdvertDO::getCreateTime);

        IPage<AdvertDO> page = baseMapper.selectPage(requestParam, wrapper);
        return page.convert(each -> BeanUtil.toBean(each, AdvertRespDTO.class));
    }

    @Override
    public List<AdvertRespDTO> getShowAdverts() {
        LambdaQueryWrapper<AdvertDO> wrapper = Wrappers.lambdaQuery(AdvertDO.class)
                .eq(AdvertDO::getIsActive, true)
                .lt(AdvertDO::getStartDate, LocalDateTime.now())
                .gt(AdvertDO::getEndDate, LocalDateTime.now())
                .orderByAsc(AdvertDO::getDisplayOrder);

        List<AdvertDO> adverts = baseMapper.selectList(wrapper);

        return adverts.stream()
                .map(advert -> BeanUtil.toBean(advert, AdvertRespDTO.class))
                .collect(Collectors.toList());
    }
}
