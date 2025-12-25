/**
 * 购物车项响应对象
 */
export interface CartItemRespDTO {
    /** 商品ID */
    productId: number;
    /** 商品数量 */
    quantity: number;
    /** 是否选中 */
    selected: boolean;
    /** 是否有库存 */
    hasStock: boolean;
    /** 商品名称 */
    productName: string;
    /** 商品图片 */
    productImage: string;
    /** 商品价格 - 建议前端使用 string 处理金额以避免精度丢失 */
    price: string;
    /** 商品总价（数量×价格） */
    totalPrice: string;
    /** 店铺ID */
    shopId: number;
    /** 店铺名称 */
    shopName: string;
}

/**
 * 购物车响应对象
 */
export interface CartRespDTO {
    /** 购物车项列表 */
    cartItems: CartItemRespDTO[];
    /** 选中商品总数量 */
    totalQuantity: number;
    /** 选中商品总金额 */
    totalAmount: string;
    /** 选中商品是否有库存 */
    allHasStock: boolean;
}

/**
 * 添加购物车项参数对象
 */
export interface CartItemAddReqDTO {
    /** 商品SKU ID (后端为 Long 类型) */
    skuId: string;
    /** 商品数量 */
    quantity: number;
    /** 是否选中 (默认 true) */
    selected?: boolean;
}

/**
 * 更新购物车项参数对象
 */
export interface CartItemUpdateReqDTO {
    /** 商品SKU ID (后端为 Long 类型) */
    skuId: string;
    /** 商品数量 */
    quantity: number;
    /** 是否选中 (必填) */
    selected: boolean;
}

