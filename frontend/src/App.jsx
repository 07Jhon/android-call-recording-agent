import React from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import RecordingDetails from './pages/RecordingDetails'

function App() {
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
