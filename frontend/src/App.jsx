import React from 'react'
import { Routes, Route, Link } from 'react-router-dom'
import { PhoneCall } from 'lucide-react'
import Dashboard from './pages/Dashboard'
import RecordingDetails from './pages/RecordingDetails'
import './styles/App.css'

function App() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <Link to="/" className="app-brand">
          <span className="app-brand-icon">
            <PhoneCall size={20} />
          </span>
          <div>
            <span className="app-brand-title">Call Recording Agent</span>
            <span className="app-brand-subtitle">Console d'administration</span>
          </div>
        </Link>
      </header>

      <main className="app-content">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/recordings/:id" element={<RecordingDetails />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
