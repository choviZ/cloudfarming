import request from './request'

export const submitFarmerApply = (data) => {
  return request.post('/api/farmer/apply', data)
}

export const getFarmerReviewStatus = () => {
  return request.get('/api/farmer/status')
}

export const pageFarmerShowcase = (data) => {
  return request.post('/api/farmer/showcase/page', data)
}

export const getFarmerShowcaseDetail = (id) => {
  return request.get('/api/farmer/showcase/detail', {
    params: { id }
  })
}

export const getMyFarmerShowcase = () => {
  return request.get('/api/farmer/showcase/my')
}

export const updateMyFarmerShowcase = (data) => {
  return request.post('/api/farmer/showcase/update', data)
}
