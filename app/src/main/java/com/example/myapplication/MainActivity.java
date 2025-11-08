package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity - Landing page for Sparty's Spreads app
 *
 * This activity displays the main screen with a grid of MSU dining halls.
 * Users can tap on any dining hall to navigate to its specific menu.
 * Also includes login functionality for user authentication.
 *
 * Layout: 2x4 grid in portrait mode, 4x2 grid in landscape mode
 * Navigation: Each dining hall box navigates to MenuActivity with hall name
 * Authentication: Login button navigates to LoginActivity
 *
 * Dining Halls Supported:
 * - Brody, Case, Owen, Shaw (top row)
 * - Akers, Landon, Holden, Hubbard (bottom row)
 */
public class MainActivity extends AppCompatActivity {

    // Intent extra key for passing dining hall name to MenuActivity
    public static final String EXTRA_HALL_NAME = "hall_name";

    // User session management
    private static final String PREFS_NAME = "SpartySpreadsPrefs";
    private static final String KEY_USER_LOGGED_IN = "user_logged_in";
    private static final String KEY_USER_NAME = "user_name";

    // UI Components
    private Button btnLogin;
    private ImageView imgSpartanLogo;

    /**
     * Initializes the main activity and sets up dining hall click listeners
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        initializeViews();

        // Set up click listeners for all dining hall boxes and login button
        setupClickListeners();

        // Update login button based on user session
        updateLoginButtonState();
    }

    /**
     * Initializes UI components
     */
    private void initializeViews() {
        btnLogin = findViewById(R.id.btnLogin);
        imgSpartanLogo = findViewById(R.id.imgSpartanLogo);
    }

    /**
     * Called when returning from LoginActivity to update the login button state
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateLoginButtonState();
    }

    /**
     * Sets up click listeners for all 8 dining hall boxes
     * Each box navigates to MenuActivity with the corresponding hall name
     */
    private void setupClickListeners() {
        // Find all dining hall box views
        FrameLayout boxBrody = findViewById(R.id.boxBrody);
        FrameLayout boxCase = findViewById(R.id.boxCase);
        FrameLayout boxOwen = findViewById(R.id.boxOwen);
        FrameLayout boxShaw = findViewById(R.id.boxShaw);
        FrameLayout boxAkers = findViewById(R.id.boxAkers);
        FrameLayout boxLandon = findViewById(R.id.boxLandon);
        FrameLayout boxHolden = findViewById(R.id.boxHolden);
        FrameLayout boxHubbard = findViewById(R.id.boxHubbard);

        // Set click listeners for each dining hall
        // Lambda expressions used for cleaner code
        boxBrody.setOnClickListener(v -> onDiningHallClick("Brody"));
        boxCase.setOnClickListener(v -> onDiningHallClick("Case"));
        boxOwen.setOnClickListener(v -> onDiningHallClick("Owen"));
        boxShaw.setOnClickListener(v -> onDiningHallClick("Shaw"));
        boxAkers.setOnClickListener(v -> onDiningHallClick("Akers"));
        boxLandon.setOnClickListener(v -> onDiningHallClick("Landon"));
        boxHolden.setOnClickListener(v -> onDiningHallClick("Holden"));
        boxHubbard.setOnClickListener(v -> onDiningHallClick("Hubbard"));

        // Set click listener for login button
        btnLogin.setOnClickListener(v -> onLoginButtonClick());

        // Set click listener for Spartan logo (admin access)
        imgSpartanLogo.setOnClickListener(v -> onSpartanLogoClick());
    }

    /**
     * Handles dining hall box clicks by navigating to MenuActivity
     *
     * @param diningHallName The name of the selected dining hall
     */
    private void onDiningHallClick(String diningHallName) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra(EXTRA_HALL_NAME, diningHallName);
        startActivity(intent);
    }

    /**
     * Handles login button clicks
     * Navigates to LoginActivity if not logged in, or shows user info if logged in
     */
    private void onLoginButtonClick() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_USER_LOGGED_IN, false);

        if (!isLoggedIn) {
            // Navigate to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            // User is logged in - could show profile or logout options
            // For now, just logout
            logoutUser();
        }
    }

    /**
     * Updates the login button text and appearance based on user session state
     */
    private void updateLoginButtonState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_USER_LOGGED_IN, false);

        if (isLoggedIn) {
            String userName = prefs.getString(KEY_USER_NAME, "User");
            btnLogin.setText(getString(R.string.welcome_user, userName));
        } else {
            btnLogin.setText(getString(R.string.login));
        }
    }

    /**
     * Logs out the current user by clearing session data
     */
    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        updateLoginButtonState();
    }

    /**
     * Handles Spartan logo clicks for admin access
     */
    private void onSpartanLogoClick() {
        Intent intent = new Intent(this, AdminLoginActivity.class);
        startActivity(intent);
    }

    /**
     * Public method to set user login state (called by LoginActivity)
     *
     * @param userName The name of the logged-in user
     */
    public static void setUserLoggedIn(MainActivity activity, String userName) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_USER_LOGGED_IN, true);
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }
}