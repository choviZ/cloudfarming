import type { AdvertRespDTO } from "@/types/advert";
import request from "./request";
import type { Result } from "@/types/common";

// 获取显示的广告
export const getShowAdverts = (): Promise<Result<AdvertRespDTO[]>> => {
    return request.get('/api/advert/show')
}
