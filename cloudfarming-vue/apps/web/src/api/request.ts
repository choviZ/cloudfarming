import axios from 'axios'
import router from '@/router/index.ts'
import {message} from "ant-design-vue";

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
        if (response.data.code == 500) {
            router.push('/user/login')
            message.warn('登录已过期，请重新登录')
        }
        // 统一处理响应数据格式
        return response.data
    },
    (error) => {
        // 统一处理错误
        console.error('API请求错误:', error)
        return Promise.reject(error)
    }
)

export default request
