<p align="center">
  <img src="app/src/main/res/drawable/img_logo.jpg" alt="Kaachu Phool Logo" width="180" style="border-radius: 28px; box-shadow: 0 10px 30px rgba(0,0,0,0.5);" />
</p>

<h1 align="center">🎴 Kaachu Phool (કાચુ ફૂલ)</h1>

<p align="center">
  <strong>Traditional Indian Trick-Taking Card Game with Ka-Chu-Fu-L Trump Rotation, Smart AI Bots, Real-Time Multiplayer & Physical Table Scorekeeper</strong>
</p>

<p align="center">
  <a href="https://github.com/patelhet0507/kacchooisdaphoolisda/releases"><img src="https://img.shields.io/github/v/release/patelhet0507/kacchooisdaphoolisda?style=for-the-badge&color=d4a843&label=Release" alt="Latest Release" /></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Platform-Android%207.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android 7.0+" /></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.2%2B-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" /></a>
  <a href="https://firebase.google.com"><img src="https://img.shields.io/badge/Backend-Firebase%20RTDB%20%26%20Auth-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase" /></a>
  <a href="https://github.com/patelhet0507/kacchooisdaphoolisda/actions"><img src="https://img.shields.io/github/actions/workflow/status/patelhet0507/kacchooisdaphoolisda/release-apk.yml?style=for-the-badge&label=CI%2FCD%20Build" alt="CI/CD Status" /></a>
</p>

---

<p align="center">
  <img src="app/src/main/res/drawable/img_hero_table.jpg" alt="Kaachu Phool Table Gameplay" width="850" style="border-radius: 16px; box-shadow: 0 12px 40px rgba(0,0,0,0.6);" />
</p>

## 📖 Table of Contents

