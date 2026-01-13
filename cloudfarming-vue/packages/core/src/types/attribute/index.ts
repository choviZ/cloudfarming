/**
 * 商品属性相关类型定义
 */

/**
 * 属性响应数据
 */
export interface AttributeRespDTO {
  /** 属性ID */
  id: string;

  /** 关联的分类ID */
  categoryId: number;

  /** 属性名 */
  name: string;

  /** 属性类型：0-基本属性 / 1-销售属性 */
  attrType: number;

  /** 排序权重 */
  sort: number;

  /** 创建时间 */
  createTime: string;

  /** 更新时间 */
  updateTime: string;
}

/**
 * 属性值响应数据
 */
export interface AttributeValueRespDTO {
  /** 属性值ID */
  id: number;

  /** 属性ID */
  attributeId: number;

  /** 属性值名称 */
  name: string;

  /** 排序权重 */
  sort: number;

  /** 创建时间 */
  createTime: string;

  /** 更新时间 */
  updateTime: string;
}
