import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Download, Trash2, Copy } from 'lucide-react'
import api from '../services/api'
import AudioPlayer from '../components/AudioPlayer'
import '../styles/RecordingDetails.css'

function RecordingDetails() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [recording, setRecording] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [copied, setCopied] = useState(false)

  useEffect(() => {
    fetchRecording()
  }, [id])

  const fetchRecording = async () => {
    try {
      const response = await api.get(`/call-recordings/${id}`)
      setRecording(response.data)
      setError(null)
    } catch (err) {
      setError('Failed to fetch recording: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleDownload = async (recordingId) => {
    try {
      const response = await api.get(`/call-recordings/${recordingId}/download`, {
        responseType: 'blob'
      })
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', recording.fileName)
      document.body.appendChild(link)
      link.click()
      link.parentElement.removeChild(link)
    } catch (err) {
      setError('Failed to download: ' + err.message)
    }
  }

  const handleDelete = async () => {
    if (confirm('Are you sure you want to delete this recording?')) {
      try {
        await api.delete(`/call-recordings/${id}`)
        navigate('/')
      } catch (err) {
        setError('Failed to delete: ' + err.message)
      }
    }
  }

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  if (loading) return <div className="loading">Loading...</div>
  if (error) return <div className="error">{error}</div>
  if (!recording) return <div className="no-data">Recording not found</div>

  return (
    <div className="recording-details">
      <button className="back-btn" onClick={() => navigate('/')}>
        <ArrowLeft size={20} /> Back
      </button>

      <div className="details-header">
        <h1>{recording.phoneNumber}</h1>
        <span className={`status status-${recording.status.toLowerCase()}`}>
          {recording.status}
        </span>
      </div>

      <div className="details-content">
        {recording.storageKey && (
          <AudioPlayer
            recordingId={recording.id}
            fileName={recording.fileName}
            onDownload={handleDownload}
          />
        )}

        <div className="details-grid">
          <div className="detail-card">
            <h3>📱 Call Information</h3>
            <div className="detail-row">
              <span className="label">Phone Number:</span>
              <div className="value-group">
                <span>{recording.phoneNumber}</span>
                <button 
                  className="copy-btn"
                  onClick={() => copyToClipboard(recording.phoneNumber)}
                  title="Copy"
                >
                  <Copy size={16} />
                </button>
              </div>
            </div>
            <div className="detail-row">
              <span className="label">Call Type:</span>
              <span>{recording.callType}</span>
            </div>
            <div className="detail-row">
              <span className="label">Duration:</span>
              <span>{recording.duration ? Math.floor(recording.duration / 1000) + 's' : 'N/A'}</span>
            </div>
          </div>

          <div className="detail-card">
            <h3>🔧 Technical Details</h3>
            <div className="detail-row">
              <span className="label">Device ID:</span>
              <div className="value-group">
                <span className="code">{recording.deviceId}</span>
                <button 
                  className="copy-btn"
                  onClick={() => copyToClipboard(recording.deviceId)}
                  title="Copy"
                >
                  <Copy size={16} />
                </button>
              </div>
            </div>
            <div className="detail-row">
              <span className="label">File Size:</span>
              <span>{recording.fileSize ? (recording.fileSize / 1024 / 1024).toFixed(2) + ' MB' : 'N/A'}</span>
            </div>
            <div className="detail-row">
              <span className="label">Storage Key:</span>
              <span className="code" style={{fontSize: '0.85rem'}}>{recording.storageKey || 'N/A'}</span>
            </div>
          </div>

          <div className="detail-card">
            <h3>⏰ Timestamps</h3>
            <div className="detail-row">
              <span className="label">Started:</span>
              <span>{new Date(recording.startedAt).toLocaleString()}</span>
            </div>
            <div className="detail-row">
              <span className="label">Ended:</span>
              <span>{recording.endedAt ? new Date(recording.endedAt).toLocaleString() : 'N/A'}</span>
            </div>
            <div className="detail-row">
              <span className="label">Created:</span>
              <span>{new Date(recording.createdAt).toLocaleString()}</span>
            </div>
            {recording.uploadedAt && (
              <div className="detail-row">
                <span className="label">Uploaded:</span>
                <span>{new Date(recording.uploadedAt).toLocaleString()}</span>
              </div>
            )}
          </div>
        </div>

        {recording.status === 'UPLOAD_FAILED' && recording.lastUploadError && (
          <div className="error-detail">
            <h3>❌ Upload Error</h3>
            <p>{recording.lastUploadError}</p>
            <p className="attempts">Upload attempts: {recording.uploadAttempts}</p>
          </div>
        )}
      </div>

      <div className="action-buttons">
        {recording.storageKey && (
          <button className="btn btn-download" onClick={() => handleDownload(recording.id)}>
            <Download size={20} /> Download
          </button>
        )}
        <button className="btn btn-delete" onClick={handleDelete}>
          <Trash2 size={20} /> Delete
        </button>
      </div>
    </div>
  )
}

export default RecordingDetails
