import type { UserRespDTO } from './userType';

/**
 * 农产品商品信息
 */
export interface ProductRespDTO {

    /**
     * id
     */
    id: number;

    /**
     * 店铺id
     */
    shopId: number;

    /**
     * 创建人信息
     */
    createUser: UserRespDTO;

    /**
     * 商品名称
     */
    productName: string;

    /**
     * 商品分类
     */
    productCategory: string;

    /**
     * 产地
     */
    originPlace: string;

    /**
     * 规格（如：5斤装/单果80mm以上）
     */
    specification: string;

    /**
     * 销售价格（元）
     */
    price: string;

    /**
     * 库存数量
     */
    stock: number;

    /**
     * 商品描述
     */
    description: string;

    /**
     * 商品图片URL，多个图片用逗号分隔
     */
    productImg: string;

    /**
     * 商品状态：0-下架，1-上架，2-待审核
     */
    status: number;

    /**
     * 上架时间
     */
    shelfTime: string | null;

    /**
     * 下架时间
     */
    offShelfTime: string | null;
}

/**
 * 分页查询农产品商品请求参数
 */
export interface ProductPageQueryReqDTO {
    /**
     * 当前页码
     */
    current?: number;

    /**
     * 每页大小
     */
    size?: number;

    /**
     * 商品id
     */
    id?: number;

    /**
     * 店铺id
     */
    shopId?: number;

    /**
     * 商品名称
     */
    productName?: string;

    /**
     * 商品分类
     */
    productCategory?: string;

    /**
     * 产地
     */
    originPlace?: string;

    /**
     * 规格（如：5斤装/单果80mm以上）
     */
    specification?: string;

    /**
     * 销售价格（元）
     */
    price?: string;

    /**
     * 库存数量
     */
    stock?: number;

    /**
     * 商品描述
     */
    description?: string;
}