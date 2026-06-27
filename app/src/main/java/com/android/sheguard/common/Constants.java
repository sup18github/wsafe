package com.android.sheguard.common;

public class Constants {

    // Firebase Firestore
    public static final String FIRESTORE_COLLECTION_USERLIST = "UserList";
    public static final String FIRESTORE_COLLECTION_PHONE2UID = "PhoneToUid";
    public static final String FIRESTORE_COLLECTION_TOKENS = "Tokens";

    // Settings
    public static final String SETTINGS_SHAKE_DETECTION = "shake_detection";
    public static final String SETTINGS_SEND_SMS = "send_sms";
    public static final String SETTINGS_SEND_NOTIFICATION = "send_notification";
    public static final String SETTINGS_PLAY_SIREN = "play_siren";
    public static final String SETTINGS_CALL_EMERGENCY_SERVICE = "call_emergency_service";
    public static final String SETTINGS_RECORD_EVIDENCE = "record_evidence";

    // Others
    public static final String CONTACTS_LIST = "contacts_list";
    public static final String PREFS_USER_NAME = "user_name";
    public static final String PREFS_USER_PHONE = "user_phone";
    public static final String PREFS_USER_EMAIL = "user_email";
    public static final String EMERGENCY_NUMBER = "999";

    // Firebase Realtime Database
    public static final String FIREBASE_SOS_ALERTS = "sos_alerts";

    // SOS Trigger Types
    public static final String TRIGGER_MANUAL = "manual";
    public static final String TRIGGER_SHAKE = "shake";
}
