package com.vv.cloudfarming.operation.service.impl;

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
import com.vv.cloudfarming.operation.dto.req.AdvertCreateReqDTO;
import com.vv.cloudfarming.operation.dto.req.AdvertPageQueryReqDTO;
import com.vv.cloudfarming.operation.dto.req.AdvertUpdateReqDTO;
import com.vv.cloudfarming.operation.dao.entity.Advert;
import com.vv.cloudfarming.operation.dao.mapper.AdvertMapper;
import com.vv.cloudfarming.operation.dto.resp.AdvertRespDTO;
import com.vv.cloudfarming.operation.service.AdvertService;
import org.springframework.stereotype.Service;

/**
 * 广告服务实现层
 */
@Service
public class AdvertServiceImpl extends ServiceImpl<AdvertMapper, Advert> implements AdvertService {

    @Override
    public void createAdvert(AdvertCreateReqDTO requestParam) {
        Advert advert = BeanUtil.toBean(requestParam, Advert.class);
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
        Advert advert = baseMapper.selectById(id);
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

        LambdaUpdateWrapper<Advert> wrapper = Wrappers.lambdaUpdate(Advert.class)
                .eq(Advert::getId, id)
                .set(StrUtil.isNotBlank(imageUrl), Advert::getImageUrl, imageUrl)
                .set(StrUtil.isNotBlank(linkUrl), Advert::getLinkUrl, linkUrl)
                .set(StrUtil.isNotBlank(altText), Advert::getAltText, altText)
                .set(ObjectUtil.isNotNull(displayOrder), Advert::getDisplayOrder, displayOrder)
                .set(ObjectUtil.isNotNull(isActive), Advert::getIsActive, isActive)
                .set(ObjectUtil.isNotNull(startDate), Advert::getStartDate, startDate)
                .set(ObjectUtil.isNotNull(endDate), Advert::getEndDate, endDate);
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
        Advert advert = baseMapper.selectById(id);
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

        LambdaQueryWrapper<Advert> wrapper = Wrappers.lambdaQuery(Advert.class)
                .eq(ObjectUtil.isNotNull(id), Advert::getId, id)
                .like(StrUtil.isNotBlank(imageUrl), Advert::getImageUrl, imageUrl)
                .like(StrUtil.isNotBlank(linkUrl), Advert::getLinkUrl, linkUrl)
                .eq(ObjectUtil.isNotNull(isActive), Advert::getIsActive, isActive)
                .eq(ObjectUtil.isNotNull(startDate), Advert::getStartDate, startDate)
                .eq(ObjectUtil.isNotNull(endDate), Advert::getEndDate, endDate)
                .orderByDesc(Advert::getCreateTime);

        IPage<Advert> page = baseMapper.selectPage(requestParam, wrapper);
        return page.convert(each -> BeanUtil.toBean(each, AdvertRespDTO.class));
    }

    @Override
    public List<AdvertRespDTO> getShowAdverts() {
        LambdaQueryWrapper<Advert> wrapper = Wrappers.lambdaQuery(Advert.class)
                .eq(Advert::getIsActive, true)
                .lt(Advert::getStartDate, LocalDateTime.now())
                .gt(Advert::getEndDate, LocalDateTime.now())
                .orderByAsc(Advert::getDisplayOrder);

        List<Advert> adverts = baseMapper.selectList(wrapper);

        return adverts.stream()
                .map(advert -> BeanUtil.toBean(advert, AdvertRespDTO.class))
                .collect(Collectors.toList());
    }
}
