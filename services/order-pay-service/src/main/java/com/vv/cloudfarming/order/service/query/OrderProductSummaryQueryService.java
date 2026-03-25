package com.vv.cloudfarming.order.service.query;

import com.vv.cloudfarming.order.dao.mapper.OrderProductSummaryMapper;
import com.vv.cloudfarming.order.dto.common.OrderProductSummaryRecordDTO;
import com.vv.cloudfarming.order.dto.common.ProductSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderProductSummaryQueryService {

    private final OrderProductSummaryMapper orderProductSummaryMapper;

    public Map<String, List<ProductSummaryDTO>> mapByOrderNos(List<String> orderNos) {
        if (orderNos == null || orderNos.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> distinctOrderNos = orderNos.stream()
            .filter(Objects::nonNull)
            .filter(orderNo -> !orderNo.isBlank())
            .distinct()
            .collect(Collectors.toList());
        if (distinctOrderNos.isEmpty()) {
            return Collections.emptyMap();
        }
        return orderProductSummaryMapper.selectByOrderNos(distinctOrderNos).stream()
            .collect(Collectors.groupingBy(
                OrderProductSummaryRecordDTO::getOrderNo,
                LinkedHashMap::new,
                Collectors.mapping(this::toProductSummary, Collectors.toList())
            ));
    }

    private ProductSummaryDTO toProductSummary(OrderProductSummaryRecordDTO record) {
        return ProductSummaryDTO.builder()
            .productName(record.getProductName())
            .coverImage(record.getCoverImage())
            .price(record.getPrice())
            .quantity(record.getQuantity())
            .build();
    }
}
