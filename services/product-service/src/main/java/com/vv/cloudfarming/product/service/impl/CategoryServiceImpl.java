package com.vv.cloudfarming.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.product.dao.entity.CategoryDO;
import com.vv.cloudfarming.product.dao.mapper.CategoryMapper;
import com.vv.cloudfarming.product.dto.req.CategoryCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.CategoryUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.CategoryRespDTO;
import com.vv.cloudfarming.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @description 针对表【t_category(商品分类表)】的数据库操作Service实现
*/
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryDO> implements CategoryService {

    @Override
    public void createCategory(CategoryCreateReqDTO requestParam) {
        String name = requestParam.getName();
        Long parentId = requestParam.getParentId();
        Integer sortOrder = requestParam.getSortOrder();

        if (StrUtil.isBlank(name)) {
            throw new ClientException("分类名称不能为空");
        }
        if (ObjectUtil.isNotNull(parentId)) {
            CategoryDO parentCategory = baseMapper.selectById(parentId);
            if (parentCategory == null) {
                throw new ClientException("父级分类不存在");
            }
        }

        CategoryDO category = BeanUtil.toBean(requestParam, CategoryDO.class);
        int inserted = baseMapper.insert(category);
        if (!SqlHelper.retBool(inserted)) {
            throw new ServiceException("商品分类创建失败");
        }
    }

    @Override
    public boolean updateCategory(CategoryUpdateReqDTO requestParam) {
        Long id = requestParam.getId();
        String name = requestParam.getName();
        Long parentId = requestParam.getParentId();
        Integer sortOrder = requestParam.getSortOrder();

        if (id == null || id <= 0) {
            throw new ClientException("分类ID不合法");
        }

        CategoryDO existingCategory = baseMapper.selectById(id);
        if (existingCategory == null) {
            throw new ClientException("分类不存在");
        }

        if (ObjectUtil.isNotNull(parentId)) {
            if (parentId.equals(id)) {
                throw new ClientException("不能将分类设置为自己的父级");
            }

            CategoryDO parentCategory = baseMapper.selectById(parentId);
            if (parentCategory == null) {
                throw new ClientException("父级分类不存在");
            }

            List<Long> childrenIds = getAllChildrenIds(id);
            if (childrenIds.contains(parentId)) {
                throw new ClientException("不能将分类设置为自己的子分类的父级");
            }
        }

        LambdaUpdateWrapper<CategoryDO> wrapper = Wrappers.lambdaUpdate(CategoryDO.class)
                .eq(CategoryDO::getId, id)
                .set(StrUtil.isNotBlank(name), CategoryDO::getName, name)
                .set(ObjectUtil.isNotNull(parentId), CategoryDO::getParentId, parentId)
                .set(ObjectUtil.isNotNull(sortOrder), CategoryDO::getSortOrder, sortOrder);
        int updated = baseMapper.update(wrapper);
        if (!SqlHelper.retBool(updated)) {
            throw new ServiceException("更新商品分类失败");
        }
        return true;
    }

    @Override
    public boolean deleteCategory(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("分类ID不合法");
        }

        CategoryDO category = baseMapper.selectById(id);
        if (category == null) {
            throw new ClientException("分类不存在");
        }

        LambdaQueryWrapper<CategoryDO> wrapper = Wrappers.lambdaQuery(CategoryDO.class)
                .eq(CategoryDO::getParentId, id);
        long childrenCount = baseMapper.selectCount(wrapper);
        if (childrenCount > 0) {
            throw new ClientException("该分类下存在子分类，无法删除");
        }

        int deleted = baseMapper.deleteById(id);
        if (deleted < 0) {
            throw new ServiceException("删除商品分类失败");
        }
        return true;
    }

    @Override
    public CategoryRespDTO getCategoryById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("分类ID不合法");
        }
        CategoryDO categoryDO = baseMapper.selectById(id);
        if (categoryDO == null) {
            return null;
        }
        return BeanUtil.toBean(categoryDO, CategoryRespDTO.class);
    }

    @Override
    public List<CategoryRespDTO> getCategoryTree() {
        LambdaQueryWrapper<CategoryDO> wrapper = Wrappers.lambdaQuery(CategoryDO.class)
                .orderByAsc(CategoryDO::getSortOrder)
                .orderByAsc(CategoryDO::getId);
        List<CategoryDO> allCategories = baseMapper.selectList(wrapper);
        return buildCategoryTree(allCategories, null);
    }

    @Override
    public List<CategoryRespDTO> getTopLevelCategories() {
        LambdaQueryWrapper<CategoryDO> wrapper = Wrappers.lambdaQuery(CategoryDO.class)
                .isNull(CategoryDO::getParentId)
                .orderByAsc(CategoryDO::getSortOrder)
                .orderByAsc(CategoryDO::getId);
        List<CategoryDO> topCategories = baseMapper.selectList(wrapper);
        return topCategories.stream()
                .map(categoryDO -> BeanUtil.toBean(categoryDO, CategoryRespDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryRespDTO> getChildrenByParentId(Long parentId) {
        if (parentId == null || parentId <= 0) {
            throw new ClientException("父级分类ID不合法");
        }

        CategoryDO parentCategory = baseMapper.selectById(parentId);
        if (parentCategory == null) {
            throw new ClientException("父级分类不存在");
        }

        LambdaQueryWrapper<CategoryDO> wrapper = Wrappers.lambdaQuery(CategoryDO.class)
                .eq(CategoryDO::getParentId, parentId)
                .orderByAsc(CategoryDO::getSortOrder)
                .orderByAsc(CategoryDO::getId);
        List<CategoryDO> children = baseMapper.selectList(wrapper);
        return children.stream()
                .map(categoryDO -> BeanUtil.toBean(categoryDO, CategoryRespDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * 构建分类树
     * @param allCategories 所有分类
     * @param parentId
     * @return
     */
    private List<CategoryRespDTO> buildCategoryTree(List<CategoryDO> allCategories, Long parentId) {
        List<CategoryRespDTO> result = new ArrayList<>();
        for (CategoryDO category : allCategories) {
            if ((parentId == null && category.getParentId() == null) || 
                (parentId != null && parentId.equals(category.getParentId()))) {
                CategoryRespDTO categoryRespDTO = BeanUtil.toBean(category, CategoryRespDTO.class);
                List<CategoryRespDTO> children = buildCategoryTree(allCategories, category.getId());
                categoryRespDTO.setChildren(children.isEmpty() ? null : children);
                result.add(categoryRespDTO);
            }
        }
        return result;
    }

    /**
     * 获取所有子分类项
     * @param parentId 父id
     */
    private List<Long> getAllChildrenIds(Long parentId) {
        List<Long> childrenIds = new ArrayList<>();
        LambdaQueryWrapper<CategoryDO> wrapper = Wrappers.lambdaQuery(CategoryDO.class)
                .eq(CategoryDO::getParentId, parentId);
        List<CategoryDO> children = baseMapper.selectList(wrapper);
        for (CategoryDO child : children) {
            childrenIds.add(child.getId());
            childrenIds.addAll(getAllChildrenIds(child.getId()));
        }
        return childrenIds;
    }
}




