import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  withCredentials: true
})

request.interceptors.response.use(
  res => res,
  err => {
    if (err.config?.skipAuthRedirect) {
      return Promise.reject(err)
    }
    if (err.response?.status === 401) {
      sessionStorage.removeItem('erpLoggedIn')
      ElMessage.error('请先登录')
      window.location.href = '/login'
    } else {
      ElMessage.error(err.response?.data?.message || '请求失败')
    }
    return Promise.reject(err)
  }
)

export default request
