package com.example.myapplication;

import android.app.Application;
import android.util.Log;

/**
 * SpartySpreadsApplication - Main Application class
 *
 * This class is called when the app starts up and handles:
 * - Firebase initialization
 * - Global app configuration
 * - Shared services setup
 */
public class SpartySpreadsApplication extends Application {
    private static final String TAG = "SpartySpreadsApp";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Sparty's Spreads application starting...");

        // Initialize Firebase services
        FirebaseManager.getInstance().initialize(this);

        // TODO: Your teammates can add other initialization here:
        // - Crash reporting setup
        // - Performance monitoring
        // - Custom backend connections
        // - Global configuration

        Log.d(TAG, "Application initialization completed");
    }
}