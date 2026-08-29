import React, { useState } from 'react'
import { Lock } from 'lucide-react'
import api, { setCredentials } from '../services/api'
import '../styles/Login.css'

function Login({ onSuccess }) {
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    // Tente les identifiants sur un endpoint protégé pour valider avant de continuer
    api.defaults.auth = { username, password }
    try {
      await api.get('/call-recordings/stats')
      setCredentials(username, password)
      onSuccess()
    } catch (err) {
      delete api.defaults.auth
      setError('Identifiants invalides')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <form className="login-card" onSubmit={handleSubmit}>
        <Lock size={28} />
        <h1>Connexion</h1>
        <p className="login-hint">
          Identifiants admin (par défaut : admin / changeme — à changer via
          ADMIN_USERNAME / ADMIN_PASSWORD)
        </p>
        <input
          type="text"
          placeholder="Nom d'utilisateur"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoFocus
        />
        <input
          type="password"
          placeholder="Mot de passe"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        {error && <div className="login-error">{error}</div>}
        <button type="submit" disabled={loading}>
          {loading ? 'Connexion...' : 'Se connecter'}
        </button>
      </form>
    </div>
  )
}

export default Login
