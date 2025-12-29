/**
 * 农户相关请求
 */
import request from './request'
import type { FarmerApplyReqDO, FarmerReviewRespDTO } from '@/types/farmer.ts'
import type { Result } from '@/types/common.ts'


/**
 * 农户入驻申请
 * @param data - 农户申请信息
 */
export const submitFarmerApply = (data: FarmerApplyReqDO): Promise<Result<void>> => {
  return request.post('/v1/farmer', data);
};

/**
 * 获取农户审核状态
 */
export const getFarmerReviewStatus = (): Promise<Result<FarmerReviewRespDTO>> => {
  return request.get('/v1/farmer/status');
};