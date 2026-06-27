# SheGuard Backend Schema Document

## 1. Overview
This document defines the backend data architecture for the SheGuard application. It specifies the authentication flow, database structure, tables, fields, and relationships required to support all core application features including SOS alerts, trusted contacts, evidence collection, smartwatch triggers, and user settings.

The backend is designed to work with **Firebase services** for simplicity, scalability, and real-time synchronization. Firestore will be used as the primary database and Firebase Storage will handle media evidence files.

---

# 2. Backend Architecture

Client Layer
Android App (Kotlin)

Backend Services
Firebase Authentication
Firebase Firestore
Firebase Cloud Messaging (future)
Firebase Storage

Security
Firebase Security Rules

---

# 3. Authentication Flow

Supported authentication methods:

1. Phone Number + OTP
2. Email + Password

Authentication Steps:

User opens app

→ User enters phone/email

→ Firebase Authentication verifies credentials

→ User receives OTP (if phone login)

→ Upon verification, user record is created in Firestore

→ User proceeds to trusted contact setup

User Session:

Firebase manages session tokens automatically.

---

# 4. Database Structure (Firestore Collections)

Primary Collections:

users
trusted_contacts
sos_events
evidence_files
devices
settings

---

# 5. Users Collection

Collection Name
users

Document ID
user_id

Fields

user_id : string
full_name : string
phone_number : string
email : string
profile_image_url : string
created_at : timestamp
last_login : timestamp
is_active : boolean

Relationship

One user → many trusted contacts
One user → many SOS events

---

# 6. Trusted Contacts Collection

Collection Name
trusted_contacts

Document ID
contact_id

Fields

contact_id : string
user_id : string
contact_name : string
contact_phone : string
priority_level : integer
created_at : timestamp

Relationship

Many contacts → belong to one user

Used for:

SOS alerts
SMS notifications

---

# 7. SOS Events Collection

Collection Name
sos_events

Document ID
sos_event_id

Fields

sos_event_id : string
user_id : string
trigger_type : string
trigger_source : string
latitude : double
longitude : double
location_accuracy : float
created_at : timestamp
status : string

Trigger Types

manual_button
smartwatch_voice

Status Values

active
resolved
cancelled

Relationship

One SOS event → multiple evidence files

---

# 8. Evidence Files Collection

Collection Name
evidence_files

Document ID
evidence_id

Fields

evidence_id : string
sos_event_id : string
user_id : string
file_url : string
file_type : string
file_size : integer
recording_duration : integer
created_at : timestamp

File Types

audio
future_video
future_image

Files stored in
Firebase Storage

---

# 9. Devices Collection

Collection Name
devices

Document ID
device_id

Fields

device_id : string
user_id : string
device_type : string
os_version : string
connected_at : timestamp
last_sync : timestamp

Device Types

mobile
wear_os_watch

Purpose

Track smartwatch pairing for voice trigger feature.

---

# 10. Settings Collection

Collection Name
settings

Document ID
user_id

Fields

user_id : string
voice_trigger_enabled : boolean
fake_call_enabled : boolean
evidence_recording_enabled : boolean
location_sharing_enabled : boolean
created_at : timestamp

---

# 11. Database Relationships

users (1)

→ trusted_contacts (many)

users (1)

→ sos_events (many)

sos_events (1)

→ evidence_files (many)

users (1)

→ devices (many)

users (1)

→ settings (1)

---

# 12. SOS Event Data Flow

SOS Triggered

→ new record created in sos_events

→ location stored

→ SMS alerts sent

→ evidence recording started

→ evidence file metadata saved

---

# 13. Evidence Storage Structure

Firebase Storage Path

/evidence/{user_id}/{sos_event_id}/{file_name}

Example

/evidence/u123/sos456/audio_01.3gp

---

# 14. Security Rules

Users can read/write only their own data.

Rules:

request.auth.uid == user_id

Evidence files require authenticated access.

Trusted contacts cannot modify user account data.

---

# 15. Future Backend Extensions

Possible additions:

Live guardian tracking

Community safety alerts

Police emergency integration

AI threat prediction

