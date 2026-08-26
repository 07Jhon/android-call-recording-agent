# Android Call Recording Agent

Un système complet d'enregistrement des appels téléphoniques pour les téléphones serveurs d'entreprise avec synchronisation backend et tableau de bord d'administration.

## 📋 Spécification

- **Détection d'appels** : entrant, sortant, accepté, terminé, manqué
- **Enregistrement audio** : format WAV configurable
- **Stockage sécurisé** : chiffrement et gestion de la conservation
- **Upload automatique** : avec queue locale et retry
- **Dashboard** : gestion et consultation des enregistrements
- **Audit** : journalisation complète des accès

## 🏗️ Architecture

### Android
- `CallMonitorService` : Service de détection d'appels
- `CallRecordingManager` : Gestion de l'enregistrement
- `RecordingStorage` : Stockage local sécurisé
- `UploadQueue` : File d'attente locale
- `RecordingUploader` : Upload automatique

### Backend
- `CallRecordingController` : API REST
- `CallRecordingService` : Logique métier
- `CallRecordingRepository` : Accès données
- `StorageService` : Gestion fichiers

## 📊 Statuts

```
RECORDING → PROCESSING → PENDING_UPLOAD → UPLOADING → UPLOADED
                                    ↓
                             UPLOAD_FAILED (retry)
                                    ↓
                                DELETED
```

## 🔐 Sécurité

- ✅ HTTPS/TLS
- ✅ Authentification des appareils
- ✅ Contrôle d'accès (RBAC)
- ✅ Chiffrement du stockage
- ✅ URLs temporaires
- ✅ Audit complet

## 📁 Structure du Projet

```
android-call-recording-agent/
├── android/              # Application Android
├── backend/              # Services Backend
├── database/             # Schéma PostgreSQL
├── docs/                 # Documentation
└── tests/                # Tests
```

## 🚀 Démarrage Rapide

Voir les dossiers spécifiques pour les instructions d'installation.

## 📝 Priorités

🔴 **ROUGE** : Critiques - Démarrer immédiatement
🟡 **ORANGE** : Importants - Suivre après
🟢 **VERT** : Optionnels - Plus tard
