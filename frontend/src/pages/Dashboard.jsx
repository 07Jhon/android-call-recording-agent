import React, { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Search,
  RefreshCw,
  Trash2,
  Eye,
  Download,
  X,
  PhoneIncoming,
  PhoneOutgoing,
  PhoneMissed,
  PhoneOff,
  Radio
} from 'lucide-react'
import { recordingsApi } from '../services/api'
import '../styles/Dashboard.css'

const STATUS_OPTIONS = [
  { value: '', label: 'Tous les statuts' },
  { value: 'RECORDING', label: 'En cours' },
  { value: 'PROCESSING', label: 'Traitement' },
  { value: 'PENDING_UPLOAD', label: "En attente d'envoi" },
  { value: 'UPLOADING', label: 'Envoi en cours' },
  { value: 'UPLOADED', label: 'Envoyé' },
  { value: 'UPLOAD_FAILED', label: 'Échec' },
  { value: 'DELETED', label: 'Supprimé' }
]

const CALL_TYPE_ICON = {
  INCOMING: PhoneIncoming,
  OUTGOING: PhoneOutgoing,
  MISSED: PhoneMissed,
  REJECTED: PhoneOff
}

const PAGE_SIZE = 10

function formatDuration(ms) {
  if (!ms && ms !== 0) return '—'
  const totalSeconds = Math.floor(ms / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

function formatStatusLabel(status) {
  return status.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase())
}

function StatCard({ label, value, active, onClick, tone }) {
  return (
    <button className={`stat-card ${active ? 'stat-card-active' : ''}`} onClick={onClick} style={{ '--tone': tone }}>
      <span className="stat-value">{value ?? '—'}</span>
      <span className="stat-label">{label}</span>
    </button>
  )
}

function Dashboard() {
  const navigate = useNavigate()

  const [recordings, setRecordings] = useState([])
  const [stats, setStats] = useState(null)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [page, setPage] = useState(0)

  const [deviceId, setDeviceId] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [status, setStatus] = useState('')

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [deletingId, setDeletingId] = useState(null)

  const fetchStats = useCallback(async () => {
    try {
      const res = await recordingsApi.stats()
      setStats(res.data)
    } catch (err) {
      // Stats are a nice-to-have; don't block the table on failure.
      console.error('Failed to load stats:', err.message)
    }
  }, [])

  const fetchRecordings = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await recordingsApi.list({ deviceId, phoneNumber, status, page, size: PAGE_SIZE })
      setRecordings(res.data.content || [])
      setTotalPages(res.data.totalPages ?? 0)
      setTotalElements(res.data.totalElements ?? 0)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [deviceId, phoneNumber, status, page])

  useEffect(() => {
    fetchStats()
  }, [fetchStats])

  useEffect(() => {
    fetchRecordings()
  }, [fetchRecordings])

  const handleFilterSubmit = (e) => {
    e.preventDefault()
    setPage(0)
    fetchRecordings()
  }

  const clearFilters = () => {
    setDeviceId('')
    setPhoneNumber('')
    setStatus('')
    setPage(0)
  }

  const handleStatCardClick = (value) => {
    setStatus(value)
    setPage(0)
  }

  const handleDelete = async (id) => {
    if (!confirm('Supprimer cet enregistrement ? Cette action est définitive.')) return
    setDeletingId(id)
    try {
      await recordingsApi.delete(id)
      await Promise.all([fetchRecordings(), fetchStats()])
    } catch (err) {
      setError(err.message)
    } finally {
      setDeletingId(null)
    }
  }

  const handleDownload = async (recording) => {
    try {
      const res = await recordingsApi.download(recording.id)
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', recording.fileName || `recording-${recording.id}.wav`)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
    } catch (err) {
      setError(err.message)
    }
  }

  const hasFilters = deviceId || phoneNumber || status

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <div>
          <h1>Enregistrements d'appels</h1>
          <p>Supervision et gestion des enregistrements synchronisés depuis les appareils.</p>
        </div>
        <button className="btn-refresh" onClick={() => { fetchRecordings(); fetchStats() }} title="Actualiser">
          <RefreshCw size={16} className={loading ? 'spin' : ''} />
          Actualiser
        </button>
      </div>

      <div className="stats-row">
        <StatCard label="Total" value={stats?.totalRecordings} active={status === ''} onClick={() => handleStatCardClick('')} tone="#4f46e5" />
        <StatCard label="En cours" value={stats?.recordingCount} active={status === 'RECORDING'} onClick={() => handleStatCardClick('RECORDING')} tone="#b91c1c" />
        <StatCard label="En attente d'envoi" value={stats?.pendingUploadCount} active={status === 'PENDING_UPLOAD'} onClick={() => handleStatCardClick('PENDING_UPLOAD')} tone="#475569" />
        <StatCard label="Envoyés" value={stats?.uploadedCount} active={status === 'UPLOADED'} onClick={() => handleStatCardClick('UPLOADED')} tone="#047857" />
        <StatCard label="Échecs" value={stats?.failedUploadCount} active={status === 'UPLOAD_FAILED'} onClick={() => handleStatCardClick('UPLOAD_FAILED')} tone="#dc2626" />
      </div>

      <form className="filters" onSubmit={handleFilterSubmit}>
        <div className="filter-field">
          <Search size={16} />
          <input
            placeholder="ID de l'appareil"
            value={deviceId}
            onChange={(e) => setDeviceId(e.target.value)}
          />
        </div>
        <div className="filter-field">
          <Search size={16} />
          <input
            placeholder="Numéro de téléphone"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
          />
        </div>
        <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0) }}>
          {STATUS_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
        <button type="submit" className="btn-primary">Filtrer</button>
        {hasFilters && (
          <button type="button" className="btn-ghost" onClick={clearFilters}>
            <X size={14} /> Effacer
          </button>
        )}
      </form>

      {error && <div className="error">{error}</div>}

      {!error && loading && recordings.length === 0 && <div className="loading">Chargement des enregistrements…</div>}

      {!error && !loading && recordings.length === 0 && (
        <div className="empty-state">
          <Radio size={28} />
          <h3>Aucun enregistrement trouvé</h3>
          <p>{hasFilters ? 'Essayez de modifier ou d\'effacer les filtres.' : 'Les enregistrements envoyés par les appareils apparaîtront ici.'}</p>
        </div>
      )}

      {recordings.length > 0 && (
        <div className="table-wrapper">
          <table className="recordings-table">
            <thead>
              <tr>
                <th>Appel</th>
                <th>Numéro</th>
                <th>Appareil</th>
                <th>Durée</th>
                <th>Statut</th>
                <th>Créé le</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {recordings.map((rec) => {
                const CallIcon = CALL_TYPE_ICON[rec.callType] || PhoneIncoming
                return (
                  <tr key={rec.id} onClick={() => navigate(`/recordings/${rec.id}`)} className="table-row-clickable">
                    <td>
                      <span className="call-type" title={rec.callType}>
                        <CallIcon size={16} />
                      </span>
                    </td>
                    <td className="mono">{rec.phoneNumber}</td>
                    <td className="mono text-muted">{rec.deviceId}</td>
                    <td>{formatDuration(rec.duration)}</td>
                    <td>
                      <span className={`status status-${rec.status.toLowerCase()}`}>
                        {formatStatusLabel(rec.status)}
                      </span>
                    </td>
                    <td className="text-muted">{new Date(rec.createdAt).toLocaleString('fr-FR')}</td>
                    <td className="row-actions" onClick={(e) => e.stopPropagation()}>
                      <button title="Voir le détail" onClick={() => navigate(`/recordings/${rec.id}`)}>
                        <Eye size={16} />
                      </button>
                      {rec.storageKey && (
                        <button title="Télécharger" onClick={() => handleDownload(rec)}>
                          <Download size={16} />
                        </button>
                      )}
                      <button
                        title="Supprimer"
                        className="danger"
                        disabled={deletingId === rec.id}
                        onClick={() => handleDelete(rec.id)}
                      >
                        <Trash2 size={16} />
                      </button>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      {totalPages > 1 && (
        <div className="pagination">
          <button disabled={page === 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>
            Précédent
          </button>
          <span>
            Page {page + 1} / {totalPages} · {totalElements} enregistrement{totalElements > 1 ? 's' : ''}
          </span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage((p) => p + 1)}>
            Suivant
          </button>
        </div>
      )}
    </div>
  )
}

export default Dashboard
