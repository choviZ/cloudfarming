/**
 * 销售属性
 */
export interface SaleAttrDTO {
  /** 属性 ID */
  attrId: string;

  /** 属性名称 */
  attrName: string;

  /** 属性值 ID */
  attrValueId: string;

  /** 属性值名称 */
  attrValueName: string;
}

/**
 * SKU 业务数据
 */
export interface SkuItemDTO {
  /** SKU 价格 */
  price: number;

  /** SKU 库存 */
  stock: number;

  /** SKU 图片 */
  image: string;

  /** 销售属性键值对 */
  saleAttrs: Record<string, string>;
}

/**
 * 创建 SKU 请求参数
 */
export interface SkuCreateReqDTO {
  /** 关联 SPU ID */
  spuId: string;

  /** 销售属性集合 */
  saleAttrs: SaleAttrDTO[];

  /** 每个 SKU 的业务数据 */
  skuItems: SkuItemDTO[];
}