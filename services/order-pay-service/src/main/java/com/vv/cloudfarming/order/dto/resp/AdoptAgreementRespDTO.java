package com.vv.cloudfarming.order.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认养项目协议
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdoptAgreementRespDTO {

    private String version;

    private String title;

    private String content;
}
