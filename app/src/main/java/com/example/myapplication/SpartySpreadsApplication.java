package com.example.myapplication;

import android.app.Application;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

/**
 * SpartySpreadsApplication - Main Application class
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

        // 2. REPLACE THE PREVIOUS BLOCK WITH THIS SAFER VERSION
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, now check for nulls to be safe
                        AuthResult authResult = task.getResult();
                        if (authResult != null) {
                            FirebaseUser firebaseUser = authResult.getUser();
                            if (firebaseUser != null) {
                                // This is now null-safe
                                Log.d(TAG, "Signed in anonymously: " + firebaseUser.getUid());
                            } else {
                                Log.w(TAG, "Anonymous sign-in successful, but user is null.");
                            }
                        } else {
                            Log.w(TAG, "Anonymous sign-in successful, but auth result is null.");
                        }
                    } else {
                        // If sign in fails
                        Log.w(TAG, "Anonymous sign-in failed", task.getException());
                    }
                });

        Log.d(TAG, "Application initialization completed");
    }
}