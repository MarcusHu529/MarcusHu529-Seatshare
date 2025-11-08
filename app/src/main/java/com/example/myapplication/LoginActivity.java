package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * LoginActivity - Handles user authentication with multiple login options
 *
 * This activity provides three authentication methods:
 * 1. Create Account - New user registration
 * 2. MSU ID Login - Authentication with MSU credentials
 * 3. Google Login - OAuth authentication with Google
 *
 * Features:
 * - Clean Material Design interface with Spartan green branding
 * - Session management with SharedPreferences
 * - Navigation back to MainActivity after successful login
 * - Placeholder implementations for different auth methods
 *
 * Navigation:
 * - Launched from MainActivity login button
 * - Returns to MainActivity after successful authentication
 */
public class LoginActivity extends AppCompatActivity {

    // UI Components
    private Button btnCreateAccount;
    private Button btnLoginMSU;
    private Button btnLoginGoogle;
    private TextView tvBackToHome;

    // User session management
    private static final String PREFS_NAME = "SpartySpreadsPrefs";
    private static final String KEY_USER_LOGGED_IN = "user_logged_in";
    private static final String KEY_USER_NAME = "user_name";

    /**
     * Initializes the login activity and sets up authentication options
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        initializeViews();

        // Set up click listeners for authentication buttons
        setupClickListeners();
    }

    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnLoginMSU = findViewById(R.id.btnLoginMSU);
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        tvBackToHome = findViewById(R.id.tvBackToHome);
    }

    /**
     * Sets up click listeners for all authentication options and navigation
     */
    private void setupClickListeners() {
        btnCreateAccount.setOnClickListener(v -> handleCreateAccount());
        btnLoginMSU.setOnClickListener(v -> handleMSULogin());
        btnLoginGoogle.setOnClickListener(v -> handleGoogleLogin());
        tvBackToHome.setOnClickListener(v -> finish());
    }

    /**
     * Handles create account button click
     * TODO: Implement actual account creation with forms and validation
     */
    private void handleCreateAccount() {
        // For now, simulate account creation
        showToast("Account creation feature coming soon!");

        // Simulate successful account creation for demo
        simulateSuccessfulLogin("New User");
    }

    /**
     * Handles MSU ID login button click
     * TODO: Implement actual MSU authentication system integration
     */
    private void handleMSULogin() {
        // For now, simulate MSU login
        showToast("MSU ID authentication coming soon!");

        // Simulate successful MSU login for demo
        simulateSuccessfulLogin("MSU Student");
    }

    /**
     * Handles Google login button click
     * TODO: Implement Google OAuth integration
     */
    private void handleGoogleLogin() {
        // For now, simulate Google login
        showToast("Google authentication coming soon!");

        // Simulate successful Google login for demo
        simulateSuccessfulLogin("Google User");
    }

    /**
     * Simulates a successful login for demonstration purposes
     * In a real implementation, this would be called after actual authentication
     *
     * @param userName The name of the authenticated user
     */
    private void simulateSuccessfulLogin(String userName) {
        // Save user session
        saveUserSession(userName);

        // Show success message
        showToast("Welcome, " + userName + "!");

        // Return to MainActivity
        finish();
    }

    /**
     * Saves user session data to SharedPreferences
     *
     * @param userName The name of the logged-in user
     */
    private void saveUserSession(String userName) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_USER_LOGGED_IN, true);
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    /**
     * Shows a toast message to the user
     *
     * @param message The message to display
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}