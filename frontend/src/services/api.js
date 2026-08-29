import axios from 'axios'

/**
 * Client API pour le backend Spring Boot.
 * En dev, Vite proxifie /api vers http://localhost:8080 (voir vite.config.js).
 * En prod, nginx proxifie /api vers le conteneur backend (voir nginx.conf).
 */
const api = axios.create({
  baseURL: '/api/v1',
  timeout: 30000
})

export default api
