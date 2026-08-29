import React, { useState, useEffect } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import RecordingDetails from './pages/RecordingDetails'
import Login from './pages/Login'
import { loadStoredCredentials } from './services/api'

function App() {
  const [authenticated, setAuthenticated] = useState(false)

  useEffect(() => {
    setAuthenticated(loadStoredCredentials())
  }, [])

  if (!authenticated) {
    return <Login onSuccess={() => setAuthenticated(true)} />
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/recordings/:id" element={<RecordingDetails />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
