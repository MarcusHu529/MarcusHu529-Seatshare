package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Firebase Manager - Centralized Firebase service management
 *
 * This class provides easy access to all Firebase services used in the app
 * and handles initialization and configuration.
 *
 * Services included:
 * - Analytics: Track user behavior and app usage
 * - Firestore: Real-time menu data storage and sync
 * - Authentication: User login and profile management
 * - Storage: Image storage for dining hall photos
 * - Messaging: Push notifications for menu updates
 */
public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;

    // Firebase services
    private FirebaseAnalytics analytics;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseMessaging messaging;

    private FirebaseManager() {
        // Private constructor for singleton
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    /**
     * Initialize Firebase services
     * Call this from your Application class
     */
    public void initialize(Context context) {
        try {
            // Initialize Firebase App if not already done
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context);
            }

            // Initialize Firebase services
            analytics = FirebaseAnalytics.getInstance(context);
            firestore = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
            storage = FirebaseStorage.getInstance();
            messaging = FirebaseMessaging.getInstance();

            Log.d(TAG, "Firebase services initialized successfully");

            // Enable Firestore offline persistence
            firestore.enableNetwork();

            // Set up FCM token retrieval
            setupFirebaseMessaging();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase services", e);
        }
    }

    /**
     * Set up Firebase Cloud Messaging for push notifications
     */
    private void setupFirebaseMessaging() {
        messaging.getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Registration Token: " + token);

                    // TODO: Send token to your teammates' backend for push notifications
                });
    }

    // Getter methods for Firebase services

    public FirebaseAnalytics getAnalytics() {
        return analytics;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseStorage getStorage() {
        return storage;
    }

    public FirebaseMessaging getMessaging() {
        return messaging;
    }

    // Analytics helper methods

    public void logEvent(String eventName, android.os.Bundle params) {
        if (analytics != null) {
            analytics.logEvent(eventName, params);
        }
    }

    public void logMenuView(String hallName, String mealTime, String date) {
        android.os.Bundle bundle = new android.os.Bundle();
        bundle.putString("hall_name", hallName);
        bundle.putString("meal_time", mealTime);
        bundle.putString("date", date);
        logEvent("menu_view", bundle);
    }

    public void logUserLogin(String method) {
        android.os.Bundle bundle = new android.os.Bundle();
        bundle.putString("method", method);
        logEvent("user_login", bundle);
    }
}