# SheGuard Frontend Guidelines (Design System)

## 1. Overview
This document defines the complete visual design system for the SheGuard mobile application. The goal is to ensure the application maintains a consistent, modern, and visually appealing user interface across all screens while prioritizing usability during emergency situations.

The design philosophy combines:
- clarity during stressful moments
- fast interaction
- minimal cognitive load
- modern award-level mobile aesthetics

The UI should feel **clean, calm, and trustworthy**, while still highlighting emergency controls.

---

# 2. Design Principles

1. **Emergency First Design**
Critical actions such as the SOS button must always be visually dominant.

2. **Low Cognitive Load**
Users in emergencies should not need to read complex interfaces.

3. **High Contrast Accessibility**
Buttons and alerts must remain visible in low-light or panic scenarios.

4. **Minimal Interaction Path**
Emergency features must be accessible within **one tap**.

5. **Modern Mobile UI Aesthetics**
Inspired by award-winning product design similar to Apple Health, Uber, and modern fintech apps.

---

# 3. Color System

Primary Color

Emergency Red
HEX: #FF3B30
Usage:
SOS button, alerts, emergency indicators

Secondary Color

Safety Blue
HEX: #2563EB
Usage:
navigation, secondary buttons

Accent Color

Trust Green
HEX: #22C55E
Usage:
confirmation states, success indicators

Neutral Colors

Background Light
HEX: #F8FAFC

Surface White
HEX: #FFFFFF

Text Primary
HEX: #111827

Text Secondary
HEX: #6B7280

Divider
HEX: #E5E7EB

---

# 4. Typography System

Primary Font

Inter

Fallback Font

Roboto

Typography Scale

Heading 1
Size: 28sp
Weight: Bold

Heading 2
Size: 22sp
Weight: SemiBold

Heading 3
Size: 18sp
Weight: Medium

Body Text
Size: 16sp
Weight: Regular

Caption
Size: 13sp
Weight: Regular

Button Text
Size: 16sp
Weight: SemiBold

---

# 5. Spacing System

Spacing should follow an **8pt grid system**.

Spacing Tokens

4px  – micro spacing

8px  – base spacing

16px – standard spacing

24px – section spacing

32px – large spacing

48px – page margins

Example Layout

Screen Padding
24px left/right

Card Padding
16px

Component Gap
16px

---

# 6. Layout Structure

Use **ConstraintLayout or Compose layout** with responsive spacing.

Maximum Content Width

Mobile full width

Card Radius

16px

Button Radius

12px

SOS Button Radius

50% circular

---

# 7. SOS Button Design

The SOS button must be the **most visually dominant element**.

Specifications:

Diameter
120dp

Color
#FF3B30

Text
SOS

Text Color
#FFFFFF

Shadow
0dp 8dp 24dp rgba(255,59,48,0.4)

Interaction States

Default
Red

Pressed
Dark Red
HEX: #DC2626

---

# 8. Card Components

Used for:

Contacts
Safety tools
Device connection

Card Style

Background
#FFFFFF

Radius
16px

Elevation
4dp

Padding
16px

---

# 9. Navigation

Bottom Navigation Bar

Height
72dp

Icons
Material Icons

Icon Size
24dp

Active Color
#2563EB

Inactive Color
#9CA3AF

Tabs

Home
Contacts
Safety Tools
Settings

---

# 10. Fake Call Screen Design

Must closely mimic a real phone call UI.

Background
#111827

Caller Name
Large white text

Accept Button
Green
#22C55E

Reject Button
Red
#FF3B30

Button Diameter
72dp

---

# 11. Animation System

Animation Duration

Micro interaction
150ms

Screen transition
250ms

SOS activation pulse
600ms repeating

---

# 12. Iconography

Use Material Icons Rounded.

Icon Stroke
2dp

Icon Style
Filled for active
Outlined for inactive

---

# 13. Accessibility

Minimum Touch Target

48dp

Contrast Ratio

Minimum 4.5:1

Voice Feedback Support

Enabled for accessibility services

---

# 14. Dark Mode

Background
#0F172A

Surface
#1E293B

Text Primary
#F9FAFB

Text Secondary
#CBD5F5

SOS color remains
#FF3B30

---

# 15. Design Consistency Rules

Never place multiple primary actions on the same screen.

SOS must always remain visible on Home Dashboard.

Use consistent spacing across screens.

Avoid cluttered layouts.

---

# 16. Future UI Expansion

Potential additions:

Safety heatmap visualization

Live guardian tracking map

AI risk alerts

Emergency community alerts
