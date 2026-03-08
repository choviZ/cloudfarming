package com.vv.cloudfarming.order.mq.modal;

import com.vv.cloudfarming.product.dao.entity.SeckillActivityDO;
import com.vv.cloudfarming.user.dto.resp.ReceiveAddressRespDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderMessage {
    private SeckillActivityDO seckillActivityDO;
    private Long userId;
    private Long nums;
    private String payNo;
    private String orderNo;
    private Long receiveId;
    private ReceiveAddressRespDTO receiveAddress;
}