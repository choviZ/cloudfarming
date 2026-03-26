import request from './request'

export const pageManageArticles = (data) => {
  return request.post('/api/article/manage/page', data)
}

export const getManageArticleDetail = (id) => {
  return request.get('/api/article/manage/get', {
    params: { id }
  })
}

export const publishArticle = (data) => {
  return request.post('/api/article/publish', data)
}

export const updateArticle = (data) => {
  return request.post('/api/article/update', data)
}

export const updateArticleStatus = (data) => {
  return request.post('/api/article/status', data)
}

export const deleteArticle = (id) => {
  return request.post('/api/article/delete', null, {
    params: { id }
  })
}
