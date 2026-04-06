import request from './request'

/**
 * 文件上传
 * @param {File} file
 * @param {Object} data - UploadFileRequest
 * @returns {Promise<Object>} Result<string>
 */
export function uploadMedia(file, data = {}) {
  const formData = new FormData()
  formData.append('file', file)

  Object.entries(data).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      formData.append(key, String(value))
    }
  })

  return request.post('/api/spu/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const upload = uploadMedia
