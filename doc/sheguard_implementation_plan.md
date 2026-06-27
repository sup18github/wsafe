# SheGuard Implementation Plan

## 1. Overview
This document defines the exact build sequence for implementing the SheGuard mobile application. The goal is to break the project into clear phases so an AI coding agent can implement the system step-by-step without ambiguity.

Each phase builds on top of the previous one and introduces a limited set of components to keep development stable and testable.

The implementation order prioritizes:
- application foundation
- authentication
- core safety functionality
- wearable integration
- supporting tools

---

# 2. Project Architecture Setup

Before building features, the base project structure must be created.

## Step 1: Create Android Project

Configuration

Application Name: SheGuard

Language: Kotlin

Minimum SDK: 24

Target SDK: 34

Architecture Pattern: MVVM

UI Framework: XML + ViewBinding (or Jetpack Compose if chosen globally)

Package Structure:

com.sheguard

├── ui
│   ├── auth
│   ├── home
│   ├── contacts
│   ├── fakecall
│   ├── sos
│   ├── evidence
│   └── settings

├── data
│   ├── repository
│   ├── model
│   └── datasource

├── services
│   ├── panic
│   ├── recording
│   └── watch

├── utils

└── di

---

# 3. Phase 1 – Core Application Setup

Goal:
Prepare application foundation.

Tasks:

1. Setup Gradle dependencies

2. Configure Firebase

3. Setup navigation framework

4. Create base theme and UI styles

5. Setup dependency injection (Hilt)

6. Create base activity

7. Setup ViewModel architecture

Deliverable:

App launches successfully.

---

# 4. Phase 2 – Authentication

Goal:
Allow users to create and access accounts.

Features:

Phone OTP login

Email login

Tasks:

Create Authentication Screens

Login Screen

OTP Verification Screen

Signup Screen

Implement Firebase Authentication

Create user document in Firestore

Navigate user to trusted contact setup

Deliverable:

User account creation works.

---

# 5. Phase 3 – Trusted Contacts System

Goal:
Allow users to store emergency contacts.

Tasks:

Create Contacts Screen

Add Contact

Remove Contact

Set priority contact

Save contacts in Firestore

Display contacts list

Deliverable:

User can manage trusted contacts.

---

# 6. Phase 4 – Home Dashboard

Goal:
Create the main control interface.

Components:

Large SOS Button

Quick Safety Tools

Watch Connection Status

Tasks:

Build Dashboard Layout

Implement navigation buttons

Connect dashboard to contact data

Deliverable:

User can access core features.

---

# 7. Phase 5 – Panic Button (SOS System)

Goal:
Enable emergency alerts.

Tasks:

Create PanicService

Fetch GPS location

Generate Google Maps link

Send SMS to trusted contacts

Create SOS event in Firestore

Navigate to SOS Active Screen

Deliverable:

SOS alerts successfully sent.

---

# 8. Phase 6 – Evidence Collection Module

Goal:
Automatically collect evidence during SOS.

Tasks:

Create RecordingService

Initialize MediaRecorder

Start audio recording when SOS triggered

Save file locally

Upload file to Firebase Storage

Create evidence record in Firestore

Deliverable:

Audio evidence recorded and saved.

---

# 9. Phase 7 – Fake Call Module

Goal:
Allow user to simulate incoming calls.

Tasks:

Create FakeCallActivity

Design incoming call UI

Add ringtone playback

Implement accept and reject buttons

Add delay scheduling option

Deliverable:

Fake call simulation works.

---

# 10. Phase 8 – Smartwatch Voice Trigger

Goal:
Enable SOS activation via smartwatch voice command.

Tasks:

Create Wear OS module

Implement SpeechRecognizer

Detect keyword "Help"

Send message using Wear Data Layer API

Implement WatchMessageListenerService on phone

Trigger PanicService when message received

Deliverable:

Voice trigger activates SOS.

---

# 11. Phase 9 – Evidence Storage System

Goal:
Ensure evidence files are safely stored.

Tasks:

Implement upload queue

Retry failed uploads

Create evidence list screen

Link files with SOS events

Deliverable:

Evidence files securely stored.

---

# 12. Phase 10 – Settings and Safety Controls

Goal:
Allow users to configure safety preferences.

Tasks:

Create Settings Screen

Enable or disable voice trigger

Enable or disable fake call

Enable or disable recording

Save preferences to Firestore

Deliverable:

User customization works.

---

# 13. Phase 11 – Background Reliability

Goal:
Ensure emergency features work even when the app is closed.

Tasks:

Convert PanicService to ForegroundService

Ensure recording continues in background

Allow watch trigger when app closed

Handle device sleep state

Deliverable:

SOS works reliably in background.

---

# 14. Phase 12 – Testing

Goal:
Validate system reliability.

Testing Types:

Unit Testing

UI Testing

Emergency scenario testing

Edge cases

No internet

Low battery

Watch disconnected

Deliverable:

Stable production-ready build.

---

# 15. Final Build

Generate release build.

Output:

APK

Android App Bundle

---

# 16. Future Enhancements

AI threat detection

Fall detection from smartwatch

Live location tracking

Police API integration

Community safety alerts
