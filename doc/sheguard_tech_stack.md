# SheGuard Technology Stack Document

## 1. Overview
This document defines the exact technology stack, frameworks, SDK versions, APIs, and dependencies required to build the SheGuard mobile safety application. All versions are pinned to avoid compatibility issues and to ensure deterministic builds for the coding agent.

The stack is designed to support:
- Android mobile application
- Wear OS smartwatch integration
- AI / ML capability
- Real-time emergency communication
- Secure data storage

---

# 2. Platform

Primary Platform: Android

Minimum SDK Version: 24 (Android 7.0)

Target SDK Version: 34

Compile SDK Version: 34

Programming Language: Kotlin 1.9.22

Java Compatibility: Java 17

IDE: Android Studio Hedgehog 2023.1.1

Build System: Gradle 8.2

Android Gradle Plugin: 8.2.2

---

# 3. Core Android Libraries

AndroidX Core
Version: 1.12.0

AppCompat
Version: 1.6.1

Material Design Components
Version: 1.11.0

ConstraintLayout
Version: 2.1.4

Navigation Component
Version: 2.7.7

Lifecycle ViewModel
Version: 2.7.0

Lifecycle LiveData
Version: 2.7.0

---

# 4. Location Services

Google Play Services Location
Version: 21.0.1

Purpose:
Used for retrieving high accuracy GPS location for SOS alerts.

---

# 5. Messaging and Alerts

SMS Manager
Android Native Telephony API

Permissions Required:
SEND_SMS
READ_PHONE_STATE

Purpose:
Send emergency alerts to trusted contacts.

---

# 6. Wear OS Integration

Wear OS SDK
Version: 18.0.0

Play Services Wearable
Version: 18.0.0

Data Layer API
Used for communication between smartwatch and mobile device.

Capabilities:
- Send SOS trigger message
- Device connection management

---

# 7. Voice Recognition

Android SpeechRecognizer API

Library: Android Speech Service

Purpose:
Detect voice command "Help" on smartwatch.

Optional ML enhancement (future):
TensorFlow Lite
Version: 2.14.0

---

# 8. Evidence Recording

MediaRecorder API

Audio Format: 3GP

Encoding: AAC

Purpose:
Capture audio evidence during SOS activation.

---

# 9. Backend Services

Firebase Platform

Firebase Authentication
Version: 22.3.1

Firebase Firestore
Version: 24.9.1

Firebase Storage
Version: 20.3.0

Purpose:
User authentication, data storage, evidence storage.

---

# 10. Machine Learning Libraries

Python Environment (if server ML used)

Python Version: 3.10

NumPy 1.26.4

Pandas 2.1.4

Scikit-learn 1.4.0

TensorFlow 2.14.0

OpenCV 4.8.1

Purpose:
Support AI-based models such as cancer detection modules.

---

# 11. Networking

Retrofit
Version: 2.9.0

OkHttp
Version: 4.12.0

Gson Converter
Version: 2.9.0

Purpose:
API communication if backend endpoints are used.

---

# 12. Dependency Injection

Hilt (Dagger Hilt)
Version: 2.48

Purpose:
Manage dependency injection across modules.

---

# 13. Background Services

WorkManager
Version: 2.9.0

Purpose:
Handle background tasks such as uploading evidence recordings.

---

# 14. Permissions

Required Android Permissions:

ACCESS_FINE_LOCATION
SEND_SMS
RECORD_AUDIO
INTERNET
FOREGROUND_SERVICE
BODY_SENSORS (optional for wearables)
BLUETOOTH_CONNECT

---

# 15. Security

Encryption Library
AndroidX Security Crypto
Version: 1.1.0-alpha06

Purpose:
Encrypt sensitive user data.

---

# 16. Testing Frameworks

JUnit
Version: 4.13.2

Espresso
Version: 3.5.1

Mockito
Version: 5.5.0

---

# 17. Continuous Integration

GitHub Actions

Pipeline Steps:
- Build APK
- Run Unit Tests
- Static Code Analysis

---

# 18. Version Control

Git

Repository Hosting: GitHub

Branch Strategy:
- main
- development
- feature/*

---

# 19. Build Output

Primary Output: Android APK

Optional Output: Android App Bundle (AAB)

---

# 20. Future Tech Expansion

Potential additions:

AI threat detection

Wearable fall detection

Real-time video streaming

Police emergency API integration
