# Rapport de correction — android-call-recording-agent

Toutes les corrections ci-dessous ont été **vérifiées par compilation/exécution réelle** :
`javac` (backend), `kotlinc` (Android), `npm run build` (frontend), et un vrai serveur
PostgreSQL 16 pour le schéma SQL.

## Backend (Java / Spring Boot)

| Fichier | Bug | Correction |
|---|---|---|
| `model/CallRecording.java` | `enum class CallType {...}` — syntaxe **Kotlin**, invalide en Java | Extrait dans `CallType.java` et `CallStatus.java`, `enum` public (un seul type public par fichier en Java) |
| `service/CallRecordingService.java` | Accolade fermante manquante dans `getRecordingsByPhoneNumber()` | Accolade ajoutée |
| `service/AuditService.java` | Variable locale `log` masquait le logger Lombok `@Slf4j` → `log.info(...)` ne compilait pas | Renommée en `auditLog` |
| `controller/CallRecordingController.java` | Endpoint `/upload` était un stub `TODO`, ne sauvegardait rien | Implémenté : sauvegarde fichier + enregistrement BDD via `CallRecordingService.processUpload()` |
| `controller/CallRecordingController.java` | Endpoint `GET /{id}/download` appelé par le frontend mais inexistant | Ajouté (+ `CallRecordingService.getFileContent()`) |
| `controller/CallRecordingController.java` | Aucun endpoint pour lister tous les enregistrements (nécessaire au Dashboard) | Ajouté `GET /api/v1/call-recordings` (paginé) |
| — | `@PreAuthorize` posé partout mais aucune config Spring Security → annotations silencieusement ignorées | Ajouté `config/SecurityConfig.java` avec `@EnableMethodSecurity`. **⚠️ L'authentification réelle des devices (token/JWT) reste à implémenter** — ce fichier active seulement le mécanisme d'autorisation par rôle. |

## Base de données (PostgreSQL)

| Fichier | Bug | Correction |
|---|---|---|
| `database/schema.sql` | Index `idx_device_id` créé deux fois (sur `call_recordings` et `registered_devices`) → le script entier échouait | Renommé en `idx_registered_devices_device_id` |

Vérifié avec un vrai PostgreSQL 16 : les 5 tables et tous les index se créent sans erreur.

## Android (Kotlin)

| Fichier | Bug | Correction |
|---|---|---|
| `CallMonitorService.kt`, `RecordingUploadService.kt` | Héritaient de `Service` mais utilisaient `lifecycleScope` (n'existe que sur `LifecycleService`) | Changé pour `LifecycleService`, `onBind`/`onStartCommand` appellent `super` correctement |
| `CallMonitorService.kt` | `TelephonyCallback()` instancié directement mais jamais défini (classe abstraite) | Implémenté `callStateCallback` avec `TelephonyCallback.CallStateListener` |
| `AndroidManifest.xml` | Permission `BIND_JOB_SERVICE` sur un service qui n'est pas un `JobService` | Retirée |
| `AndroidManifest.xml` | **Aucun foreground service** déclaré → crash après ~5s sur Android 8+ (`ForegroundServiceDidNotStartInTimeException` sur API 31+) | Ajouté permissions `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MICROPHONE`, `FOREGROUND_SERVICE_DATA_SYNC`, `POST_NOTIFICATIONS` + `foregroundServiceType` sur les deux services + `startForeground()` avec notification dans `CallMonitorService` |
| `AndroidManifest.xml` | Référençait `.ui.MainActivity`, `.receiver.BootReceiver`, `.receiver.CallStateReceiver` — **aucun n'existait** | Créés (implémentations fonctionnelles minimales) |
| — | **Aucun dossier `res/`** (thèmes, strings, icônes) alors que le manifest les référence | Créé : `strings.xml`, `themes.xml`, `backup_rules.xml`, `data_extraction_rules.xml`, icônes adaptatives |
| — | **Aucun scaffold Gradle racine** (`settings.gradle`, `build.gradle` racine, `proguard-rules.pro`) | Créés |
| `android/build.gradle` | Pas de `namespace` (requis par AGP 8+) | Ajouté `namespace 'com.enterprise.callrecorder'` |
| `AndroidManifest.xml` | Mal placé à `android/src/` au lieu de `android/src/main/` (emplacement standard Gradle) | Déplacé |
| `AndroidManifest.xml` | Attribut `package=` en conflit avec le `namespace` désormais déclaré en Gradle | Retiré |

**Non vérifié par compilation réelle** : `kotlinc` seul ne peut pas résoudre `androidx.*`/Dagger/Retrofit
sans le SDK Android complet (non disponible dans ce bac à sable, réseau restreint). Zéro erreur de
syntaxe détectée ; les erreurs restantes sont des cascades attendues de dépendances non résolues.
Une vraie compilation Gradle avec le SDK Android est recommandée avant mise en production.

## Frontend (React / Vite) — était un fragment incomplet, entièrement reconstitué

Il manquait : `package.json`, `vite.config.js`, `index.html`, point d'entrée (`main.jsx`),
`App.jsx` (routing), `services/api.js` (importé par `RecordingDetails.jsx` mais inexistant),
page `Dashboard.jsx`, et les CSS associés.

**Vérifié par build réel** : `npm install` + `npm run build` réussissent (`vite build` produit
`dist/` sans erreur), et `npm run dev` démarre correctement sur `http://localhost:5173`.

## Ce qui reste à faire (hors bug, fonctionnalité manquante)

- **Authentification réelle des devices** : `SecurityConfig.java` active le mécanisme
  `@PreAuthorize` mais ne définit aucune stratégie d'authentification (token API par device, JWT...).
  En l'état, Spring Security utilisera son mode par défaut (Basic Auth, mot de passe aléatoire
  loggé au démarrage) — inutilisable en production.
- **Chiffrement du stockage** : le README l'annonce, mais `StorageService.java` écrit les
  fichiers en clair sur disque.
- **Consentement légal à l'enregistrement d'appel** : selon les juridictions, enregistrer un
  appel sans consentement des deux parties peut être illégal. Le code ne gère aucune notification
  ni consentement.
