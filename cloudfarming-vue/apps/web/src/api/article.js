import request from './request'

export const pagePublishedArticles = (data) => {
  return request.post('/api/article/page', data)
}

export const getPublishedArticleDetail = (id) => {
  return request.get('/api/article/get', {
    params: { id }
  })
}