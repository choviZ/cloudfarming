import request from './request'

export const searchByKeyword = (data) => {
  return request.post('/api/search/v1/page', data)
}
