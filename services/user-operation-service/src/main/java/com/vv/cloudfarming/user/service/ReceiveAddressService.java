package com.vv.cloudfarming.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.user.dao.entity.ReceiveAddressDO;
import com.vv.cloudfarming.user.dto.req.ReceiveAddressAddReqDTO;
import com.vv.cloudfarming.user.dto.req.ReceiveAddressSetDefaultReqDTO;
import com.vv.cloudfarming.user.dto.req.ReceiveAddressUpdateReqDTO;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;

import java.util.List;

/**
 * 用户收货地址服务接口
 */
public interface ReceiveAddressService extends IService<ReceiveAddressDO> {

    /**
     * 添加收货地址
     *
     * @param requestParam 添加收货地址参数
     * @return 是否成功
     */
    Boolean addReceiveAddress(ReceiveAddressAddReqDTO requestParam);

    /**
     * 修改收货地址
     *
     * @param requestParam 修改收货地址参数
     * @return 是否成功
     */
    Boolean updateReceiveAddress(ReceiveAddressUpdateReqDTO requestParam);

    /**
     * 设置默认收货地址
     *
     * @param requestParam 设置默认地址参数
     * @return 是否成功
     */
    Boolean setDefaultReceiveAddress(ReceiveAddressSetDefaultReqDTO requestParam);

    /**
     * 删除收货地址
     *
     * @param id 地址ID
     * @return 是否成功
     */
    Boolean deleteReceiveAddress(Long id);

    /**
     * 根据ID获取收货地址详情
     *
     * @param id 地址ID
     * @return 收货地址详情
     */
    ReceiveAddressRespDTO getReceiveAddressById(Long id);

    /**
     * 获取当前登录用户的所有收货地址
     *
     * @return 收货地址列表
     */
    List<ReceiveAddressRespDTO> getCurrentUserReceiveAddresses();

    /**
     * 获取当前登录用户的默认收货地址
     *
     * @return 默认收货地址
     */
    ReceiveAddressRespDTO getCurrentUserDefaultReceiveAddress();
}