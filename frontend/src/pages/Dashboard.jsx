import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Phone, PhoneIncoming, PhoneOutgoing, PhoneMissed, RefreshCw } from 'lucide-react'
import api from '../services/api'
import '../styles/Dashboard.css'

const CALL_TYPE_ICONS = {
  INCOMING: PhoneIncoming,
  OUTGOING: PhoneOutgoing,
  MISSED: PhoneMissed,
  REJECTED: PhoneMissed
}

function Dashboard() {
  const navigate = useNavigate()
  const [recordings, setRecordings] = useState([])
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  useEffect(() => {
    fetchData()
  }, [page])

  const fetchData = async () => {
    setLoading(true)
    try {
      const [recordingsRes, statsRes] = await Promise.all([
        api.get('/call-recordings', { params: { page, size: 20 } }),
        api.get('/call-recordings/stats')
      ])
      setRecordings(recordingsRes.data.content)
      setTotalPages(recordingsRes.data.totalPages)
      setStats(statsRes.data)
      setError(null)
    } catch (err) {
      setError('Impossible de charger les enregistrements: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1><Phone size={28} /> Call Recording Dashboard</h1>
        <button className="refresh-btn" onClick={fetchData} disabled={loading}>
          <RefreshCw size={18} className={loading ? 'spinning' : ''} /> Actualiser
        </button>
      </header>

      {stats && (
        <div className="stats-grid">
          <div className="stat-card">
            <span className="stat-value">{stats.totalRecordings}</span>
            <span className="stat-label">Total</span>
          </div>
          <div className="stat-card">
            <span className="stat-value">{stats.uploadedCount}</span>
            <span className="stat-label">Uploadés</span>
          </div>
          <div className="stat-card">
            <span className="stat-value">{stats.pendingUploadCount}</span>
            <span className="stat-label">En attente</span>
          </div>
          <div className="stat-card stat-error">
            <span className="stat-value">{stats.failedUploadCount}</span>
            <span className="stat-label">Échecs</span>
          </div>
        </div>
      )}

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <div className="loading">Chargement...</div>
      ) : (
        <>
          <table className="recordings-table">
            <thead>
              <tr>
                <th>Type</th>
                <th>Numéro</th>
                <th>Appareil</th>
                <th>Statut</th>
                <th>Démarré</th>
              </tr>
            </thead>
            <tbody>
              {recordings.length === 0 && (
                <tr>
                  <td colSpan={5} className="no-data">Aucun enregistrement</td>
                </tr>
              )}
              {recordings.map((recording) => {
                const Icon = CALL_TYPE_ICONS[recording.callType] || Phone
                return (
                  <tr key={recording.id} onClick={() => navigate(`/recordings/${recording.id}`)}>
                    <td><Icon size={18} /></td>
                    <td>{recording.phoneNumber}</td>
                    <td className="code">{recording.deviceId}</td>
                    <td>
                      <span className={`status status-${recording.status.toLowerCase()}`}>
                        {recording.status}
                      </span>
                    </td>
                    <td>{new Date(recording.startedAt).toLocaleString()}</td>
                  </tr>
                )
              })}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div className="pagination">
              <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>Précédent</button>
              <span>Page {page + 1} / {totalPages}</span>
              <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Suivant</button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

export default Dashboard
