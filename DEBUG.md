# 🔧 Troubleshooting Guide

## Docker Build Errors

### Error: "failed to read dockerfile: open Dockerfile: no such file or directory"

**Solution:**
```bash
# Vérifier que les Dockerfiles existent
ls -la backend/Dockerfile
ls -la frontend/Dockerfile

# Reconstruire
docker-compose build --no-cache
```

## Logs & Debugging

```bash
# Vérifier les logs du backend
docker-compose logs backend -f

# Vérifier les logs de PostgreSQL
docker-compose logs postgres -f

# Vérifier les logs du frontend
docker-compose logs frontend -f

# Voir tous les logs
docker-compose logs -f
```

## Common Issues

### 1. Port already in use
```bash
# Trouver le processus utilisant le port
lsof -i :8080

# Arrêter le processus
kill -9 <PID>
```

### 2. Database connection error
```bash
# Vérifier que PostgreSQL est en cours d'exécution
docker ps | grep postgres

# Redémarrer PostgreSQL
docker-compose restart postgres
```

### 3. Frontend can't connect to backend
```bash
# Vérifier la configuration API
cat frontend/src/services/api.js

# Assurez-vous que API_BASE_URL pointe vers le bon backend
```

## Reset Everything

```bash
# Arrêter et supprimer tous les conteneurs
docker-compose down -v

# Supprimer les images
docker rmi android-call-recording-agent_backend android-call-recording-agent_frontend postgres

# Reconstruire et redémarrer
docker-compose up -d --build
```

## Access Services

```bash
# Backend Health Check
curl http://localhost:8080/api/v1/call-recordings/stats

# Database GUI
# http://localhost:8081
# Server: postgres
# Username: postgres
# Password: postgres

# Frontend
# http://localhost:5173 (development)
# http://localhost:80 (production)
```

## Performance Tuning

```yaml
# docker-compose.yml - Add resource limits
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
```
