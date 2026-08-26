import React, { useState } from 'react'
import { Play, Pause, Volume2, Download } from 'lucide-react'
import '../styles/AudioPlayer.css'

function AudioPlayer({ recordingId, fileName, onDownload }) {
  const [isPlaying, setIsPlaying] = useState(false)
  const [currentTime, setCurrentTime] = useState(0)
  const [duration, setDuration] = useState(0)
  const [volume, setVolume] = useState(1)
  const audioRef = React.useRef(null)

  const handlePlayPause = () => {
    if (audioRef.current) {
      if (isPlaying) {
        audioRef.current.pause()
      } else {
        audioRef.current.play()
      }
      setIsPlaying(!isPlaying)
    }
  }

  const handleTimeUpdate = () => {
    if (audioRef.current) {
      setCurrentTime(audioRef.current.currentTime)
    }
  }

  const handleLoadedMetadata = () => {
    if (audioRef.current) {
      setDuration(audioRef.current.duration)
    }
  }

  const handleProgressChange = (e) => {
    const newTime = parseFloat(e.target.value)
    setCurrentTime(newTime)
    if (audioRef.current) {
      audioRef.current.currentTime = newTime
    }
  }

  const formatTime = (time) => {
    if (isNaN(time)) return '0:00'
    const minutes = Math.floor(time / 60)
    const seconds = Math.floor(time % 60)
    return `${minutes}:${seconds.toString().padStart(2, '0')}`
  }

  return (
    <div className="audio-player">
      <audio
        ref={audioRef}
        onTimeUpdate={handleTimeUpdate}
        onLoadedMetadata={handleLoadedMetadata}
        onEnded={() => setIsPlaying(false)}
      />

      <div className="player-controls">
        <button className="play-btn" onClick={handlePlayPause}>
          {isPlaying ? <Pause size={24} /> : <Play size={24} />}
        </button>

        <div className="progress-container">
          <span className="time">{formatTime(currentTime)}</span>
          <input
            type="range"
            min="0"
            max={duration || 0}
            value={currentTime}
            onChange={handleProgressChange}
            className="progress-bar"
          />
          <span className="time">{formatTime(duration)}</span>
        </div>

        <div className="volume-control">
          <Volume2 size={20} />
          <input
            type="range"
            min="0"
            max="1"
            step="0.1"
            value={volume}
            onChange={(e) => {
              const vol = parseFloat(e.target.value)
              setVolume(vol)
              if (audioRef.current) {
                audioRef.current.volume = vol
              }
            }}
            className="volume-slider"
          />
        </div>

        <button className="download-btn" onClick={() => onDownload(recordingId)}>
          <Download size={20} />
        </button>
      </div>
    </div>
  )
}

export default AudioPlayer
