package com.vv.cloudfarming.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.dao.entity.AttributeDO;
import com.vv.cloudfarming.product.dao.mapper.AttributeMapper;
import com.vv.cloudfarming.product.dto.req.AttributeCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.AttributeUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.AttributeRespDTO;
import com.vv.cloudfarming.product.service.AttributeService;
import com.vv.cloudfarming.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* 针对表【t_attribute(属性表)】的数据库操作Service实现
*/
@Service
@RequiredArgsConstructor
public class AttributeServiceImpl extends ServiceImpl<AttributeMapper, AttributeDO> implements AttributeService {

    private final CategoryService categoryService;

    @Override
    public void createAttribute(AttributeCreateReqDTO requestParam) {
        Long categoryId = requestParam.getCategoryId();
        String name = requestParam.getName();
        Integer attrType = requestParam.getAttrType();
        Integer sort = requestParam.getSort();

        if (categoryId == null || categoryId <= 0) {
            throw new ClientException("分类ID不合法");
        }

        if (StrUtil.isBlank(name)) {
            throw new ClientException("属性名称不能为空");
        }

        if (attrType == null || (attrType != 0 && attrType != 1)) {
            throw new ClientException("属性类型不合法，必须为0（基本属性）或1（销售属性）");
        }

        if (categoryService.getById(categoryId) == null) {
            throw new ClientException("分类不存在");
        }

        AttributeDO attribute = BeanUtil.toBean(requestParam, AttributeDO.class);
        int inserted = baseMapper.insert(attribute);
        if (inserted < 0) {
            throw new ServiceException("属性创建失败");
        }
    }

    @Override
    public boolean updateAttribute(AttributeUpdateReqDTO requestParam) {
        Long id = requestParam.getId();
        Long categoryId = requestParam.getCategoryId();
        String name = requestParam.getName();
        Integer attrType = requestParam.getAttrType();
        Integer sort = requestParam.getSort();

        if (id == null || id <= 0) {
            throw new ClientException("属性ID不合法");
        }

        AttributeDO existingAttribute = baseMapper.selectById(id);
        if (existingAttribute == null) {
            throw new ClientException("属性不存在");
        }

        if (ObjectUtil.isNotNull(categoryId)) {
            if (categoryService.getById(categoryId) == null) {
                throw new ClientException("分类不存在");
            }
        }

        if (ObjectUtil.isNotNull(attrType) && attrType != 0 && attrType != 1) {
            throw new ClientException("属性类型不合法，必须为0（基本属性）或1（销售属性）");
        }

        LambdaUpdateWrapper<AttributeDO> wrapper = Wrappers.lambdaUpdate(AttributeDO.class)
                .eq(AttributeDO::getId, id)
                .set(ObjectUtil.isNotNull(categoryId), AttributeDO::getCategoryId, categoryId)
                .set(StrUtil.isNotBlank(name), AttributeDO::getName, name)
                .set(ObjectUtil.isNotNull(attrType), AttributeDO::getAttrType, attrType)
                .set(ObjectUtil.isNotNull(sort), AttributeDO::getSort, sort);
        int updated = baseMapper.update(wrapper);
        if (updated < 0) {
            throw new ServiceException("更新属性失败");
        }
        return true;
    }

    @Override
    public boolean deleteAttribute(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("属性ID不合法");
        }

        AttributeDO attribute = baseMapper.selectById(id);
        if (attribute == null) {
            throw new ClientException("属性不存在");
        }

        int deleted = baseMapper.deleteById(id);
        if (deleted < 0) {
            throw new ServiceException("删除属性失败");
        }
        return true;
    }

    @Override
    public AttributeRespDTO getAttributeById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("属性ID不合法");
        }
        AttributeDO attributeDO = baseMapper.selectById(id);
        if (attributeDO == null) {
            return null;
        }
        return BeanUtil.toBean(attributeDO, AttributeRespDTO.class);
    }

    @Override
    public List<AttributeRespDTO> getAttributesByCategoryId(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new ClientException("分类ID不合法");
        }

        if (categoryService.getById(categoryId) == null) {
            throw new ClientException("分类不存在");
        }

        LambdaQueryWrapper<AttributeDO> wrapper = Wrappers.lambdaQuery(AttributeDO.class)
                .eq(AttributeDO::getCategoryId, categoryId)
                .orderByAsc(AttributeDO::getSort)
                .orderByAsc(AttributeDO::getId);
        List<AttributeDO> attributes = baseMapper.selectList(wrapper);
        return attributes.stream()
                .map(attributeDO -> BeanUtil.toBean(attributeDO, AttributeRespDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AttributeRespDTO> getAttributesByCategoryIdAndType(Long categoryId, Integer attrType) {
        if (categoryId == null || categoryId <= 0) {
            throw new ClientException("分类ID不合法");
        }

        if (attrType == null || (attrType != 0 && attrType != 1)) {
            throw new ClientException("属性类型不合法，必须为0（基本属性）或1（销售属性）");
        }

        if (categoryService.getById(categoryId) == null) {
            throw new ClientException("分类不存在");
        }

        LambdaQueryWrapper<AttributeDO> wrapper = Wrappers.lambdaQuery(AttributeDO.class)
                .eq(AttributeDO::getCategoryId, categoryId)
                .eq(AttributeDO::getAttrType, attrType)
                .orderByAsc(AttributeDO::getSort)
                .orderByAsc(AttributeDO::getId);
        List<AttributeDO> attributes = baseMapper.selectList(wrapper);
        return attributes.stream()
                .map(attributeDO -> BeanUtil.toBean(attributeDO, AttributeRespDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean batchDeleteAttributes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ClientException("属性ID列表不能为空");
        }

        int deleted = baseMapper.deleteBatchIds(ids);
        if (deleted < 0) {
            throw new ServiceException("批量删除属性失败");
        }
        return true;
    }
}