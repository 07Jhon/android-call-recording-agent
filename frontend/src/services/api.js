import axios from 'axios'

// nginx.conf (prod) and vite.config.js (dev) both proxy /api -> the Spring Boot backend,
// so a relative base URL works in every environment.
const api = axios.create({
  baseURL: '/api/v1',
  timeout: 30000
})

// Centralized place to plug in an auth token later (JWT / session), once
// the backend's Spring Security configuration issues one.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ||
      error.response?.statusText ||
      error.message ||
      'Erreur inconnue'
    return Promise.reject(new Error(message))
  }
)

export const recordingsApi = {
  list: ({ deviceId, phoneNumber, status, page = 0, size = 20 } = {}) =>
    api.get('/call-recordings', {
      params: {
        deviceId: deviceId || undefined,
        phoneNumber: phoneNumber || undefined,
        status: status || undefined,
        page,
        size
      }
    }),

  getById: (id) => api.get(`/call-recordings/${id}`),

  delete: (id) => api.delete(`/call-recordings/${id}`),

  download: (id) => api.get(`/call-recordings/${id}/download`, { responseType: 'blob' }),

  stats: () => api.get('/call-recordings/stats')
}

export default api
