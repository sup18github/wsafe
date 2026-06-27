# SheGuard Application Flow Document

## 1. Overview
This document defines the full navigation structure, page hierarchy, and user journeys of the SheGuard mobile application. The goal is to clearly describe how users move through the application and how each feature is triggered so that a coding agent can implement the correct UI structure and navigation logic.

The application is designed to minimize friction during emergency situations. Therefore, the most critical actions (SOS trigger, voice trigger, and fake call) must be accessible within one or two interactions.

---

# 2. High Level Navigation Structure

Main Navigation Structure:

1. Splash Screen
2. Onboarding Screens
3. Authentication
4. Home Dashboard
5. Trusted Contacts
6. Fake Call Setup
7. Safety Settings
8. SOS Active Screen
9. Evidence Recording Screen
10. Watch Integration Screen

Navigation Pattern:

Primary navigation uses a **bottom navigation bar** with quick access to:

- Home
- Contacts
- Safety Tools
- Settings

Critical emergency features (SOS button) remain **centered and persistent**.

---

# 3. Screen Flow

## 3.1 Splash Screen
Purpose:
Load application resources and check authentication state.

Flow:

App Launch → Splash Screen → Authentication Check

If user logged in:

→ Home Dashboard

If not logged in:

→ Onboarding

---

# 3.2 Onboarding Screens

Purpose:
Introduce core safety features.

Screens:

1. Welcome
2. Emergency SOS Feature
3. Voice Trigger via Smartwatch
4. Evidence Recording
5. Permissions Setup

User Action:

Tap "Get Started" → Authentication

---

# 3.3 Authentication

Authentication Options:

- Phone Number + OTP
- Email + Password

Flow:

Login Success → Setup Trusted Contacts

---

# 3.4 Trusted Contacts Setup

Purpose:
Register emergency contacts.

Actions:

- Add Contact
- Remove Contact
- Set Priority Contact

Flow:

After setup → Home Dashboard

---

# 3.5 Home Dashboard

Main Control Center.

Components:

Large SOS Button (center)

Quick Safety Actions:

- Trigger Fake Call
- Start Safety Recording
- Connect Smartwatch

Information Cards:

- Emergency contacts summary
- Device connection status

User Actions:

Press SOS → Activate Panic Service

Press Fake Call → Fake Call Screen

Press Watch Setup → Watch Integration

---

# 3.6 SOS Activation Flow

User presses SOS button.

System Actions:

1. Activate Panic Service
2. Fetch current GPS location
3. Send SMS alerts to trusted contacts
4. Start evidence recording
5. Display SOS Active Screen

---

# 3.7 SOS Active Screen

Displays during active emergency.

Information shown:

- Alert status
- Location sharing indicator
- Recording status

User Actions:

Stop SOS

Call Emergency Contact

---

# 3.8 Evidence Recording Screen

Activated automatically when SOS begins.

Functions:

- Audio recording
- Timestamp
- Location metadata

Recording stops when:

- SOS cancelled
- Time limit reached

---

# 3.9 Fake Call Screen

Purpose:
Allow users to escape uncomfortable situations.

Flow:

User presses Fake Call → Incoming Call UI appears.

Components:

Caller Name

Accept Button

Reject Button

Ringtone playback

---

# 3.10 Smartwatch Integration Screen

Purpose:
Connect Wear OS device.

Actions:

Pair device

Enable voice trigger

Test connection

Voice Trigger Flow:

User says "Help" → Watch detects keyword → Message sent to phone → Panic Service triggered

---

# 4. Core User Journeys

## Journey 1: Manual SOS

Open App

→ Home Dashboard

→ Press SOS Button

→ Alerts Sent

→ Recording Started

---

## Journey 2: Voice Trigger SOS

User speaks "Help" on smartwatch

→ Watch sends message to phone

→ Mobile app triggers Panic Service

→ SOS alerts sent

---

## Journey 3: Fake Call Escape

User opens app

→ Tap Fake Call

→ Incoming call simulation appears

---

# 5. Emergency Flow Summary

SOS Trigger

↓

Location Capture

↓

SMS Alert Sent

↓

Evidence Recording Started

↓

SOS Active Screen Displayed

---

# 6. Navigation Logic Rules

SOS screen overrides all screens.

Emergency recording must continue even if:

- app minimized
- screen locked

Watch-triggered SOS must activate Panic Service even if the app is closed.

---

# 7. Success Criteria

The app flow is successful if:

- Users can trigger SOS within 2 interactions
- Navigation remains simple during emergencies
- Watch voice trigger activates panic flow correctly

---

# 8. Future Navigation Extensions

Possible additions:

- Safety heatmap
- Live guardian tracking
- Community safety alerts

