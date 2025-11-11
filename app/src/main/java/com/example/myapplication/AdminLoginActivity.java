package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * AdminLoginActivity - Handles admin authentication with password
 *
 * This activity provides a simple admin login interface for the 5 administrators.
 * Uses a single alphanumeric password for authentication.
 *
 * Features:
 * - Simple password field authentication
 * - Clean Material Design interface with Spartan green branding
 * - Session management with SharedPreferences
 * - Navigation back to MainActivity after successful login
 *
 * Navigation:
 * - Launched from MainActivity Spartan logo click
 * - Returns to MainActivity after successful authentication
 */
public class AdminLoginActivity extends AppCompatActivity {

    // UI Components
    private EditText etAdminPassword;
    private Button btnAdminLogin;
    private TextView tvBackToHome;

    // Admin authentication
    private static final String ADMIN_PASSWORD = "SPARTAN2024"; // Simple alphanumeric password
    private static final String PREFS_NAME = "SpartySpreadsPrefs";
    private static final String KEY_ADMIN_LOGGED_IN = "admin_logged_in";

    /**
     * Initializes the admin login activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        etAdminPassword = findViewById(R.id.etAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        tvBackToHome = findViewById(R.id.tvBackToHome);
    }

    /**
     * Sets up click listeners for authentication and navigation
     */
    private void setupClickListeners() {
        btnAdminLogin.setOnClickListener(v -> handleAdminLogin());
        tvBackToHome.setOnClickListener(v -> finish());
    }

    /**
     * Handles admin login button click
     * Validates password and grants admin access
     */
    private void handleAdminLogin() {
        String enteredPassword = etAdminPassword.getText().toString().trim();

        if (enteredPassword.isEmpty()) {
            showToast("Please enter admin password");
            return;
        }

        if (enteredPassword.equals(ADMIN_PASSWORD)) {
            // Successful admin login
            saveAdminSession();
            showToast("Admin access granted!");

            // TODO: Navigate to admin dashboard/panel
            // For now, just return to main activity
            finish();
        } else {
            // Failed login
            showToast("Invalid admin password");
            etAdminPassword.setText(""); // Clear password field
        }
    }

    /**
     * Saves admin session data to SharedPreferences
     */
    private void saveAdminSession() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_ADMIN_LOGGED_IN, true);
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