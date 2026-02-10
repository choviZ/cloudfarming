/**
 * 农户相关请求
 */
import request from './request'

/**
 * 农户入驻申请
 * @param {Object} data - 农户申请信息
 */
export const submitFarmerApply = (data) => {
  return request.post('/v1/farmer', data);
};

/**
 * 获取农户审核状态
 */
export const getFarmerReviewStatus = () => {
  return request.get('/v1/farmer/status');
};
