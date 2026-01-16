/**
 * 商品分类相关类型定义
 */

/**
 * 商品分类信息
 */
export interface CategoryRespDTO {
  /** 分类ID */
  id: string;

  /** 分类名称 */
  name: string;

  /** 父级分类ID，顶级分类为NULL */
  parentId: string | null;

  /** 排序权重 */
  sortOrder: number;

  /** 创建时间 */
  createTime: Date;

  /** 更新时间 */
  updateTime: Date;

  /** 子分类列表 */
  children?: CategoryRespDTO[];
}

/**
 * 创建商品分类请求参数
 */
export interface CategoryCreateReqDTO {
  /**
   * 分类名称
   */
  name: string;

  /**
   * 父级分类ID，顶级分类为NULL
   */
  parentId?: string | null;

  /**
   * 排序权重
   */
  sortOrder?: number;
}

/**
 * 更新商品分类请求参数
 */
export interface CategoryUpdateReqDTO {
  /**
   * 分类ID
   */
  id: string;

  /**
   * 分类名称
   */
  name?: string;

  /**
   * 父级分类ID，顶级分类为NULL
   */
  parentId?: string | null;

  /**
   * 排序权重
   */
  sortOrder?: number;
}

/**
 * 商品分类分页查询请求参数
 */
export interface CategoryPageQueryReqDTO {
  /**
   * 当前页码
   */
  current?: number;

  /**
   * 每页显示条数
   */
  size?: number;

  /**
   * 分类名称（模糊查询）
   */
  name?: string;

  /**
   * 父级分类ID
   */
  parentId?: string | null;
}
