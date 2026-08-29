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

const CREDENTIALS_KEY = 'call_recorder_credentials'

export function setCredentials(username, password) {
  api.defaults.auth = { username, password }
  sessionStorage.setItem(CREDENTIALS_KEY, JSON.stringify({ username, password }))
}

export function loadStoredCredentials() {
  const raw = sessionStorage.getItem(CREDENTIALS_KEY)
  if (!raw) return false
  const { username, password } = JSON.parse(raw)
  api.defaults.auth = { username, password }
  return true
}

export function clearCredentials() {
  delete api.defaults.auth
  sessionStorage.removeItem(CREDENTIALS_KEY)
}

export default api