- [🌟 Overview & Cultural Lore](#-overview--cultural-lore)
- [♠️ The Ka-Chu-Fu-L Trump Rotation](#️-the-ka-chu-fu-l-trump-rotation)
- [✨ Key Features](#-key-features)
  - [1. Single Player vs Adaptive AI Bots](#1-single-player-vs-adaptive-ai-bots)
  - [2. Real-Time Online Multiplayer & Voice Notes](#2-real-time-online-multiplayer--voice-notes)
  - [3. Digital Scorekeeper for Physical Games](#3-digital-scorekeeper-for-physical-games)
  - [4. Visual Customization & Achievements](#4-visual-customization--achievements)
  - [5. In-App Auto Update System](#5-in-app-auto-update-system)
  - [6. Companion Web Showcase](#6-companion-web-showcase)
- [📜 Rules of Kaachu Phool (How to Play)](#-rules-of-kaachu-phool-how-to-play)
  - [Round Progression & Card Distribution](#round-progression--card-distribution)
  - [The Hook Rule (*Bandi / Nasti*)](#the-hook-rule-bandi--nasti)
  - [Trick-Taking & Suit Precedence](#trick-taking--suit-precedence)
  - [Scoring Rules](#scoring-rules)
- [🏗️ Architecture & Technology Stack](#️-architecture--technology-stack)
- [📂 Repository Structure](#-repository-structure)
- [🚀 Quick Start & Installation](#-quick-start--installation)
  - [Option A: Install Pre-Built APK (Recommended)](#option-a-install-pre-built-apk-recommended)
  - [Option B: Build from Source](#option-b-build-from-source)
- [⚙️ Configuration & Secrets](#️-configuration--secrets)
- [🌐 Web Showcase & Landing Page](#-web-showcase--landing-page)
- [🤝 Contributing](#-contributing)
- [📄 License & Acknowledgments](#-license--acknowledgments)

---

## 🌟 Overview & Cultural Lore

**Kaachu Phool** (also widely known across India as *Kachuful*, *Kachhu Phool*, or *Judgement*) is a beloved traditional Indian trick-taking card game with roots deeply entrenched in Gujarati culture. 

Unlike conventional card games where players simply aim to accumulate as many tricks as possible, **Kaachu Phool tests pure tactical prediction and card discipline**:
- Players are dealt a varying number of cards each round (e.g., ascending from 1 up to 13 cards, and descending back down).
- Before a single card is played, each player must **bid** the exact number of tricks they foresee winning.
- Scoring is achieved **only** when your actual tricks won precisely equal your initial bid. Miss by even one trick, and you risk scoring zero or incurring harsh penalties!

This repository provides a **production-ready native Android application** powered by Jetpack Compose, an **in-memory and Firebase-backed real-time multiplayer engine**, an **intelligent bot AI engine**, a **standalone digital scorekeeper** for your family's real-life physical card nights, and an interactive **WebGL web showcase**.

---

## ♠️ The Ka-Chu-Fu-L Trump Rotation

The name **Kaachu Phool** is an acronym / mnemonic representing the strict cyclical order of the trump suits rotated across rounds:

| Mnemonic | Local Name (Gujarati) | International Suit | Symbol | Color | Round Cycle Position |
|:---:|:---:|:---:|:---:|:---:|:---:|
| **Ka** | **Kali (કાળી)** | Spades | `♠` | Black | Round `4k + 1` |
| **Chu** | **Chokat (ચોકટ)** | Diamonds | `♦` | Red | Round `4k + 2` |
| **Fu** | **Fuli (ફુલ્લી)** | Clubs | `♣` | Black | Round `4k + 3` |
| **L** | **Laal (લાલ)** | Hearts | `♥` | Red | Round `4k + 4` |

> [!NOTE]
> Every consecutive round advances through **Ka ➔ Chu ➔ Fu ➔ L ➔ Ka...**, keeping every hand dynamically unpredictable as trump dominance changes every deal.

---

## ✨ Key Features

### 1. Single Player vs Adaptive AI Bots
- **3 Distinct AI Difficulties**:
  - 🟢 **Easy**: Relaxed bidding, forgiving trick play, ideal for beginners learning suit mechanics.
  - 🟡 **Medium**: Balanced bidding with probabilistic card valuation and realistic delays.
  - 🔴 **Hard**: Master-level bots with card-counting memory, cut-throat trump preservation, and defensive play to sabotage opponents' bids.
- **5 Bot Personas**: Play alongside distinct opponents (*Aarav 🦁*, *Priya 🌸*, *Rohan ⚡*, *Meera 💎*, and *Vikram 🦅*).
- **100% Offline Capability**: Complete game loop works with zero internet access.

### 2. Real-Time Online Multiplayer & Voice Notes
- **Instant Room Creation**: Host a table and share an alphanumeric 6-character room code (e.g. `KP-7X9AB`).
- **Firebase Realtime Database Synchronization**: State synchronizes seamlessly across all connected devices using atomic database transactions.
- **Seat Auto-Fill**: Unfilled player slots can be automatically converted into smart bots so games can launch immediately without waiting.
- **Integrated Voice Notes**: Record and transmit voice audio clips directly at the card table (`AudioVoiceManager`), bringing the lively banter of real-world card nights to your phone.
- **Live Emote Reactions**: Send real-time emoji reactions (*Laugh, GG, Surprised, Thinking, Fire*) with floating animations.

### 3. Digital Scorekeeper for Physical Games
Playing with physical cards around the living room table with family? **Throw away the notepad and pen!**
- Dedicated **Digital Scorekeeper Mode** built directly into the app.
- Configures 2 to 8 custom player names.
- Automatically calculates the active trump suit for each round based on the Ka-Chu-Fu-L sequence.
- Shows the current dealer and automatically enforces the **Hook / Bandi Rule** on the dealer's bid.
- Real-time round tallying, cumulative leaderboard, and match history saved to local SQLite via Android Room.

### 4. Visual Customization & Achievements
- **5 Felt Table Themes**:
  - 🌲 *Classic Emerald* (Traditional card club green)
  - 🍷 *Royal Crimson* (Velvet casino felt)
  - 🌌 *Midnight Blue* (Cosmic evening felt)
  - ☀️ *Golden Sunburst* (Warm amber felt)
  - 🔮 *Neon Amethyst* (Vibrant cyberpunk purple)
- **6 Collectible Avatars**: Unlock Royal Lion 🦁, Cunning Fox 🦊, Golden Dragon 🐉, Emperor Crown 👑, Fierce Tiger 🐯, and Storm Lightning ⚡.
- **8 In-Game Achievements**: Track milestones including *First Victory*, *Kaachu Master (10 Wins)*, *Perfect Game (100% Accuracy)*, *Hook Master*, and *Socialite*.

### 5. In-App Auto Update System
- Integrated `AppUpdateManager` connects directly to GitHub Releases API.
- Non-intrusive in-app banner notifies players whenever a new version is published.
- Downloads the APK in the background via Android's native `DownloadManager` and triggers one-tap installation.

### 6. Companion Web Showcase
- Includes a standalone, mobile-responsive **HTML5 / TailwindCSS / GSAP landing page** (`index.html`) and **Flutter Web application** (`flutter_app/`) featuring:
  - Interactive 3D WebGL background.
  - Interactive card fan animation illustrating the four suits.
  - Live GitHub API integration fetching download links for the latest release APK.

---

## 📜 Rules of Kaachu Phool (How to Play)

```
       [ DEALT CARDS ]
              │
              ▼
    ┌──────────────────┐
    │  Bidding Phase   │ ◄── Dealer cannot bid "Hook" number (Sum of bids ≠ Total cards)
    └─────────┬────────┘
              │
              ▼
    ┌──────────────────┐
    │  Playing Tricks  │ ◄── Must follow lead suit if possible; else trump or slough
    └─────────┬────────┘
              │
              ▼
    ┌──────────────────┐
    │  Scoring Phase   │ ◄── Exact match: +10 + Bid | Missed: 0 or Penalty
    └─────────┬────────┘
              │
              ▼
    [ Next Round (Ka ➔ Chu ➔ Fu ➔ L) ]
```

### Round Progression & Card Distribution
Kaachu Phool can be played across 7 configurable game modes:
1. **Quick Match (1 ➔ 5 ➔ 1)**: 9 rounds (fast-paced 15-minute game).
2. **Classic Standard (1 ➔ 8 ➔ 1)**: 15 rounds (standard competitive length).
3. **Full Ladder (1 ➔ 10 ➔ 1)**: 19 rounds.
4. **Grand Ladder (1 ➔ 13 ➔ 1)**: 25 rounds (traditional tournament format).
5. **Ascending 8 (1 ➔ 8)**: 8 rounds ascending.
6. **Ascending 5 (1 ➔ 5)**: 5 rounds ascending.
7. **Single Drop (8 ➔ 1)**: 8 rounds descending.

### The Hook Rule (*Bandi / Nasti*)
To ensure that **not everyone can win their bid simultaneously**, the dealer is constrained:
$$\text{Forbidden Dealer Bid} = \text{Cards Dealt in Round} - \sum (\text{Bids of all other players})$$

> [!IMPORTANT]
> If 5 cards are dealt, and the first 3 players bid 1, 2, and 1 (sum = 4), the dealer **is forbidden from bidding 1**, because $4 + 1 = 5$. At least one player at the table is guaranteed to fail their bid!

### Trick-Taking & Suit Precedence
1. The player to the dealer's left plays the first card (Lead Suit).
2. All players **must follow suit** if they hold a card of the lead suit.
3. If a player has no cards in the lead suit, they may:
   - Play a **Trump card** (Ka-Chu-Fu-L suit) to cut and potentially win the trick.
   - Discard/slough any off-suit card.
4. The trick is awarded to:
   - The **highest Trump card** played (`A > K > Q > J > 10 > ... > 2`).
   - If no trump was played, the **highest card of the Lead Suit** wins.

### Scoring Rules
Choose between 3 distinct scoring formats in the lobby settings:
- **Standard (Traditional)**: 
  - Exact Bid hit: `10 + Bid` points (e.g. Bid 3 made = 13 pts, Bid 0 made = 10 pts).
  - Missed Bid: `0` points.
- **Penalty (High Stakes)**:
  - Exact Bid hit: `10 + Bid` points.
  - Missed Bid: `-10 - Bid` points (e.g. Bid 3 missed = -13 pts).
- **Bonus (Progressive)**:
  - Earn `1 point` per trick won + `10 points` bonus for hitting exact bid.

---

## 🏗️ Architecture & Technology Stack

The project adheres to Google's recommended Modern Android Architecture (MVVM + Unidirectional Data Flow):

```
┌────────────────────────────────────────────────────────┐
│               Jetpack Compose UI Layer                 │
│  HomeScreen • GamePlayScreen • ScorecardScreen • Lobby │
└──────────────────────────┬─────────────────────────────┘
                           │ StateFlow / Events
┌──────────────────────────▼─────────────────────────────┐
│                   ViewModel Layer                      │
│   GameViewModel • ScorecardViewModel • MultiplayerVM   │
└────────────┬─────────────────────────────┬─────────────┘
             │                             │
┌────────────▼─────────────┐ ┌─────────────▼─────────────┐
│    Game Core Engine      │ │       Data / Sync Layer   │
│   KaachuPhoolEngine      │ │  Firebase Realtime DB     │
│   SoundEffectsManager    │ │  Room DB (SQLite Local)   │
│   AudioVoiceManager      │ │  UserProfile / AppUpdate  │
└──────────────────────────┘ └───────────────────────────┘
```

| Layer | Technologies Used |
|---|---|
| **Language & Toolchain** | Kotlin 2.2.10, Java 17, Gradle 9.3+ |
| **UI Framework** | Jetpack Compose (BOM 2024.09), Material 3, Material Icons Extended |
| **Concurrency & Async** | Kotlin Coroutines (`1.10.2`), `StateFlow`, `callbackFlow` |
| **Local Persistence** | Android Room 2.7.0 (`KaachuPhoolDatabase`, DAOs, Entities) |
| **Cloud & Realtime Sync**| Firebase Realtime Database (`firebase-database`), Firebase AppCheck |
| **Authentication** | Firebase Auth (`firebase-auth`), Android Credential Manager & Google ID |
| **Networking & HTTP** | OkHttp 4.10, Retrofit 2.12, Moshi 1.15 |
| **Media & Audio** | Android `MediaRecorder` & `MediaPlayer` (AAC/M4A), synthetic tone generator |
| **CI / CD Pipeline** | GitHub Actions (`release-apk.yml`), Temurin JDK 17, automated signing |
| **Web Presentation** | HTML5, TailwindCSS, GSAP, WebGL Canvas, Flutter Web |

---

## 📂 Repository Structure

```text
kacchooisdaphoolisda/
├── .github/
│   └── workflows/
│       └── release-apk.yml         # Automated GitHub Actions CI/CD for release APK
├── app/
│   ├── build.gradle.kts            # App module configuration & dependency catalog
│   └── src/main/
│       ├── AndroidManifest.xml     # Permissions, activities, receivers
│       ├── java/com/example/
│       │   ├── MainActivity.kt     # Root Activity & navigation host
│       │   ├── auth/               # Google Credential Manager & Phone Auth
│       │   │   ├── AuthManager.kt
│       │   │   └── AuthResultHandler.kt
│       │   ├── data/               # Room DB, DAOs, User Profile & Settings
│       │   │   ├── KaachuPhoolDatabase.kt
│       │   │   ├── ScorecardDao.kt / ScorecardEntities.kt
│       │   │   ├── MatchHistoryDao.kt / MatchHistoryEntity.kt
│       │   │   └── UserProfileManager.kt
│       │   ├── engine/             # Core game logic, bot AI & audio managers
│       │   │   ├── KaachuPhoolEngine.kt
│       │   │   ├── RoomManager.kt (Firebase sync & live rooms)
│       │   │   ├── AudioVoiceManager.kt
│       │   │   ├── SoundEffectsManager.kt
│       │   │   └── GameDiagnosticLogger.kt
│       │   ├── model/              # Cards, Suits, Achievements, Table Themes
│       │   │   ├── Card.kt
│       │   │   ├── GameModels.kt
│       │   │   ├── TableRoom.kt
│       │   │   └── Customizations.kt
│       │   ├── ui/                 # Jetpack Compose screens and components
│       │   │   ├── screens/        # GamePlay, Home, Lobby, Scorecard, Rules
│       │   │   ├── components/     # Cards, TrumpIndicator, Bidding, Dialogs
│       │   │   └── theme/          # Color palettes, Felt surfaces, Typography
│       │   ├── update/             # AppUpdateManager (GitHub release updater)
│       │   └── viewmodel/          # GameViewModel, MultiplayerVM, ScorecardVM
│       └── res/                    # Drawables, mipmaps, strings, colors
├── flutter_app/                    # Companion Flutter Web application
│   ├── lib/main.dart
│   └── pubspec.yaml
├── index.html                      # Interactive Web showcase with WebGL & Tailwind
├── gradle/                         # Gradle wrapper & version catalogs (libs.versions.toml)
├── build.gradle.kts                # Root project build configuration
├── settings.gradle.kts             # Module inclusions & plugin management
└── .env.example                    # Environment variable template
```

---

## 🚀 Quick Start & Installation

### Option A: Install Pre-Built APK (Recommended)

1. Navigate to the [Releases](https://github.com/patelhet0507/kacchooisdaphoolisda/releases) page on your Android device.
2. Download the latest `kaachu-phool-vX.X.X.apk` (or use the built-in APKs `apk19.apk` / `apk20.apk` included in this repository).
3. Tap the downloaded file to install (*enable "Install from unknown sources" if prompted*).
4. Launch **Kaachu Phool** and start playing!

### Option B: Build from Source

#### Prerequisites
- **Android Studio** (Ladybug 2024.2.1+ or Meerkat recommended)
- **JDK 17** (Amazon Corretto or Eclipse Temurin)
- **Android SDK** (API Level 36, minimum API Level 24)

#### 1. Clone the repository
```bash
git clone https://github.com/patelhet0507/kacchooisdaphoolisda.git
cd kacchooisdaphoolisda
```

#### 2. Open in Android Studio or Build via CLI
To build the debug APK directly from your terminal:

**Windows (PowerShell):**
```powershell
.\gradlew.bat assembleDebug
```

**macOS / Linux:**
```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

The compiled APK will be located at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

#### 3. Install to Connected Device / Emulator
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚙️ Configuration & Secrets

### 1. Firebase Setup (For Online Multiplayer & Auth)
The project includes default test Firebase configurations. For custom production deployments:
1. Create a project in the [Firebase Console](https://console.firebase.google.com).
2. Enable **Realtime Database** and **Authentication** (Google Sign-In, Phone Auth, Anonymous).
3. Download `google-services.json` and place it inside `app/google-services.json`.

### 2. Secrets & Environment Variables (`.env`)
Copy `.env.example` to `.env` in the root directory:
```bash
cp .env.example .env
```
Available environment properties:
- `GEMINI_API_KEY`: *(Optional)* API key for AI Studio / server-side Gemini features.
- `KEYSTORE_PATH`: Path to your release keystore `.jks` file.
- `STORE_PASSWORD`: Keystore master password.
- `KEY_ALIAS`: Key alias name.
- `KEY_PASSWORD`: Key password.

---

## 🌐 Web Showcase & Landing Page

Want to preview or host the web landing page?

- **HTML5 Showcase**: Simply open `index.html` in any modern web browser or serve it via:
  ```bash
  # Python 3 built-in server
  python -m http.server 8080
  ```
  Visit `http://localhost:8080` to experience the 3D card tilt animation, game guide, and download portal.

- **Flutter Web App**:
  ```bash
  cd flutter_app
  flutter pub get
  flutter run -d chrome
  ```

---

## 🤝 Contributing

Contributions, bug reports, and suggestions are warmly welcomed!

1. **Fork the Repository**
2. **Create a Feature Branch**: `git checkout -b feat/my-awesome-feature`
3. **Commit your Changes**: `git commit -m 'feat: add support for 6-player table rotation'`
4. **Push to the Branch**: `git push origin feat/my-awesome-feature`
5. **Open a Pull Request**

Please follow standard Kotlin style guidelines and ensure any additions to game logic have corresponding diagnostic test coverage in `GameDiagnosticLogger`.

---

## 📄 Acknowledgments


- **Cultural Heritage**: Dedicated to traditional Gujarati and Indian card gaming traditions preserved across generations.
- **Card Assets & Design**: Inspired by traditional cloth table card rooms and modern Material 3 aesthetics.

<p align="center">
  <sub>Made with ❤️ for card game lovers worldwide • ♠️ ♦️ ♣️ ♥️</sub>
</p>
