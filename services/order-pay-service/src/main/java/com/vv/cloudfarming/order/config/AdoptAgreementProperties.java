package com.vv.cloudfarming.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/***
 * 认养项目协议配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "agreement.adopt")
public class AdoptAgreementProperties {

    private String version = "v1.0.0";

    private String title = "云农场认养异常处置协议";

    private String content = """
        1. 认养项目受养殖周期、自然灾害、疫病防控等客观因素影响，平台与农户将根据实际情况启动异常处置流程。
        2. 如发生牲畜死亡、重大疾病或其他无法继续履约的情况，平台将优先安排同等价值的替换认养方案；无法替换时，按协议约定退款或折算补偿。
        3. 异常处置结果以平台公告、站内消息或订单通知为准，用户应配合完成后续确认流程。
        4. 用户下单即确认已充分知悉认养风险及处置方式，不得以未阅读协议为由拒绝平台已公示的处理方案。
        """;
}
