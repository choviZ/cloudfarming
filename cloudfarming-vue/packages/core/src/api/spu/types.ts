/**
 * 农产品商品 SPU 相关类型定义
 */

import type { SkuRespDTO } from '../sku/types';

/**
 * SPU 价格摘要
 */
export interface SpuPriceSummary {
  /** 所属 SPU ID */
  spuId: number;

  /** 最低价格 */
  minPrice: number;

  /** 最高价格 */
  maxPrice: number;

  /** 最低价格对应的 SKU ID */
  minPriceSkuId: number;
}

/**
 * SPU 响应数据
 */
export interface SpuRespDTO {
  /** 主键ID */
  id: number;

  /** 标准商品名称 */
  title: string;

  /** 商品分类 id */
  categoryId: string;

  /** 商品图片 URL，多个图片用逗号分隔 */
  image: string;

  /** 商品状态：0-下架，1-上架，2-待审核 */
  status: number;
  
  /** 价格摘要 */
  priceSummary?: SpuPriceSummary;

  /** 店铺ID */
  shopId: string;


  /** 创建时间 */
  createTime: string;

  /** 更新时间 */
  updateTime: string;
}

/**
 * SPU 详情响应数据
 */
export interface SpuDetailRespDTO extends SpuRespDTO {
  /** 基础属性 */
  baseAttrs: Record<string, string>;

  /** SKU 列表 */
  skuList: SkuRespDTO[];
}

/**
 * 创建或修改 SPU 请求参数
 */
export interface SpuCreateOrUpdateReqDTO {
  /** 主键ID（修改时需要） */
  id?: number;

  /** 标准商品名称 */
  title: string;

  /** 店铺 id */
  shopId: string;

  /** 商品分类 id */
  categoryId: string;

  /** 商品图片 URL，多个图片用逗号分隔 */
  images: string;

  /** 商品状态：0-下架，1-上架，2-待审核 */
  status: number;
}

/**
 * SPU属性值响应数据
 */
export interface SpuAttrValueRespDTO {
  /** 主键ID */
  id: number;

  /** 标准商品 ID */
  spuId: number;

  /** 属性 ID */
  attrId: number;

  /** 属性名称 */
  attrName: string;

  /** 属性值 */
  attrValue: string;

  /** 创建时间 */
  createTime: string;

  /** 更新时间 */
  updateTime: string;
}

/**
 * 创建 SPU 属性值请求参数
 */
export interface SpuAttrValueCreateReqDTO {
  /** 标准商品 ID */
  spuId: string;

  /** 属性 ID */
  attrId: string;

  /** 属性值 */
  attrValue: string;
}

/**
 * 更新 SPU 属性值请求参数
 */
export interface SpuAttrValueUpdateReqDTO {
  /** 主键 ID */
  id: number;

  /** 标准商品 ID */
  spuId: number;

  /** 属性 ID */
  attrId: number;

  /** 属性值 */
  attrValue: string;
}

/**
 * SPU 分页查询请求参数
 */
export interface SpuPageQueryReqDTO {
  /** 当前页码 */
  current?: number;

  /** 每页显示条数 */
  size?: number;

  /** id */
  id?: number;

  /** 标准商品名称 */
  spuName?: string;

  /** 商品分类 id */
  categoryId?: string;

  /** 商品状态：0-下架，1-上架，2-待审核 */
  status?: number;
}
