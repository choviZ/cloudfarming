package com.vv.cloudfarming.shop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.shop.dao.entity.SpuDO;
import com.vv.cloudfarming.shop.dao.mapper.SpuMapper;
import com.vv.cloudfarming.shop.dto.req.SpuCreateOrUpdateReqDTO;
import com.vv.cloudfarming.shop.dto.req.SpuPageQueryReqDTO;
import com.vv.cloudfarming.shop.dto.resp.CategoryRespDTO;
import com.vv.cloudfarming.shop.dto.resp.SpuRespDTO;
import com.vv.cloudfarming.shop.service.CategoryService;
import com.vv.cloudfarming.shop.service.SpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * SPU服务实现层
 */
@Service
@RequiredArgsConstructor
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuDO> implements SpuService {

    private final CategoryService categoryService;

    @Override
    public void saveOrUpdateSpu(SpuCreateOrUpdateReqDTO requestParam) {
        if (requestParam == null) {
            throw new ClientException("参数不能为空");
        }
        Long categoryId = requestParam.getCategoryId();

        CategoryRespDTO category = categoryService.getCategoryById(categoryId);
        if (category == null){
            throw new ClientException("分类不存在");
        }
        SpuDO spu = BeanUtil.toBean(requestParam, SpuDO.class);
        boolean result = this.saveOrUpdate(spu);
        if (!result) {
            throw new ServiceException("SPU创建或修改失败");
        }
    }

    @Override
    public void deleteSpuById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new ServiceException("SPU删除失败");
        }
    }

    @Override
    public SpuRespDTO getSpuById(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        SpuDO spuDO = this.getById(id);
        if (spuDO == null) {
            return null;
        }
        return BeanUtil.toBean(spuDO, SpuRespDTO.class);
    }

    @Override
    public IPage<SpuRespDTO> listSpuByPage(SpuPageQueryReqDTO queryParam) {
        if (queryParam == null) {
            throw new ClientException("参数不能为空");
        }

        Long id = queryParam.getId();
        String spuName = queryParam.getSpuName();
        Long categoryId = queryParam.getCategoryId();
        Integer status = queryParam.getStatus();
        // 构建查询条件
        LambdaQueryWrapper<SpuDO> queryWrapper = Wrappers.lambdaQuery(SpuDO.class)
                .like(!StringUtils.isEmpty(spuName), SpuDO::getSpuName, spuName)
                .eq(categoryId != null, SpuDO::getCategoryId, categoryId)
                .eq(status != null, SpuDO::getStatus, status);

        IPage<SpuDO> spuDOPage = baseMapper.selectPage(queryParam, queryWrapper);

        return spuDOPage.convert(spuDO -> BeanUtil.toBean(spuDO, SpuRespDTO.class));
    }

    @Override
    public void updateSpuStatus(Long id, Integer status) {
        if (id == null || id <= 0) {
            throw new ClientException("id不合法");
        }
        if (status == null) {
            throw new ClientException("状态不能为空");
        }
        SpuDO spuDO = baseMapper.selectById(id);
        if (spuDO == null) {
            throw new ClientException("SPU不存在");
        }
        if (spuDO.getStatus() == status) {
            throw new ClientException("请勿重复修改状态");
        }
        LambdaUpdateWrapper<SpuDO> updateWrapper = Wrappers.lambdaUpdate(SpuDO.class)
                .eq(SpuDO::getId, id)
                .set(SpuDO::getStatus, status);
        boolean updated = this.update(updateWrapper);
        if (!updated) {
            throw new ServiceException("SPU状态更新失败");
        }
    }
}
