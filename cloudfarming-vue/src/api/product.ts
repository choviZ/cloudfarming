import type { ProductPageQueryReqDTO, ProductRespDTO } from "@/types/product";
import type { IPage } from "../types/common";
import type { Result } from "../types/common";
import request from "./request";

/**
 * 获取农产品列表
 * @param queryParam 
 * @returns 
 */
export const getProductList = (queryParam: ProductPageQueryReqDTO): Promise<Result<IPage<ProductRespDTO>>> => {
    return request.post('/v1/product/page', queryParam)
}