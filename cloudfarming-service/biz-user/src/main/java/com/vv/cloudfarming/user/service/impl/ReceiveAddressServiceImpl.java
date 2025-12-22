package com.vv.cloudfarming.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.user.dao.entity.ReceiveAddressDO;
import com.vv.cloudfarming.user.dao.mapper.ReceiveAddressMapper;
import com.vv.cloudfarming.user.dto.req.ReceiveAddressAddReqDTO;
import com.vv.cloudfarming.user.dto.req.ReceiveAddressSetDefaultReqDTO;
import com.vv.cloudfarming.user.dto.req.ReceiveAddressUpdateReqDTO;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import com.vv.cloudfarming.user.service.ReceiveAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户收货地址服务实现层
 */
@Service
public class ReceiveAddressServiceImpl extends ServiceImpl<ReceiveAddressMapper, ReceiveAddressDO> implements ReceiveAddressService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addReceiveAddress(ReceiveAddressAddReqDTO requestParam) {
        long userId = StpUtil.getLoginIdAsLong();
        
        // 如果设置为默认地址，先将该用户其他地址设为非默认
        if (requestParam.getIsDefault() != null && requestParam.getIsDefault() == 1) {
            cancelDefault(userId);
        }
        
        ReceiveAddressDO receiveAddressDO = BeanUtil.toBean(requestParam, ReceiveAddressDO.class);
        receiveAddressDO.setUserId(userId);
        
        int inserted = baseMapper.insert(receiveAddressDO);
        if (inserted < 1) {
            throw new ServiceException("添加收货地址失败");
        }
        return true;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateReceiveAddress(ReceiveAddressUpdateReqDTO requestParam) {
        long userId = StpUtil.getLoginIdAsLong();
        
        // 验证地址是否存在且属于当前用户
        ReceiveAddressDO existingAddress = baseMapper.selectById(requestParam.getId());
        if (ObjectUtil.isNull(existingAddress) || !existingAddress.getUserId().equals(userId)) {
            throw new ClientException("收货地址不存在");
        }
        
        // 如果设置为默认地址，先将该用户其他地址设为非默认
        if (requestParam.getIsDefault() != null && requestParam.getIsDefault() == 1) {
            cancelDefault(userId);
        }
        
        ReceiveAddressDO receiveAddressDO = BeanUtil.toBean(requestParam, ReceiveAddressDO.class);
        int updated = baseMapper.updateById(receiveAddressDO);
        if (updated < 1) {
            throw new ServiceException("修改收货地址失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setDefaultReceiveAddress(ReceiveAddressSetDefaultReqDTO requestParam) {
        long userId = StpUtil.getLoginIdAsLong();
        
        // 验证地址是否存在且属于当前用户
        ReceiveAddressDO existingAddress = baseMapper.selectById(requestParam.getId());
        if (ObjectUtil.isNull(existingAddress) || !existingAddress.getUserId().equals(userId)) {
            throw new ClientException("收货地址不存在");
        }
        // 先将该用户所有地址设为非默认
        cancelDefault(userId);
        // 将指定地址设为默认
        LambdaUpdateWrapper<ReceiveAddressDO> defaultWrapper = new LambdaUpdateWrapper<>();
        defaultWrapper.eq(ReceiveAddressDO::getId, requestParam.getId())
                .set(ReceiveAddressDO::getIsDefault, 1);
        int updated = baseMapper.update(null, defaultWrapper);
        if (updated < 1) {
            throw new ServiceException("设置默认地址失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteReceiveAddress(Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        // 验证地址是否存在且属于当前用户
        ReceiveAddressDO existingAddress = baseMapper.selectById(id);
        if (ObjectUtil.isNull(existingAddress) || !existingAddress.getUserId().equals(userId)) {
            throw new ClientException("收货地址不存在");
        }
        int deleted = baseMapper.deleteById(id);
        if (deleted < 1) {
            throw new ServiceException("删除收货地址失败");
        }
        return true;
    }

    @Override
    public ReceiveAddressRespDTO getReceiveAddressById(Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        
        ReceiveAddressDO receiveAddressDO = baseMapper.selectById(id);
        if (ObjectUtil.isNull(receiveAddressDO) || !receiveAddressDO.getUserId().equals(userId)) {
            throw new ClientException("收货地址不存在");
        }
        return BeanUtil.toBean(receiveAddressDO, ReceiveAddressRespDTO.class);
    }

    @Override
    public List<ReceiveAddressRespDTO> getCurrentUserReceiveAddresses() {
        long userId = StpUtil.getLoginIdAsLong();
        
        LambdaQueryWrapper<ReceiveAddressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReceiveAddressDO::getUserId, userId)
                .orderByDesc(ReceiveAddressDO::getIsDefault)
                .orderByDesc(ReceiveAddressDO::getCreateTime);
        
        List<ReceiveAddressDO> addressList = baseMapper.selectList(wrapper);
        if (CollUtil.isEmpty(addressList)) {
            return CollUtil.newArrayList();
        }
        
        return addressList.stream()
                .map(address -> BeanUtil.toBean(address, ReceiveAddressRespDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ReceiveAddressRespDTO getCurrentUserDefaultReceiveAddress() {
        long userId = StpUtil.getLoginIdAsLong();
        
        LambdaQueryWrapper<ReceiveAddressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReceiveAddressDO::getUserId, userId)
                .eq(ReceiveAddressDO::getIsDefault, 1)
                .orderByDesc(ReceiveAddressDO::getCreateTime)
                .last("LIMIT 1");
        
        ReceiveAddressDO receiveAddressDO = baseMapper.selectOne(wrapper);
        if (ObjectUtil.isNull(receiveAddressDO)) {
            return null;
        }
        
        return BeanUtil.toBean(receiveAddressDO, ReceiveAddressRespDTO.class);
    }

    /**
     * 取消默认地址
     */
    private void cancelDefault(long userId) {
        LambdaUpdateWrapper<ReceiveAddressDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ReceiveAddressDO::getUserId, userId)
                .set(ReceiveAddressDO::getIsDefault, 0);
        baseMapper.update(null, updateWrapper);
    }
}