/**
 * 创建认养项目请求参数
 */
export interface AdoptItemCreateReq {
    /** 认养标题 */
    title: string

    /** 动物分类（代码） */
    animalCategory: string

    /** 认养周期（天） */
    adoptDays: number

    /** 认养价格 */
    price: number

    /** 预计收益（如：10kg肉） */
    expectedYield?: string

    /** 认养说明 */
    description: string

    /** 封面图片 */
    coverImage: string
}

/**
 * MyBatis Plus 分页请求基类
 */
export interface PageReq {
    /** 当前页 */
    current: number

    /** 每页大小 */
    size: number
}

/**
 * 分页查询认养项目请求参数
 */
export interface AdoptItemPageReq extends PageReq {
    /** 动物分类（代码） */
    animalCategory?: string

    /** 标题（模糊查询） */
    title?: string

    /** 审核状态 */
    reviewStatus?: number

    /** 上架状态 */
    status?: number

    /** 用户ID（查询我发布的认养项目） */
    userId?: string
}

/**
 * 更新认养项目请求参数
 */
export interface AdoptItemUpdateReq {
    /** 认养项目ID */
    id: string

    /** 认养标题 */
    title: string

    /** 动物分类（代码） */
    animalCategory: string

    /** 认养周期（天） */
    adoptDays: number

    /** 认养价格 */
    price: number

    /** 预计收益 */
    expectedYield?: string

    /** 认养说明 */
    description: string

    /** 封面图片 */
    coverImage: string
}

/**
 * 创建认养订单请求参数
 */
export interface AdoptOrderCreateReq {
    /** 认养项目ID */
    adoptItemId: string
    /** 收货地址ID */
    receiveId: string
}

/**
 * 认养项目响应
 */
export interface AdoptItemResp {
    id: string
    userId: string
    title: string
    animalCategory: string
    adoptDays: number
    price: number
    expectedYield?: string
    description: string
    coverImage: string
    reviewStatus: number
    /** 审核拒绝原因 */
    reviewText?: string
    status: number
    createTime: string
}

/**
 * 认养订单响应 DTO
 */
export interface AdoptOrderResp {
    /** 订单ID（雪花ID） */
    id: string

    /** 买家用户ID */
    buyerId: string

    /** 认养项目ID */
    adoptItemId: string

    /** 认养价格（下单时快照） */
    price: number

    /** 认养开始日期 */
    startDate: string

    /** 认养结束日期 */
    endDate: string

    /** 认养状态：1=认养中 2=已完成 3=已取消 */
    status: number

    /** 收货地址ID */
    receiveId: string

    /** 创建时间 */
    createTime: string

    /** 更新时间 */
    updateTime: string
}

/**
 * 动物分类响应 DTO
 */
export interface AnimalCategoryResp {
  /** 分类代码（用于数据库/业务逻辑） */
  code: string

  /** 分类名称（用于展示） */
  name: string
}
