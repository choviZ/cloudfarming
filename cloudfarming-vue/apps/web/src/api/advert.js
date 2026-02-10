import request from "./request";

// 获取显示的广告
export const getShowAdverts = () => {
    return request.get('/api/advert/show')
}
