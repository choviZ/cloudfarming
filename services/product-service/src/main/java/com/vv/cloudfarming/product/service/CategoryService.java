package com.vv.cloudfarming.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.product.dao.entity.CategoryDO;
import com.vv.cloudfarming.product.dto.req.CategoryCreateReqDTO;
import com.vv.cloudfarming.product.dto.req.CategoryUpdateReqDTO;
import com.vv.cloudfarming.product.dto.resp.CategoryRespDTO;

import java.util.List;

/**
* @description 针对表【t_category(商品分类表)】的数据库操作Service
*/
public interface CategoryService extends IService<CategoryDO> {

    /**
     * 创建商品分类
     *
     * @param requestParam 请求参数
     */
    void createCategory(CategoryCreateReqDTO requestParam);

    /**
     * 更新商品分类
     *
     * @param requestParam 请求参数
     * @return 是否成功
     */
    boolean updateCategory(CategoryUpdateReqDTO requestParam);

    /**
     * 删除商品分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long id);

    /**
     * 根据ID查询分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    CategoryRespDTO getCategoryById(Long id);

    /**
     * 获取分类树（支持多级分类）
     *
     * @return 分类树结构
     */
    List<CategoryRespDTO> getCategoryTree();

    /**
     * 获取所有顶级分类
     *
     * @return 顶级分类列表
     */
    List<CategoryRespDTO> getTopLevelCategories();

    /**
     * 根据父级ID查询子分类
     *
     * @param parentId 父级分类ID
     * @return 子分类列表
     */
    List<CategoryRespDTO> getChildrenByParentId(Long parentId);
}
