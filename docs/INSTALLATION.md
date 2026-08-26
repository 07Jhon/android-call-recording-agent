# Call Recording Agent - Installation & Usage Guide

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Git
- Node.js 18+ (for frontend)

### 1. Clone the Repository
```bash
git clone https://github.com/07Jhon/android-call-recording-agent.git
cd android-call-recording-agent
```

### 2. Start the Backend with Docker Compose
```bash
docker-compose up -d
```

This will start:
- **PostgreSQL** (port 5432)
- **Spring Boot Backend** (port 8080)
- **Adminer** (port 8081) - Database GUI

### 3. Verify Backend is Running
```bash
curl http://localhost:8080/api/v1/call-recordings/stats
```

### 4. Start the Frontend
```bash
cd frontend
npm install
npm run dev
```

Access at: `http://localhost:5173`

---

## 📱 Android App Setup

### Configure API Endpoint
Edit `android/build.gradle`:
```gradle
buildConfigField "String", "API_BASE_URL", "\"http://your-backend-ip:8080/api/v1\""
```

### Build & Run
```bash
cd android
./gradlew assembleDebug
```

---

## 🔐 Security Configuration

### 1. Database Credentials
Edit `.env` or `docker-compose.yml`:
```yaml
POSTGRES_USER: your_user
POSTGRES_PASSWORD: your_strong_password
```

### 2. API Authentication
The backend includes Spring Security with JWT tokens.

### 3. File Encryption
Recordings are stored securely with encryption.

---

## 📊 Accessing the Dashboard

### Web Dashboard
- **URL**: `http://localhost:5173`
- **Features**:
  - View all recordings
  - Search by phone number or device
  - Filter by status
  - Play recordings (if audio player installed)
  - Download recordings
  - Delete recordings
  - View analytics

### Database GUI (Adminer)
- **URL**: `http://localhost:8081`
- **Login**:
  - System: PostgreSQL
  - Server: postgres
  - Username: postgres
  - Password: postgres
  - Database: call_recordings

### Postman Testing
Import the collection: `docs/postman_collection.json`

**Example Requests:**

1. **Upload Recording**
```bash
curl -X POST http://localhost:8080/api/v1/call-recordings/upload \
  -F "deviceId=device-001" \
  -F "phoneNumber=+33612345678" \
  -F "callType=INCOMING" \
  -F "duration=120000" \
  -F "fileSize=1024000" \
  -F "file=@recording.m4a"
```

2. **Get All Recordings**
```bash
curl http://localhost:8080/api/v1/call-recordings/device/device-001?page=0&size=20
```

3. **Get Statistics**
```bash
curl http://localhost:8080/api/v1/call-recordings/stats
```

---

## 📝 Database Schema

The database includes:
- `call_recordings` - Main recordings table
- `audit_logs` - Audit trail
- `recording_access` - Access permissions
- `registered_devices` - Device information
- `upload_queue` - Retry queue

---

## 🛑 Stopping Services

```bash
# Stop all services
docker-compose down

# Remove volumes (careful!)
docker-compose down -v
```

---

## 🐛 Troubleshooting

### Backend not connecting to database
```bash
# Check PostgreSQL container
docker-compose logs postgres

# Restart services
docker-compose restart
```

### Frontend API errors
1. Check backend URL in `frontend/src/services/api.js`
2. Ensure backend is running: `curl http://localhost:8080/health`
3. Check CORS settings in backend

### Permission errors
```bash
# Fix file permissions
chmod -R 755 ./recordings
```

---

## 📞 Support
For issues, check the GitHub repository or contact the development team.
