# 🛡️ Coach Anti-Arnaque

Application Android destinée aux aînés francophones pour détecter les SMS frauduleux.

## Fonctionnalités

- Analyse automatique des SMS entrants
- Analyse manuelle par copier-coller
- Détection basée sur des règles (sans IA) :
  - Mots-clés suspects
  - Liens et raccourcisseurs d'URL
  - Numéros inconnus / frauduleux
  - Sentiment d'urgence
  - Demande d'informations personnelles
- Résultat clair : 🟢 Sécuritaire / 🟠 Attention / 🔴 Arnaque probable
- Historique des 10 derniers messages
- Partage avec un proche (SMS, WhatsApp, email)
- Text-to-Speech (lecture vocale du résultat)
- Mode sombre automatique
- Interface ultra-accessible (gros texte, boutons larges, contraste élevé)

## Stack technique

- Kotlin + Jetpack Compose
- Architecture MVVM (ViewModel + StateFlow)
- Room (base de données locale)
- Retrofit (appels API)
- Coroutines
- Navigation Compose

## Configuration des APIs

Dans `app/build.gradle.kts`, remplacez les clés API :

```kotlin
buildConfigField("String", "GOOGLE_SAFE_BROWSING_API_KEY", "\"VOTRE_CLE\"")
buildConfigField("String", "VIRUSTOTAL_API_KEY", "\"VOTRE_CLE\"")
```

### Google Safe Browsing
1. Créez un projet sur [Google Cloud Console](https://console.cloud.google.com)
2. Activez l'API Safe Browsing
3. Créez une clé API

### VirusTotal
1. Créez un compte sur [VirusTotal](https://www.virustotal.com)
2. Récupérez votre clé API dans votre profil

> L'application fonctionne sans ces clés (détection locale uniquement).

## Permissions requises

- `READ_SMS` — Lire les SMS pour analyse
- `RECEIVE_SMS` — Analyser automatiquement les SMS entrants
- `READ_CONTACTS` — Identifier les numéros connus
- `INTERNET` — Appels aux APIs de vérification

## Compilation

1. Ouvrez le projet dans Android Studio
2. Synchronisez Gradle
3. Lancez sur un appareil ou émulateur (API 26+)

## Structure du projet

```
com.coachantiarnaque/
├── ui/
│   ├── screens/        # Écrans Compose
│   ├── components/     # Composants réutilisables
│   ├── navigation/     # Navigation
│   ├── theme/          # Thème et couleurs
│   └── MainActivity.kt
├── viewmodel/          # ViewModels MVVM
├── data/
│   ├── local/          # Room (DB locale)
│   ├── api/            # Services Retrofit
│   ├── repository/     # Repository
│   └── receiver/       # BroadcastReceiver SMS
└── domain/
    ├── model/          # Modèles de données
    └── engine/         # Moteur de détection
```
