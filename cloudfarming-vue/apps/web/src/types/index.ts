/**
 * 销售属性数据结构
 * 用于 SkuTable 组件的 props，描述销售属性的配置
 */
export interface SaleAttribute {
  key: string
  label: string
  values: string[]
}

/**
 * 基本属性项数据结构
 * 用于描述商品的基本属性（类型0），如产地、保质期等
 */
export interface BaseAttributeItem {
  key: string
  label: string
  value: string
}

/**
 * 销售属性项数据结构
 * 用于描述商品的销售属性（类型1），如颜色、尺码等
 */
export interface SaleAttributeItem {
  key: string
  label: string
  values: string[]
}

/**
 * 单个规格项（SKU的一个维度）
 */
export interface SpecItem {
  attrId: string
  attrName: string
  value: string
}

/**
 * SKU 数据结构
 * 用于组件间传递 SKU 数据
 */
export interface SKU {
  specs: SpecItem[]
  stock: number
  price: number
  _key?: string
}
