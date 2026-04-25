package com.vv.cloudfarming.order.service.query;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.order.config.AliyunLogisticsProperties;
import com.vv.cloudfarming.order.dao.entity.OrderDO;
import com.vv.cloudfarming.order.dto.resp.OrderLogisticsRespDTO;
import com.vv.cloudfarming.order.dto.resp.OrderLogisticsTraceRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 阿里云物流查询服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AliyunLogisticsQueryService {

    private static final String AUTHORIZATION_PREFIX = "APPCODE ";
    private static final Map<Integer, String> DELIVERY_STATUS_TEXT_MAP = Map.of(
        0, "快递收件(揽件)",
        1, "在途中",
        2, "正在派件",
        3, "已签收",
        4, "派送失败",
        5, "疑难件",
        6, "退件签收"
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AliyunLogisticsProperties aliyunLogisticsProperties;

    public OrderLogisticsRespDTO queryOrderLogistics(OrderDO order) {
        if (!Boolean.TRUE.equals(aliyunLogisticsProperties.getEnabled())) {
            throw new ServiceException("物流查询服务未启用");
        }
        String appCode = StrUtil.trim(aliyunLogisticsProperties.getAppCode());
        if (StrUtil.isBlank(appCode)) {
            throw new ServiceException("物流查询服务未配置 AppCode");
        }

        String companyCode = resolveCompanyCode(order.getLogisticsCompany());
        String queryNo = buildQueryNo(order, companyCode);
        URI queryUri = buildQueryUri(queryNo, companyCode);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, AUTHORIZATION_PREFIX + appCode);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                queryUri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
            );
            String responseBody = response.getBody();
            if (StrUtil.isBlank(responseBody)) {
                throw new ServiceException("物流服务返回为空");
            }
            JsonNode rootNode = objectMapper.readTree(responseBody);
            return buildResponse(order, rootNode, companyCode);
        } catch (RestClientResponseException ex) {
            log.error("调用阿里云物流查询失败, orderNo={}, status={}, body={}",
                order.getOrderNo(), ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex);
            throw new ServiceException("物流查询服务调用失败，请稍后重试");
        } catch (JsonProcessingException ex) {
            log.error("解析阿里云物流返回失败, orderNo={}", order.getOrderNo(), ex);
            throw new ServiceException("物流查询结果解析失败，请稍后重试");
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("调用阿里云物流查询异常, orderNo={}", order.getOrderNo(), ex);
            throw new ServiceException("物流查询失败，请稍后重试");
        }
    }

    private URI buildQueryUri(String queryNo, String companyCode) {
        String baseUrl = StrUtil.removeSuffix(StrUtil.trim(aliyunLogisticsProperties.getBaseUrl()), "/");
        String path = StrUtil.blankToDefault(StrUtil.trim(aliyunLogisticsProperties.getPath()), "/kdi");
        String fullUrl = baseUrl + (path.startsWith("/") ? path : "/" + path);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fullUrl)
            .queryParam("no", queryNo);
        if (StrUtil.isNotBlank(companyCode)) {
            builder.queryParam("type", companyCode);
        }
        return builder.build(true).toUri();
    }

    private OrderLogisticsRespDTO buildResponse(OrderDO order, JsonNode rootNode, String resolvedCompanyCode) {
        JsonNode resultNode = rootNode.path("result");
        List<OrderLogisticsTraceRespDTO> traces = extractTraces(resultNode.path("list"));
        Integer deliveryStatus = parseInteger(firstNonBlank(
            text(resultNode, "deliverystatus"),
            text(resultNode, "deliveryStatus")
        ));

        String latestTrackTime = firstNonBlank(
            text(resultNode, "updateTime"),
            traces.isEmpty() ? null : traces.get(0).getTime()
        );

        return OrderLogisticsRespDTO.builder()
            .orderNo(order.getOrderNo())
            .logisticsNo(StrUtil.trim(order.getLogisticsNo()))
            .logisticsCompany(StrUtil.trim(order.getLogisticsCompany()))
            .queryStatus(text(rootNode, "status"))
            .queryMessage(firstNonBlank(text(rootNode, "msg"), "暂无物流信息"))
            .companyCode(firstNonBlank(text(resultNode, "type"), resolvedCompanyCode))
            .companyName(firstNonBlank(
                text(resultNode, "expName"),
                text(resultNode, "typename"),
                StrUtil.trim(order.getLogisticsCompany())
            ))
            .companyPhone(text(resultNode, "expPhone"))
            .companyWebsite(text(resultNode, "expSite"))
            .courierName(text(resultNode, "courier"))
            .courierPhone(text(resultNode, "courierPhone"))
            .logo(text(resultNode, "logo"))
            .latestTrackTime(latestTrackTime)
            .takeTime(text(resultNode, "takeTime"))
            .deliveryStatus(deliveryStatus)
            .deliveryStatusText(resolveDeliveryStatusText(deliveryStatus))
            .signed(parseSignedFlag(firstNonBlank(text(resultNode, "issign"), text(resultNode, "isSign"))))
            .traces(traces)
            .build();
    }

    private String resolveDeliveryStatusText(Integer deliveryStatus) {
        if (deliveryStatus == null) {
            return "暂无状态";
        }
        return DELIVERY_STATUS_TEXT_MAP.getOrDefault(deliveryStatus, "未知状态");
    }

    private List<OrderLogisticsTraceRespDTO> extractTraces(JsonNode listNode) {
        if (listNode == null || !listNode.isArray()) {
            return List.of();
        }
        List<OrderLogisticsTraceRespDTO> traces = new ArrayList<>();
        for (JsonNode traceNode : listNode) {
            String time = firstNonBlank(text(traceNode, "time"), text(traceNode, "ftime"));
            String status = firstNonBlank(text(traceNode, "status"), text(traceNode, "context"));
            if (StrUtil.isAllBlank(time, status)) {
                continue;
            }
            traces.add(OrderLogisticsTraceRespDTO.builder()
                .time(time)
                .status(status)
                .build());
        }
        return traces;
    }

    private String buildQueryNo(OrderDO order, String companyCode) {
        String logisticsNo = StrUtil.trim(order.getLogisticsNo());
        if (StrUtil.isBlank(logisticsNo) || logisticsNo.contains(":")) {
            return logisticsNo;
        }
        if (!"SFEXPRESS".equalsIgnoreCase(companyCode)) {
            return logisticsNo;
        }
        String receivePhone = StrUtil.trim(order.getReceivePhone());
        String digits = receivePhone == null ? "" : receivePhone.replaceAll("\\D", "");
        if (digits.length() < 4) {
            return logisticsNo;
        }
        return logisticsNo + ":" + digits.substring(digits.length() - 4);
    }

    private String resolveCompanyCode(String logisticsCompany) {
        String normalized = normalizeCompany(logisticsCompany);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        if (normalized.matches("[A-Z0-9_]+")) {
            return normalized;
        }
        String lowerCaseName = normalized.toLowerCase(Locale.ROOT);
        if (lowerCaseName.contains("百世快运") || lowerCaseName.contains("bsky")) {
            return "BSKY";
        }
        if (lowerCaseName.contains("中通快运") || lowerCaseName.contains("zto56")) {
            return "ZTO56";
        }
        if (lowerCaseName.contains("韵达快运") || lowerCaseName.contains("yunda56")) {
            return "YUNDA56";
        }
        if (lowerCaseName.contains("顺丰") || lowerCaseName.contains("sfexpress")) {
            return "SFEXPRESS";
        }
        if (lowerCaseName.contains("中通")) {
            return "ZTO";
        }
        if (lowerCaseName.contains("圆通")) {
            return "YTO";
        }
        if (lowerCaseName.contains("申通")) {
            return "STO";
        }
        if (lowerCaseName.contains("韵达")) {
            return "YUNDA";
        }
        if (lowerCaseName.contains("京东")) {
            return "JD";
        }
        if (lowerCaseName.contains("德邦")) {
            return "DEPPON";
        }
        if (lowerCaseName.contains("百世") || lowerCaseName.contains("汇通") || lowerCaseName.contains("htky")) {
            return "HTKY";
        }
        if (lowerCaseName.contains("天天")) {
            return "TTKDEX";
        }
        if (lowerCaseName.contains("宅急送")) {
            return "ZJS";
        }
        if (lowerCaseName.contains("安能快递") || lowerCaseName.contains("aneex")) {
            return "ANEEX";
        }
        if (lowerCaseName.contains("安能")) {
            return "ANE";
        }
        if (lowerCaseName.contains("邮政包裹") || lowerCaseName.contains("chinapost")) {
            return "CHINAPOST";
        }
        if (lowerCaseName.contains("邮政速递") || lowerCaseName.contains("邮政ems") || lowerCaseName.contains("ems")) {
            return "EMS";
        }
        if (lowerCaseName.contains("跨越")) {
            return "KYEXPRESS";
        }
        return null;
    }

    private String normalizeCompany(String logisticsCompany) {
        String companyName = StrUtil.trim(logisticsCompany);
        if (StrUtil.isBlank(companyName)) {
            return null;
        }
        return companyName
            .replaceAll("[\\s\\-()（）]", "")
            .toUpperCase(Locale.ROOT);
    }

    private Integer parseInteger(String value) {
        if (StrUtil.isBlank(value) || !value.matches("-?\\d+")) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private Boolean parseSignedFlag(String signedFlag) {
        if (StrUtil.isBlank(signedFlag)) {
            return null;
        }
        return "1".equals(signedFlag);
    }

    private String firstNonBlank(String... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        for (String value : values) {
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        JsonNode valueNode = node.path(fieldName);
        if (valueNode.isMissingNode() || valueNode.isNull()) {
            return null;
        }
        String value = valueNode.asText();
        return StrUtil.isBlank(value) ? null : value;
    }
}
