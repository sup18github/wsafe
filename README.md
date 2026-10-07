# ✨ WSafe
WSafe stands as the quintessential companion for women, ensuring their safety in every circumstance. Through its user-friendly features, it empowers you to swiftly alert your loved ones of your whereabouts and connect with emergency services effortlessly.

## Screenshots 📱

<div align="center">
  <h3>Onboarding & Authentication</h3>
  <img src="screenshots/01_splash_screen.jpg" width="18%" alt="Splash Screen" />
  <img src="screenshots/02_introduction_screen.jpg" width="18%" alt="Introduction Screen" />
  <img src="screenshots/03_user_registration_screen.jpg" width="18%" alt="Registration Screen" />
  <img src="screenshots/04_sign_up_screen.jpg" width="18%" alt="Sign Up Screen" />
  <img src="screenshots/05_login_screen.jpg" width="18%" alt="Login Screen" />
  <br/><br/>
  <img src="screenshots/06_username_login_screen.jpg" width="18%" alt="Username Login" />
  <img src="screenshots/07_reset_password_screen.jpg" width="18%" alt="Reset Password" />
  <img src="screenshots/08_email_verification_screen.jpg" width="18%" alt="Email Verification" />
  <br/><br/>

  <h3>Dashboard, Profile & Settings</h3>
  <img src="screenshots/09_home_dashboard_screen_1.jpg" width="18%" alt="Home Dashboard 1" />
  <img src="screenshots/10_home_dashboard_screen_2.jpg" width="18%" alt="Home Dashboard 2" />
  <img src="screenshots/11_menu_screen.jpg" width="18%" alt="Menu Screen" />
  <img src="screenshots/12_edit_profile_screen.jpg" width="18%" alt="Edit Profile" />
  <img src="screenshots/18_about_screen.jpg" width="18%" alt="About Screen" />
  <br/><br/>
  <img src="screenshots/13_setting_screen_1.jpg" width="18%" alt="Settings 1" />
  <img src="screenshots/14_setting_screen_2.jpg" width="18%" alt="Settings 2" />
  <br/><br/>

  <h3>Emergency Contacts & SOS Alerts</h3>
  <img src="screenshots/15_add_emergency_contact_screen.jpg" width="18%" alt="Add Emergency Contact" />
  <img src="screenshots/16_saved_emergency_contact_screen.jpg" width="18%" alt="Saved Contacts" />
  <img src="screenshots/17_sms_alert_received_on_emergency_contact_phone.jpg" width="18%" alt="SMS Alert Received" />
  <br/><br/>

  <h3>Fake Call & Helpline Services</h3>
  <img src="screenshots/21_fake_call_setup_screen.jpg" width="18%" alt="Fake Call Setup" />
  <img src="screenshots/22_fake_incoming_call_screen.jpg" width="18%" alt="Fake Incoming Call" />
  <img src="screenshots/23_fake_call_received_screen.jpg" width="18%" alt="Fake Call Received" />
  <img src="screenshots/24_helpline_screen.jpg" width="18%" alt="Helpline Screen" />
  <img src="screenshots/25_nearby_police_station_screen.jpg" width="18%" alt="Nearby Police Station" />
  <br/><br/>
  <img src="screenshots/26_nearby_hospital_screen.jpg" width="18%" alt="Nearby Hospital" />
  <br/><br/>

  <h3>Safety Tips, Assessment Quiz & Evidence</h3>
  <img src="screenshots/27_safety_tips_screen.jpg" width="18%" alt="Safety Tips" />
  <img src="screenshots/19_mental_harassment_quiz_screen.jpg" width="18%" alt="Quiz Screen" />
  <img src="screenshots/20_mental_harassment_quiz_analysis_screen.jpg" width="18%" alt="Quiz Analysis Screen" />
  <img src="screenshots/28_quiz_analyzed_chart_screen.jpg" width="18%" alt="Quiz Analyzed Chart" />
  <br/><br/>
  <img src="screenshots/29_evidence_screen.jpg" width="18%" alt="Evidence Screen" />
  <img src="screenshots/30_audio_evidence_play_screen.jpg" width="18%" alt="Audio Evidence Play Screen" />
</div>

## Features 🔥

- **User Management:**
  - **Login and Registration:** Easy access for users.

- **Safety Measures:**
  - **Live Location Sharing:** Instantly share your location with trusted contacts.
  - **Trusted Contacts:** Add up to 10 trusted contacts for quick access.
  - **User Notifications:** Alert contacts who are also WSafe users via notifications.
  - **SMS Notifications:** Reach out to non-users via SMS notifications.

- **Emergency Assistance:**
  - **Emergency Helplines:** Access important emergency contact numbers.
  - **Safety Tips:** Learn from a list of safety tips to stay secure.

- **SOS Mode:**
  - **Shake Detection:** Trigger SOS mode with a simple shake gesture.
  - **Audible Alert:** Activate a loud siren to attract attention.
  - **Automatic Emergency Call:** Connect with emergency services instantly in SOS mode.

## Architecture 🗼

This app uses [***Firebase***](https://firebase.google.com/) services.

## Build-Tool 🧰

You need to have [Android Studio Giraffe or above](https://developer.android.com/studio) to build this project.

## Getting Started 🚀

- In Android Studio project, go to `Tools` > `Firebase` > `Authentication` > `Authenticate using a custom authentication system`:
  - First, `Connect to Firebase`
  - After that, `Add the Firebase Authentication SDK to your app`

- Now open your project's [Firebase Console](https://console.firebase.google.com/) > `Authentication` > `Sign-in method`:
  - Enable `Email/Password`
  - Do not enable `Email link (passwordless sign-in)`

- Enable [Token Service API](https://console.cloud.google.com/apis/library/securetoken.googleapis.com)

- After that, go to your project's [Firebase Console](https://console.firebase.google.com/) > `Settings icon` (beside Project Overview) > `Project Settings` > `Service accounts`:
  - Generate new private key, rename the key to `service_account.json` and paste the file in [/res/raw](https://github.com/Mahmud0808/SheGuard/tree/master/app/src/main/res/raw)

- Open the `service_account.json` file:
  - Copy the `project_id` of your private key and paste it in [NotificationAPI.java](https://github.com/Mahmud0808/SheGuard/blob/master/app/src/main/java/com/android/sheguard/api/NotificationAPI.java)

- That's it. Now you are good to go!
