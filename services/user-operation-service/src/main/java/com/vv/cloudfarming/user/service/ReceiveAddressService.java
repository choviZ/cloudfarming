package com.vv.cloudfarming.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.user.dao.entity.ReceiveAddressDO;
import com.vv.cloudfarming.user.dto.req.ReceiveAddressAddReqDTO;
import com.vv.cloudfarming.user.dto.req.ReceiveAddressSetDefaultReqDTO;
import com.vv.cloudfarming.user.dto.req.ReceiveAddressUpdateReqDTO;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;

import java.util.List;

/**
 * 收货地址服务接口
 */
public interface ReceiveAddressService extends IService<ReceiveAddressDO> {

    Boolean addReceiveAddress(ReceiveAddressAddReqDTO requestParam);

    Boolean updateReceiveAddress(ReceiveAddressUpdateReqDTO requestParam);

    Boolean setDefaultReceiveAddress(ReceiveAddressSetDefaultReqDTO requestParam);

    Boolean deleteReceiveAddress(Long id);

    ReceiveAddressRespDTO getReceiveAddressById(Long id);

    /**
     * 内部接口：根据地址 ID 和用户 ID 获取收货地址
     */
    ReceiveAddressRespDTO getReceiveAddressByIdAndUserId(Long id, Long userId);

    List<ReceiveAddressRespDTO> getCurrentUserReceiveAddresses();

    ReceiveAddressRespDTO getCurrentUserDefaultReceiveAddress();
}