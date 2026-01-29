package com.vv.cloudfarming.product.dto.resp;

import com.vv.cloudfarming.product.dao.entity.SpuDO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * SPU 详情响应对象
 */
@Data
public class SpuDetailRespDTO extends SpuDO implements Serializable {
    
    /**
     * 基础属性集合 (key: 属性名, value: 属性值)
     */
    private Map<String, String> baseAttrs;
    
    /**
     * 规格列表
     */
    private List<SkuRespDTO> skuList;
}
