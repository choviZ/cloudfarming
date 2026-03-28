import request from './request'

export const getFeedbackTypes = () => {
  return request.get('/api/feedback/types')
}

export const submitFeedback = (data) => {
  return request.post('/api/feedback/submit', data)
}
