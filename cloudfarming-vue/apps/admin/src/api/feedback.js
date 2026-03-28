import request from './request'

export const getFeedbackTypes = () => {
  return request.get('/api/feedback/types')
}

export const getFeedbackPage = (data) => {
  return request.post('/api/feedback/page/admin', data)
}

export const processFeedback = (data) => {
  return request.post('/api/feedback/process', data)
}
