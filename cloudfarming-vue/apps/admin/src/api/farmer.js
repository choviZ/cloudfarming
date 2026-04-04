import request from './request'

export const getFarmerPage = (data) => {
  return request.post('/api/farmer/admin/page', data)
}

export const updateFarmerFeaturedFlag = (data) => {
  return request.post('/api/farmer/admin/feature', data)
}
