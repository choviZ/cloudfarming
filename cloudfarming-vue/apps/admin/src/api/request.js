import axios from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: '/api', // API基础路径，可根据环境变量配置
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 可以在这里添加token等认证信息
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data.code && data.code !== '0') {
      return Promise.reject(data)
    }
    return data
  },
  (error) => {
    console.error('API请求错误:', error)
    return Promise.reject(error)
  }
)

export default request
