# MindCare — Mental Health & Cognitive Well-Being Native Android Application

A native Android mobile platform designed for mental wellness and stress management, combining positive psychology and neuroscience principles with modern Android development standards. The application provides an offline, privacy-first ecosystem featuring emotional self-reporting, guided mindfulness, box-breathing exercises, and gamified anti-stress mini-games.

---

## 📌 Features & Architecture

- **User Authentication & Session Management:** Decoupled login and registration flow utilizing local credentials, lifecycle-aware components, and persistent session state handling via `SharedPreferences` and `Room ORM`.
- **Guided Meditation Hub:** Two-tier catalog supporting multiple session routines (Beginners, Sleep, Anxiety Relief, Gratitude, Body Scan) powered by dynamic card generation following the DRY principle, immersive countdown timers, and post-session mood logging.
- **4-4-4 Box Breathing Guide:** Visual respiratory pacing via `ValueAnimator` scale transitions synchronized with stateful `CountDownTimer` callbacks to stimulate parasympathetic nervous system activation.
- **Personal Emotional Journal:** Structured CRUD journaling entries serialized with timestamp markers, mood tags, Android `ACTION_SEND` intent integration for external sharing, and non-destructive undo mechanisms using `Snackbar`.
- **Cognitive Anti-Stress Mini-Games:**
  - *Bubble Pop:* Continuous vertical object displacement with `ObjectAnimator` and memory-safe garbage collection.
  - *Memory Match (4x4):* Short-term memory retention board featuring input-locking flags (`isProcessing`) and state restoration across screen rotations (`onSaveInstanceState`).
  - *Color Match:* Stroop-effect cognitive flexibility challenge introducing intentional cognitive interference between semantic word meaning and font color.
- **Observer-Driven Gamification & Achievements:** Event-driven statistics engine persisting real-time streaks, high scores, and milestone unlocks (`UserStatsDao`) without redundant background polling.
- **Scheduled & Resilient Notifications:** Granular `Notification Channels` (Reminders, Motivation, Achievements) dispatched through `AlarmManager` and restored automatically across system reboots via an `ACTION_BOOT_COMPLETED` broadcast receiver.

---

## 🛠 Tech Stack & Android Components

- **Language:** Kotlin 1.9.20
- **Target Platform:** Android SDK API 24 to 34 (Android 7.0 Nougat to Android 14)
- **IDE & Build Tool:** Android Studio, Gradle 8.2 (Kotlin DSL)
- **Architecture:** Activity-Based Architecture, Helper Classes Pattern, Lifecycle Runtime KTX
- **UI & Layout:** ConstraintLayout, Material Design Components 1.11.0, Property Animation API (`ValueAnimator`, `ObjectAnimator`)
- **Asynchronous Execution:** Kotlin Coroutines 1.7.3 (`lifecycleScope`)
- **Data Persistence:** Room Database 2.6.1 (SQLite ORM with DAOs), SharedPreferences
- **System Services:** AlarmManager, NotificationManager, BroadcastReceiver (`BootReceiver`)

---

## 🔒 Privacy & Data Storage

MindCare is architected around a strict **local-first privacy model**. All personal reflections, mood logs, and game metrics are persisted locally within the device's sandbox (`Room` / `SharedPreferences`), eliminating external data transmission risks for sensitive mental health information.

---

## 👥 Developers & Academic Details

- **Institution:** Instituto Politécnico Nacional (IPN) — Escuela Superior de Cómputo (ESCOM)
- **Course:** Desarrollo de Aplicaciones Móviles Nativas (7CV2)
- **Advising Professor:** Sandra Luz Morales Guitrón
- **Developers:**
  - Isaac Pardo Gómez
  - Ernesto Jiménez Bernal
  - ---

## 📲 Download & Installation Guide (Android APK)

Follow these simple steps to install and run the standalone `.apk` build directly on your Android mobile device:

### Step 1: Download the Application
- Locate the **Releases** section on the right sidebar of this GitHub repository.
- Click on the latest release tag (e.g., `v1.0.0`).
- Under the **Assets** dropdown, download the file ending in `.apk` (e.g., `app-release.apk`) directly to your Android device[cite: 2].
  > *Alternatively: Download it onto your computer and transfer the `.apk` file to your phone's storage via USB or cloud drive.*

### Step 2: Enable Unknown App Installations
Android restricts sideloading apps outside Google Play by default. To proceed:
1. Open your device's **Settings** (`Ajustes`).
2. Navigate to **Security & Privacy** > **Install Unknown Apps** (or search for *"Install unknown apps"* in the settings search bar).
3. Find the browser or file manager you used to download the file (e.g., **Chrome** or **My Files / Mis Archivos**).
4. Toggle the switch to **Allow from this source** (`Permitir desde esta fuente`).

### Step 3: Run the Installer
1. Open your phone's **Files / Downloads** app and tap on the downloaded `.apk` file.
2. When prompted by the system dialog, tap **Install** (`Instalar`).
3. If **Google Play Protect** displays a warning (*"Unrecognized developer"*), tap **More details** (`Más detalles`) followed by **Install anyway** (`Instalar de todas formas`).

### Step 4: Open and Launch
- Once the installation finishes, tap **Open** (`Abrir`) or find the application icon on your home screen or app drawer to start using the app.
