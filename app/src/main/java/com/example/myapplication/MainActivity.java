package com.example.myapplication;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


// Firebase imports
import android.os.Bundle;
// Google Play Services imports temporarily removed to fix redirect issue
// import com.google.android.gms.location.FusedLocationProviderClient;
// import com.google.android.gms.location.LocationCallback;
// import com.google.android.gms.location.LocationRequest;
// import com.google.android.gms.location.LocationResult;
// import com.google.android.gms.location.LocationServices;
// import com.google.android.gms.location.Priority;
// import com.google.android.gms.tasks.OnSuccessListener;
import java.util.Arrays;
import java.util.Comparator;

/**
 * MainActivity - Landing page for Sparty's Spreads app
 *
 * This activity displays the main screen with MSU dining halls in a smart layout:
 * - Featured dining hall at the top (full width) - shows the CLOSEST hall to user
 * - Remaining 6 dining halls in a 3x2 grid below in default order
 *
 * Users can tap on any dining hall to navigate to its specific menu.
 * Also includes login functionality and location-based featured hall switching.
 *
 * Layout: Dynamic featured hall + 3x2 grid layout
 * Navigation: Each dining hall box navigates to MenuActivity with hall name
 * Authentication: Login button navigates to LoginActivity
 * Location: Uses Android LocationManager (no Google Play Services) - closest hall featured
 *
 * Dining Halls Supported:
 * - Featured position: Dynamically shows closest hall (image + text update)
 * - Grid positions: Snyder-Phillips, Brody, Case, Owen, Shaw, Akers, Landon (excluding featured)
 */
public class MainActivity extends AppCompatActivity {

    // Intent extra key for passing dining hall name to MenuActivity
    public static final String EXTRA_HALL_NAME = "hall_name";

    // Location permission request code
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // User session management
    private static final String PREFS_NAME = "SpartySpreadsPrefs";
    private static final String KEY_USER_LOGGED_IN = "user_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_LOCATION_PERMISSION_ASKED = "location_permission_asked";

    // UI Components
    private Button btnLogin;
    private ImageView imgSpartanLogo;
    private GridLayout diningHallGrid;
    private ProgressBar locationProgressBar;

    // Firebase
    private FirebaseManager firebaseManager;

    // Location services using Android LocationManager (no Google Play Services)
    private LocationManager locationManager;
    private LocationListener locationListener;
    private DiningHall[] diningHalls;
    private boolean isReorganizing = false;
    private double lastLatitude = 0.0;
    private double lastLongitude = 0.0;

    // Debug flag - set to false to disable reorganization temporarily
    private static final boolean ENABLE_LOCATION_REORGANIZATION = true;

    private static final String PREFS = "SpartySpreadsPrefs";
    private TextView txtNearestHall;


    /**
     * Helper functions for local storage
     */
    private void saveLocalState(String nearestHall, Location loc) {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        sp.edit()
                .putString("last_nearest_hall", nearestHall)
                .putLong("last_update_time", System.currentTimeMillis())
                .putFloat("last_lat", (float) loc.getLatitude())
                .putFloat("last_lon", (float) loc.getLongitude())
                .putFloat("last_acc", loc.hasAccuracy() ? loc.getAccuracy() : -1f)
                .apply();
    }

    private boolean restoreLocalStateIfAvailable() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String lastHall = sp.getString("last_nearest_hall", null);
        if (lastHall != null) {

            txtNearestHall.setText("Showing last known location from local storage: " + lastHall + " (approximate)");
            return true;
        }
        return false;
    }

    /**
     * Initializes the main activity and sets up dining hall click listeners
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            android.util.Log.d("MainActivity", "onCreate started");

            // Set layout - most critical step
            setContentView(R.layout.activity_main);
            android.util.Log.d("MainActivity", "Layout set successfully");

            // Initialize Firebase
            firebaseManager = FirebaseManager.getInstance();

            // Initialize basic UI components only
            btnLogin = findViewById(R.id.btnLogin);
            imgSpartanLogo = findViewById(R.id.imgSpartanLogo);
            diningHallGrid = findViewById(R.id.diningHallGrid);
            android.util.Log.d("MainActivity", "Basic UI components found");

            // Initialize dining halls data
            initializeDiningHalls();
            android.util.Log.d("MainActivity", "Dining halls initialized");

            // Set up basic click listeners (no complex features)
            if (btnLogin != null) {
                btnLogin.setOnClickListener(v -> onLoginButtonClick());
            }
            if (imgSpartanLogo != null) {
                imgSpartanLogo.setOnClickListener(v -> onSpartanLogoClick());
            }

            // Set up dining hall click listeners
            setupBasicClickListeners();

            // Update login button state
            updateLoginButtonState();

            // Initialize menu update service for all halls
            initializeMenuUpdateService();

            // Delayed safe initialization of location features
            scheduleLocationInitialization();

            android.util.Log.d("MainActivity", "Minimal onCreate completed successfully");

            restoreLocalStateIfAvailable();

        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Critical error in onCreate", e);
            e.printStackTrace(); // Print full stack trace
            finish();
        }
    }

    /**
     * Sets up basic click listeners without complex initialization
     */
    private void setupBasicClickListeners() {
        try {
            // Snyder-Phillips
            FrameLayout boxSnyderPhillips = findViewById(R.id.boxSnyderPhillips);
            if (boxSnyderPhillips != null) {
                boxSnyderPhillips.setOnClickListener(v -> onDiningHallClick("Snyder-Phillips"));
            }

            // Brody
            FrameLayout boxBrody = findViewById(R.id.boxBrody);
            if (boxBrody != null) {
                boxBrody.setOnClickListener(v -> onDiningHallClick("Brody"));
            }

            // Case
            FrameLayout boxCase = findViewById(R.id.boxCase);
            if (boxCase != null) {
                boxCase.setOnClickListener(v -> onDiningHallClick("Case"));
            }

            // Owen
            FrameLayout boxOwen = findViewById(R.id.boxOwen);
            if (boxOwen != null) {
                boxOwen.setOnClickListener(v -> onDiningHallClick("Owen"));
            }

            // Shaw
            FrameLayout boxShaw = findViewById(R.id.boxShaw);
            if (boxShaw != null) {
                boxShaw.setOnClickListener(v -> onDiningHallClick("Shaw"));
            }

            // Akers
            FrameLayout boxAkers = findViewById(R.id.boxAkers);
            if (boxAkers != null) {
                boxAkers.setOnClickListener(v -> onDiningHallClick("Akers"));
            }

            // Landon
            FrameLayout boxLandon = findViewById(R.id.boxLandon);
            if (boxLandon != null) {
                boxLandon.setOnClickListener(v -> onDiningHallClick("Landon"));
            }

            android.util.Log.d("MainActivity", "Basic click listeners set up successfully");
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to set up basic click listeners", e);
        }
    }

    /**
     * Initializes UI components
     */
    private void initializeViews() {

        txtNearestHall = findViewById(R.id.txtNearestHall);

        try {
            btnLogin = findViewById(R.id.btnLogin);
            android.util.Log.d("MainActivity", "Login button found: " + (btnLogin != null));
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to find login button", e);
        }

        try {
            imgSpartanLogo = findViewById(R.id.imgSpartanLogo);
            android.util.Log.d("MainActivity", "Spartan logo found: " + (imgSpartanLogo != null));
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to find spartan logo", e);
        }

        try {
            diningHallGrid = findViewById(R.id.diningHallGrid);
            android.util.Log.d("MainActivity", "Dining hall grid found: " + (diningHallGrid != null));
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to find dining hall grid", e);
        }

        // Link dining hall data with UI components
        try {
            if (diningHalls != null) {
                android.util.Log.d("MainActivity", "Linking " + diningHalls.length + " dining halls to UI");
                for (int i = 0; i < diningHalls.length; i++) {
                    DiningHall hall = diningHalls[i];
                    try {
                        FrameLayout frameLayout = findViewById(hall.getViewId());
                        hall.setFrameLayout(frameLayout);
                        android.util.Log.d("MainActivity", "Linked " + hall.getName() + " to view: " + (frameLayout != null));
                    } catch (Exception e) {
                        android.util.Log.e("MainActivity", "Failed to link " + hall.getName() + " to view", e);
                    }
                }
            } else {
                android.util.Log.w("MainActivity", "diningHalls is null, skipping view linking");
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to link dining halls to views", e);
        }
    }

    /**
     * Called when returning from LoginActivity to update the login button state
     */
    @Override
    protected void onResume() {
        super.onResume();
        try {
            updateLoginButtonState();

            // Start location services automatically if enabled and location manager is ready
            if (ENABLE_LOCATION_REORGANIZATION && locationManager != null) {
                android.util.Log.d("MainActivity", "Starting location services in onResume");
                requestLocationAndReorganizeSafely();
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error in onResume", e);
        }
    }

    /**
     * Stop location updates when activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop location updates
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                // Permission might have been revoked
            }
        }
    }

    /**
     * Sets up click listeners for all 7 dining hall boxes
     * Each box navigates to MenuActivity with the corresponding hall name
     */
    private void setupClickListeners() {
        // Find all dining hall box views
        FrameLayout boxSnyderPhillips = findViewById(R.id.boxSnyderPhillips);
        FrameLayout boxBrody = findViewById(R.id.boxBrody);
        FrameLayout boxCase = findViewById(R.id.boxCase);
        FrameLayout boxOwen = findViewById(R.id.boxOwen);
        FrameLayout boxShaw = findViewById(R.id.boxShaw);
        FrameLayout boxAkers = findViewById(R.id.boxAkers);
        FrameLayout boxLandon = findViewById(R.id.boxLandon);

        // Set click listeners for each dining hall
        // Lambda expressions used for cleaner code
        boxSnyderPhillips.setOnClickListener(v -> onDiningHallClick("Snyder-Phillips"));
        boxBrody.setOnClickListener(v -> onDiningHallClick("Brody"));
        boxCase.setOnClickListener(v -> onDiningHallClick("Case"));
        boxOwen.setOnClickListener(v -> onDiningHallClick("Owen"));
        boxShaw.setOnClickListener(v -> onDiningHallClick("Shaw"));
        boxAkers.setOnClickListener(v -> onDiningHallClick("Akers"));
        boxLandon.setOnClickListener(v -> onDiningHallClick("Landon"));

        // Set click listener for login button
        btnLogin.setOnClickListener(v -> onLoginButtonClick());

        // Set click listener for Spartan logo (admin access)
        imgSpartanLogo.setOnClickListener(v -> onSpartanLogoClick());

        // Add title tap for manual location refresh and long-press for testing
        if (ENABLE_LOCATION_REORGANIZATION) {
            View titleLayout = findViewById(R.id.titleLayout);
            titleLayout.setOnClickListener(v -> {
                if (hasLocationPermission()) {
                    Toast.makeText(this, "Refreshing location...", Toast.LENGTH_SHORT).show();
                    getCurrentLocationAndReorganize();
                } else {
                    Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
                }
            });

            // Long press for testing different dining hall locations
            titleLayout.setOnLongClickListener(v -> {
                testLocationSorting();
                return true;
            });
        }
    }

    /**
     * Handles dining hall box clicks by navigating to MenuActivity
     *
     * @param diningHallName The name of the selected dining hall
     */
    private void onDiningHallClick(String diningHallName) {
        // Log analytics event for dining hall selection
        if (firebaseManager != null) {
            Bundle params = new Bundle();
            params.putString("hall_name", diningHallName);
            params.putString("source", "main_activity");
            firebaseManager.logEvent("dining_hall_selected", params);
        }

        Intent intent = new Intent(this, ImprovedMenuActivity.class);
        intent.putExtra(EXTRA_HALL_NAME, diningHallName);
        startActivity(intent);
    }

    /**
     * Handles login button clicks
     * Navigates to LoginActivity if not logged in, or shows user info if logged in
     */
    private void onLoginButtonClick() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            startActivity(new Intent(this, AccountActivity.class));
        }
    }

    /**
     * Login Button change to "Welcome, <user>!" helper (retrieves name)
     */
    private String deriveNameFromEmail(String email) {
        if (email == null || email.trim().isEmpty()) return "User";
        int at = email.indexOf('@');
        String local = at > 0 ? email.substring(0, at) : email;
        if (local.length() == 0) return "User";
        return local.substring(0,1).toUpperCase() + local.substring(1);
    }

    /**
     * Updates the login button text and appearance based on user session state
     */
    private void updateLoginButtonState() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u != null) {
            String name = (u.getDisplayName() != null && !u.getDisplayName().trim().isEmpty())
                    ? u.getDisplayName().trim()
                    : deriveNameFromEmail(u.getEmail());
            btnLogin.setText(getString(R.string.welcome_user, name));
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
     * Initializes dining halls data with original positions
     */
    private void initializeDiningHalls() {
        diningHalls = DiningHall.getAllDiningHalls();

        // Set original positions (0-7 for 8 dining halls)
        for (int i = 0; i < diningHalls.length; i++) {
            diningHalls[i].setOriginalPosition(i);
        }
    }

    /**
     * Initialize menu update service to fetch menus in the background
     */
    private void initializeMenuUpdateService() {
        try {
            MenuUpdateService menuService = new MenuUpdateService(this);
            menuService.setMenuUpdateListener(new MenuUpdateService.MenuUpdateListener() {
                @Override
                public void onMenuUpdated(String hallName, boolean success, String message) {
                    android.util.Log.d("MainActivity", "Menu update for " + hallName + ": " + message);
                }

                @Override
                public void onAllMenusUpdated() {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "All menus updated", Toast.LENGTH_SHORT).show();
                    });
                }
            });

            // Fetch all menus in background (won't re-fetch if already up to date)
            menuService.updateAllHallMenus(false);
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error initializing menu update service", e);
        }
    }

    /**
     * Safely schedules location initialization after the activity is fully loaded
     */
    private void scheduleLocationInitialization() {
        try {
            // Post a delayed runnable to ensure all UI components are ready
            if (btnLogin != null) {
                btnLogin.post(() -> {
                    try {
                        initializeLocationFeaturesSafely();
                    } catch (Exception e) {
                        android.util.Log.e("MainActivity", "Error in delayed location initialization", e);
                    }
                });
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error scheduling location initialization", e);
        }
    }

    /**
     * Safely initializes location features with extensive error checking
     */
    private void initializeLocationFeaturesSafely() {
        try {
            android.util.Log.d("MainActivity", "Starting safe location initialization");

            // Initialize LocationManager safely
            if (locationManager == null) {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                android.util.Log.d("MainActivity", "LocationManager initialized: " + (locationManager != null));
            }

            // Link dining halls to UI components safely
            linkDiningHallsToViews();

            // Add safe testing handler to title
            addSafeTestingHandler();

            // Set up location listener for automatic updates
            setupLocationListenerSafely();

            // Initialize with default state - trigger an initial reorganization
            initializeDefaultState();

            android.util.Log.d("MainActivity", "Location features initialized successfully");

        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error in safe location initialization", e);
        }
    }

    /**
     * Sets up location listener for automatic updates
     */
    private void setupLocationListenerSafely() {
        try {
            if (locationManager == null) {
                android.util.Log.w("MainActivity", "LocationManager is null, cannot setup listener");
                return;
            }

            // Create location listener
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull android.location.Location location) {
                    try {
                        double newLatitude = location.getLatitude();
                        double newLongitude = location.getLongitude();

                        android.util.Log.d("LocationUpdate",
                            String.format("New location: %.6f, %.6f", newLatitude, newLongitude));

                        // Check if location has changed significantly (more than ~10 meters for responsive updates)
                        double distance = DiningHall.calculateDistance(
                                lastLatitude, lastLongitude, newLatitude, newLongitude);

                        if (distance > 10 || (lastLatitude == 0.0 && lastLongitude == 0.0)) {
                            lastLatitude = newLatitude;
                            lastLongitude = newLongitude;

                            android.util.Log.d("LocationUpdate",
                                String.format("Significant location change: %.0fm, reorganizing tiles", distance));

                            Toast.makeText(MainActivity.this,
                                    String.format("Location updated: %.4f, %.4f", newLatitude, newLongitude),
                                    Toast.LENGTH_SHORT).show();

                            // Reorganize tiles based on new location
                            reorganizeTilesByDistanceSafely(newLatitude, newLongitude);
                        } else {
                            android.util.Log.d("LocationUpdate",
                                String.format("Minor location change: %.0fm, skipping reorganization", distance));
                        }
                    } catch (Exception e) {
                        android.util.Log.e("LocationUpdate", "Error processing location change", e);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, android.os.Bundle extras) {
                    android.util.Log.d("LocationUpdate", "Provider status changed: " + provider + " status: " + status);
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {
                    android.util.Log.d("LocationUpdate", "Provider enabled: " + provider);
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    android.util.Log.d("LocationUpdate", "Provider disabled: " + provider);
                }
            };

            android.util.Log.d("MainActivity", "Location listener created successfully");

        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error setting up location listener", e);
        }
    }

    /**
     * Safely requests location permission and starts location updates
     */
    private void requestLocationAndReorganizeSafely() {
        try {
            if (hasLocationPermission()) {
                android.util.Log.d("LocationRequest", "Location permission granted, starting updates");
                startLocationUpdatesSafely();
            } else {
                android.util.Log.d("LocationRequest", "Location permission not granted, requesting");
                requestLocationPermission();
            }
        } catch (Exception e) {
            android.util.Log.e("LocationRequest", "Error requesting location", e);
        }
    }

    /**
     * Safely starts location updates
     */
    private void startLocationUpdatesSafely() {
        try {
            if (!hasLocationPermission() || locationManager == null || locationListener == null) {
                android.util.Log.w("LocationStart", "Cannot start location updates - missing permission, manager, or listener");
                return;
            }

            android.util.Log.d("LocationStart", "Starting location updates");

            // Try to get last known location first for immediate reorganization
            android.location.Location lastKnownLocation = null;
            try {
                if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                    lastKnownLocation = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
                    android.util.Log.d("LocationStart", "Got GPS last known location: " + (lastKnownLocation != null));
                }
                if (lastKnownLocation == null && locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                    lastKnownLocation = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
                    android.util.Log.d("LocationStart", "Got Network last known location: " + (lastKnownLocation != null));
                }
            } catch (SecurityException e) {
                android.util.Log.e("LocationStart", "Security exception getting last known location", e);
            }

            // If we have a last known location, use it immediately
            if (lastKnownLocation != null) {
                double lat = lastKnownLocation.getLatitude();
                double lon = lastKnownLocation.getLongitude();
                android.util.Log.d("LocationStart", String.format("Using last known location: %.6f, %.6f", lat, lon));

                lastLatitude = lat;
                lastLongitude = lon;
                reorganizeTilesByDistanceSafely(lat, lon);
            }

            // Start location updates for continuous monitoring
            try {
                if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        android.location.LocationManager.GPS_PROVIDER,
                        2000, // 2 seconds (faster for emulator testing)
                        5,    // 5 meters (more sensitive)
                        locationListener
                    );
                    android.util.Log.d("LocationStart", "GPS location updates started (2s/5m)");
                }
                if (locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        android.location.LocationManager.NETWORK_PROVIDER,
                        3000, // 3 seconds (faster for emulator testing)
                        5,    // 5 meters (more sensitive)
                        locationListener
                    );
                    android.util.Log.d("LocationStart", "Network location updates started (3s/5m)");
                }

                Toast.makeText(this, "Location tracking started", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                android.util.Log.e("LocationStart", "Security exception starting location updates", e);
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            android.util.Log.e("LocationStart", "Error starting location updates", e);
        }
    }

    /**
     * Initialize the app with device location for proper hall distribution
     */
    private void initializeDefaultState() {
        try {
            android.util.Log.d("InitState", "Initializing with device location for proper hall distribution");

            // Post this to run after the UI is fully loaded
            btnLogin.postDelayed(() -> {
                try {
                    android.util.Log.d("InitState", "Running delayed device location initialization");
                    initializeWithDeviceLocation();
                } catch (Exception e) {
                    android.util.Log.e("InitState", "Error in delayed initialization", e);
                }
            }, 500); // 500ms delay to ensure UI is ready

        } catch (Exception e) {
            android.util.Log.e("InitState", "Error initializing default state", e);
        }
    }

    /**
     * Initialize using actual device location
     */
    private void initializeWithDeviceLocation() {
        try {
            if (hasLocationPermission() && locationManager != null) {
                android.util.Log.d("InitLocation", "Has permission, getting device location");
                getDeviceLocationForInitialization();
            } else {
                android.util.Log.d("InitLocation", "No permission, requesting and then getting location");
                // Request permission first, then get location
                requestLocationPermission();
            }
        } catch (Exception e) {
            android.util.Log.e("InitLocation", "Error initializing with device location", e);
            // Fallback to default if device location fails
            fallbackToDefaultLocation();
        }
    }

    /**
     * Get actual device location for initialization
     */
    private void getDeviceLocationForInitialization() {
        try {
            android.util.Log.d("InitLocation", "Getting device location for initialization");

            android.location.Location deviceLocation = null;

            // Try to get last known location
            try {
                if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                    deviceLocation = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
                    android.util.Log.d("InitLocation", "GPS last known location: " + (deviceLocation != null));
                }
                if (deviceLocation == null && locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                    deviceLocation = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
                    android.util.Log.d("InitLocation", "Network last known location: " + (deviceLocation != null));
                }
            } catch (SecurityException e) {
                android.util.Log.e("InitLocation", "Security exception getting device location", e);
                fallbackToDefaultLocation();
                return;
            }

            if (deviceLocation != null) {
                double lat = deviceLocation.getLatitude();
                double lon = deviceLocation.getLongitude();

                android.util.Log.d("InitLocation",
                    String.format("Using device location: %.6f, %.6f", lat, lon));

                Toast.makeText(this,
                    String.format("Using your location: %.4f, %.4f", lat, lon),
                    Toast.LENGTH_SHORT).show();

                // Organize halls based on actual device location
                reorganizeTilesByDistanceSafely(lat, lon);

                // Store this location for future updates
                lastLatitude = lat;
                lastLongitude = lon;

                // Start location updates for future changes
                startLocationUpdatesSafely();
            } else {
                android.util.Log.d("InitLocation", "No device location available, using fallback");
                fallbackToDefaultLocation();
            }

        } catch (Exception e) {
            android.util.Log.e("InitLocation", "Error getting device location", e);
            fallbackToDefaultLocation();
        }
    }

    /**
     * Fallback to default MSU campus center if device location unavailable
     */
    private void fallbackToDefaultLocation() {
        try {
            android.util.Log.d("InitLocation", "Using fallback MSU campus center location");

            // MSU campus center coordinates
            double defaultLat = 42.728;
            double defaultLon = -84.480;

            Toast.makeText(this, "Using MSU campus center location", Toast.LENGTH_SHORT).show();

            reorganizeTilesByDistanceSafely(defaultLat, defaultLon);

        } catch (Exception e) {
            android.util.Log.e("InitLocation", "Error in fallback location", e);
        }
    }

    /**
     * Adds safe testing handler to title layout
     */
    private void addSafeTestingHandler() {
        try {
            View titleLayout = findViewById(R.id.titleLayout);
            if (titleLayout != null) {
                titleLayout.setOnClickListener(v -> requestLocationPermissionSafely());
                titleLayout.setOnLongClickListener(v -> {
                    testLocationFeatureSafely();
                    return true;
                });
                android.util.Log.d("MainActivity", "Safe testing handlers added to title");
            }

            // Add a debug button to the login button for manual testing
            if (btnLogin != null) {
                btnLogin.setOnLongClickListener(v -> {
                    testManualReorganization();
                    return true;
                });
                android.util.Log.d("MainActivity", "Manual test handler added to login button");
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error adding testing handlers", e);
        }
    }

    /**
     * Manual test to cycle through different closest halls
     */
    private void testManualReorganization() {
        try {
            // Test different locations to see if reorganization works
            double[][] testLocations = {
                {42.724567591646384, -84.48870729559268}, // Case Hall
                {42.72657035421706, -84.47055947007354},   // Owen Hall
                {42.731379562909424, -84.49526567905579}, // Brody Hall
                {42.72434170664002, -84.46480484532314},  // Akers Hall
                {42.73022289349873, -84.47344892521157}   // Snyder-Phillips
            };

            String[] locationNames = {"Case", "Owen", "Brody", "Akers", "Snyder-Phillips"};

            // Cycle through locations
            int testIndex = (int) (System.currentTimeMillis() / 2000) % testLocations.length;
            double[] location = testLocations[testIndex];

            android.util.Log.d("ManualTest",
                String.format("MANUAL TEST: Setting location to %s (%.6f, %.6f)",
                    locationNames[testIndex], location[0], location[1]));

            Toast.makeText(this,
                String.format("MANUAL TEST: %s location", locationNames[testIndex]),
                Toast.LENGTH_SHORT).show();

            reorganizeTilesByDistanceSafely(location[0], location[1]);

        } catch (Exception e) {
            android.util.Log.e("ManualTest", "Error in manual reorganization test", e);
            Toast.makeText(this, "Manual test failed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Safely requests location permission without causing crashes
     */
    private void requestLocationPermissionSafely() {
        try {
            if (hasLocationPermission()) {
                Toast.makeText(this, "Location permission already granted", Toast.LENGTH_SHORT).show();
                getCurrentLocationSafely();
            } else {
                Toast.makeText(this, "Requesting location permission...", Toast.LENGTH_SHORT).show();
                requestLocationPermission();
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error requesting location permission", e);
            Toast.makeText(this, "Error requesting location", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Safely tests location feature without requiring actual GPS
     */
    private void testLocationFeatureSafely() {
        try {
            // Test from Case Hall location
            double testLat = 42.724567591646384;
            double testLon = -84.48870729559268;

            Toast.makeText(this, "Testing location feature from Case Hall", Toast.LENGTH_SHORT).show();

            // Use the full reorganization method
            reorganizeTilesByDistanceSafely(testLat, testLon);
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error in safe location testing", e);
            Toast.makeText(this, "Location test failed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Safely gets current location without crashing
     */
    private void getCurrentLocationSafely() {
        try {
            if (!hasLocationPermission() || locationManager == null) {
                Toast.makeText(this, "Location permission or manager not available", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show();

            // Try to get last known location first
            Location lastKnownLocation = null;
            try {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (lastKnownLocation == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            } catch (SecurityException e) {
                android.util.Log.e("MainActivity", "Security exception getting location", e);
                Toast.makeText(this, "Location access denied", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lastKnownLocation != null) {
                double lat = lastKnownLocation.getLatitude();
                double lon = lastKnownLocation.getLongitude();

                Toast.makeText(this,
                    String.format("Found location: %.4f, %.4f", lat, lon),
                    Toast.LENGTH_SHORT).show();

                // Use the full reorganization method instead of just updating featured content
                reorganizeTilesByDistanceSafely(lat, lon);
            } else {
                Toast.makeText(this, "No location available. Try the long-press test.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error getting current location safely", e);
            Toast.makeText(this, "Error getting location", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Safely finds the closest dining hall
     */
    private DiningHall findClosestHallSafely(double userLat, double userLon) {
        try {
            DiningHall[] halls = DiningHall.getAllDiningHalls();
            if (halls == null || halls.length == 0) {
                return null;
            }

            DiningHall closest = halls[0];
            double shortestDistance = DiningHall.calculateDistance(
                userLat, userLon, closest.getLatitude(), closest.getLongitude());

            for (DiningHall hall : halls) {
                double distance = DiningHall.calculateDistance(
                    userLat, userLon, hall.getLatitude(), hall.getLongitude());
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    closest = hall;
                }
            }

            android.util.Log.d("MainActivity",
                String.format("Closest hall to %.6f,%.6f is %s (%.0fm)",
                    userLat, userLon, closest.getName(), shortestDistance));

            return closest;
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error finding closest hall safely", e);
            return null;
        }
    }

    /**
     * Safely reorganizes tiles by distance without breaking the layout
     */
    private void reorganizeTilesByDistanceSafely(double userLatitude, double userLongitude) {
        try {
            android.util.Log.d("SafeReorganize",
                String.format("Reorganizing for location: %.6f, %.6f", userLatitude, userLongitude));

            // Get all dining halls and calculate distances
            DiningHall[] allHalls = DiningHall.getAllDiningHalls();
            if (allHalls == null || allHalls.length == 0) {
                android.util.Log.e("SafeReorganize", "No dining halls available");
                return;
            }

            // Calculate distances for all halls
            for (DiningHall hall : allHalls) {
                double distance = DiningHall.calculateDistance(
                    userLatitude, userLongitude,
                    hall.getLatitude(), hall.getLongitude()
                );
                hall.setDistanceFromUser(distance);

                android.util.Log.d("SafeReorganize",
                    String.format("%s: %.0fm away", hall.getName(), distance));
            }

            // Find the closest hall
            DiningHall closestHall = allHalls[0];
            for (DiningHall hall : allHalls) {
                if (hall.getDistanceFromUser() < closestHall.getDistanceFromUser()) {
                    closestHall = hall;
                }
            }

            android.util.Log.d("SafeReorganize", "Closest hall: " + closestHall.getName());

            // --- Persist last known nearest hall + location locally ---
            try {
                Location snap = new Location(LocationManager.PASSIVE_PROVIDER);
                snap.setLatitude(userLatitude);
                snap.setLongitude(userLongitude);
                // If you have a computed distance threshold, you can set a rough accuracy; otherwise pick a sane default
                snap.setAccuracy(50f);
                saveLocalState(closestHall.getName(), snap);
            } catch (Exception ignore) {}

            // Update the featured tile with the closest hall
            updateFeaturedHallContent(closestHall);

            // Update the grid with the remaining 6 halls
            updateGridContent(closestHall, allHalls);

            // Show feedback
            Toast.makeText(this,
                String.format("Featured: %s (%.0fm away)",
                    closestHall.getName(), closestHall.getDistanceFromUser()),
                Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            android.util.Log.e("SafeReorganize", "Error in safe reorganization", e);
            Toast.makeText(this, "Error reorganizing halls", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the grid content with proper hall swapping
     */
    private void updateGridContent(DiningHall newFeaturedHall, DiningHall[] allHalls) {
        try {
            android.util.Log.d("GridSwap", "Starting grid swap for new featured: " + newFeaturedHall.getName());

            // Map of view IDs to their default hall names
            // IMPORTANT: All 7 halls must be represented (6 in grid + 1 featured)
            int[] gridViewIds = {
                R.id.boxBrody, R.id.boxCase, R.id.boxOwen,
                R.id.boxShaw, R.id.boxAkers, R.id.boxLandon
            };

            String[] defaultGridHallNames = {
                "Brody", "Case", "Owen", "Shaw", "Akers", "Landon"
            };

            // All 7 halls including Snyder-Phillips
            String[] allHallNames = {
                "Snyder-Phillips", "Brody", "Case", "Owen", "Shaw", "Akers", "Landon"
            };

            // Find what was previously featured (get current featured hall name from UI)
            TextView currentFeaturedText = findViewById(R.id.txtSnyderPhillips);
            String previousFeaturedHallName = currentFeaturedText != null ?
                extractHallNameFromDisplayText(currentFeaturedText.getText().toString()) : "Snyder-Phillips";

            android.util.Log.d("GridSwap",
                String.format("Previous featured: %s, New featured: %s",
                    previousFeaturedHallName, newFeaturedHall.getName()));

            // Find which grid position the new featured hall should vacate
            int vacatedGridPosition = -1;
            for (int i = 0; i < defaultGridHallNames.length; i++) {
                if (defaultGridHallNames[i].equals(newFeaturedHall.getName())) {
                    vacatedGridPosition = i;
                    break;
                }
            }

            android.util.Log.d("GridSwap",
                String.format("New featured hall %s vacated grid position %d",
                    newFeaturedHall.getName(), vacatedGridPosition));

            // Create a list of all halls that need to be in the grid (exclude the featured hall)
            java.util.List<String> hallsNeededInGrid = new java.util.ArrayList<>();
            for (String hall : allHallNames) {
                if (!hall.equals(newFeaturedHall.getName())) {
                    hallsNeededInGrid.add(hall);
                }
            }

            android.util.Log.d("GridSwap",
                String.format("Halls needed in grid (6): %s", String.join(", ", hallsNeededInGrid)));

            // Update each grid position with the 6 non-featured halls
            for (int i = 0; i < gridViewIds.length; i++) {
                FrameLayout gridFrame = findViewById(gridViewIds[i]);
                if (gridFrame != null && i < hallsNeededInGrid.size()) {

                    String hallNameForPosition = hallsNeededInGrid.get(i);
                    DiningHall hallForThisPosition = findHallByName(allHalls, hallNameForPosition);

                    android.util.Log.d("GridSwap",
                        String.format("Position %d: Assigning %s", i, hallNameForPosition));

                    if (hallForThisPosition != null) {
                        updateGridTileContent(gridFrame, hallForThisPosition, gridViewIds[i]);
                        android.util.Log.d("GridSwap",
                            String.format("FINAL Position %d: %s", i, hallForThisPosition.getName()));
                    }
                }
            }

        } catch (Exception e) {
            android.util.Log.e("GridSwap", "Error in grid swap logic", e);
        }
    }

    /**
     * Helper method to find a hall by name
     */
    private DiningHall findHallByName(DiningHall[] halls, String name) {
        for (DiningHall hall : halls) {
            if (hall.getName().equals(name)) {
                return hall;
            }
        }
        return null;
    }

    /**
     * Helper method to extract hall name from display text
     */
    private String extractHallNameFromDisplayText(String displayText) {
        // Convert display names back to hall names
        if (displayText.contains("Snyder-Phillips")) return "Snyder-Phillips";
        if (displayText.contains("Brody")) return "Brody";
        if (displayText.contains("Case")) return "Case";
        if (displayText.contains("Owen")) return "Owen";
        if (displayText.contains("Shaw")) return "Shaw";
        if (displayText.contains("Akers")) return "Akers";
        if (displayText.contains("Landon")) return "Landon";
        return "Snyder-Phillips"; // Default fallback
    }

    /**
     * Updates a single grid tile with the appropriate content
     */
    private void updateGridTileContent(FrameLayout frameLayout, DiningHall hall, int viewId) {
        try {
            // Find the ImageView and TextView within this frame
            ImageView imageView = null;
            TextView textView = null;

            // Map view IDs to their child ImageView and TextView IDs using if-else
            if (viewId == R.id.boxBrody) {
                imageView = findViewById(R.id.imgBrody);
                textView = findViewById(R.id.txtBrody);
            } else if (viewId == R.id.boxCase) {
                imageView = findViewById(R.id.imgCase);
                textView = findViewById(R.id.txtCase);
            } else if (viewId == R.id.boxOwen) {
                imageView = findViewById(R.id.imgOwen);
                textView = findViewById(R.id.txtOwen);
            } else if (viewId == R.id.boxShaw) {
                imageView = findViewById(R.id.imgShaw);
                textView = findViewById(R.id.txtShaw);
            } else if (viewId == R.id.boxAkers) {
                imageView = findViewById(R.id.imgAkers);
                textView = findViewById(R.id.txtAkers);
            } else if (viewId == R.id.boxLandon) {
                imageView = findViewById(R.id.imgLandon);
                textView = findViewById(R.id.txtLandon);
            }

            if (imageView != null && textView != null) {
                // Ensure grid updates happen on UI thread
                final ImageView finalImageView = imageView;
                final TextView finalTextView = textView;

                runOnUiThread(() -> {
                    try {
                        // Update the content to show this hall
                        finalTextView.setText(hall.getDisplayName());
                        updateImageForHall(finalImageView, hall.getName());

                        // Force refresh
                        finalTextView.invalidate();
                        finalImageView.invalidate();
                        frameLayout.invalidate();
                        frameLayout.requestLayout();

                        android.util.Log.d("GridTile",
                            String.format("UI updated for tile %d with %s", viewId, hall.getName()));

                    } catch (Exception e) {
                        android.util.Log.e("GridTile", "Error updating grid tile UI", e);
                    }
                });

                // Update click listener
                frameLayout.setOnClickListener(v -> onDiningHallClick(hall.getName()));

                android.util.Log.d("GridTile",
                    String.format("Updated tile %d with %s", viewId, hall.getName()));
            }

        } catch (Exception e) {
            android.util.Log.e("GridTile", "Error updating grid tile content", e);
        }
    }

    /**
     * Updates an ImageView with the correct image for a dining hall
     */
    private void updateImageForHall(ImageView imageView, String hallName) {
        try {
            switch (hallName.toLowerCase()) {
                case "snyder-phillips":
                    imageView.setImageResource(R.drawable.sny_phi);
                    break;
                case "brody":
                    imageView.setImageResource(R.drawable.brody);
                    break;
                case "case":
                    imageView.setImageResource(R.drawable.casehall);
                    break;
                case "owen":
                    imageView.setImageResource(R.drawable.owen);
                    break;
                case "shaw":
                    imageView.setImageResource(R.drawable.shaw);
                    break;
                case "akers":
                    imageView.setImageResource(R.drawable.akers);
                    break;
                case "landon":
                    imageView.setImageResource(R.drawable.landon);
                    break;
                default:
                    // Fallback to Snyder-Phillips image
                    imageView.setImageResource(R.drawable.sny_phi);
                    break;
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error updating image for " + hallName, e);
        }
    }

    /**
     * Safely links dining hall objects to their UI components
     */
    private void linkDiningHallsToViews() {
        try {
            if (diningHalls != null) {
                android.util.Log.d("MainActivity", "Linking " + diningHalls.length + " dining halls to UI");

                for (DiningHall hall : diningHalls) {
                    try {
                        FrameLayout frameLayout = findViewById(hall.getViewId());
                        if (frameLayout != null) {
                            hall.setFrameLayout(frameLayout);
                            android.util.Log.d("MainActivity", "Linked " + hall.getName() + " to view");
                        } else {
                            android.util.Log.w("MainActivity", "Could not find view for " + hall.getName());
                        }
                    } catch (Exception e) {
                        android.util.Log.e("MainActivity", "Failed to link " + hall.getName(), e);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error linking dining halls to views", e);
        }
    }

    /**
     * Sets up location services using Android LocationManager
     */
    private void setupLocationServices() {
        // Initialize LocationManager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Create location listener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double newLatitude = location.getLatitude();
                double newLongitude = location.getLongitude();

                // Check if location has changed significantly (more than ~50 meters)
                double distance = DiningHall.calculateDistance(
                        lastLatitude, lastLongitude, newLatitude, newLongitude);

                if (distance > 50 || (lastLatitude == 0.0 && lastLongitude == 0.0)) {
                    lastLatitude = newLatitude;
                    lastLongitude = newLongitude;

                    Toast.makeText(MainActivity.this,
                            String.format("Location updated: %.4f, %.4f", newLatitude, newLongitude),
                            Toast.LENGTH_SHORT).show();

                    reorganizeTilesByDistanceSafely(newLatitude, newLongitude);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        };
    }

    /**
     * Requests location permission and reorganizes tiles based on distance
     */
    private void requestLocationAndReorganize() {
        // Ensure all FrameLayouts are properly linked before proceeding
        boolean allViewsLinked = true;
        for (DiningHall hall : diningHalls) {
            if (hall.getFrameLayout() == null) {
                allViewsLinked = false;
                break;
            }
        }

        if (!allViewsLinked) {
            // Try again after a short delay
            diningHallGrid.postDelayed(this::requestLocationAndReorganize, 100);
            return;
        }

        if (hasLocationPermission()) {
            getCurrentLocationAndReorganize();
        } else {
            requestLocationPermission();
        }
    }

    /**
     * Checks if location permission is granted
     */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests location permission from user
     */
    private void requestLocationPermission() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean hasAsked = prefs.getBoolean(KEY_LOCATION_PERMISSION_ASKED, false);

        if (!hasAsked) {
            // Show explanation toast
            Toast.makeText(this, "Enable location to see dining halls sorted by distance",
                    Toast.LENGTH_LONG).show();

            // Mark that we've asked
            prefs.edit().putBoolean(KEY_LOCATION_PERMISSION_ASKED, true).apply();
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                           Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Gets current location and starts location updates using LocationManager
     */
    private void getCurrentLocationAndReorganize() {
        if (!hasLocationPermission()) {
            return;
        }

        try {
            // Try to get last known location first
            Location lastKnownLocation = null;
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (lastKnownLocation == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastKnownLocation != null) {
                lastLatitude = lastKnownLocation.getLatitude();
                lastLongitude = lastKnownLocation.getLongitude();
                reorganizeTilesByDistanceSafely(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            } else {
                Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show();
            }

            // Start location updates for live reorganization
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 50, locationListener);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 15000, 100, locationListener);
            }

        } catch (SecurityException e) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Reorganizes dining hall tiles - puts closest hall in featured position, rest in default order
     */
    private void reorganizeTilesByDistance(double userLatitude, double userLongitude) {
        if (isReorganizing) {
            return; // Prevent multiple simultaneous reorganizations
        }

        isReorganizing = true;

        // Calculate distances for all dining halls
        for (DiningHall hall : diningHalls) {
            double distance = DiningHall.calculateDistance(
                    userLatitude, userLongitude,
                    hall.getLatitude(), hall.getLongitude()
            );
            hall.setDistanceFromUser(distance);

            // Enhanced debug logging
            android.util.Log.d("DiningHallDistance",
                String.format("%s: %.0fm (hall coords: %.6f, %.6f)",
                    hall.getName(), distance, hall.getLatitude(), hall.getLongitude()));
        }

        // Find the closest dining hall
        DiningHall closestHall = diningHalls[0];
        for (DiningHall hall : diningHalls) {
            if (hall.getDistanceFromUser() < closestHall.getDistanceFromUser()) {
                closestHall = hall;
            }
        }

        // Create new arrangement: closest hall first, others in original order
        DiningHall[] newArrangement = new DiningHall[diningHalls.length];
        newArrangement[0] = closestHall; // Featured position

        // Fill remaining positions with other halls in default order, skipping the closest
        int gridIndex = 1;
        DiningHall[] originalOrder = DiningHall.getAllDiningHalls();
        for (DiningHall hall : originalOrder) {
            if (!hall.getName().equals(closestHall.getName()) && gridIndex < diningHalls.length) {
                // Find the actual hall object with distance data
                for (DiningHall calculatedHall : diningHalls) {
                    if (calculatedHall.getName().equals(hall.getName())) {
                        newArrangement[gridIndex++] = calculatedHall;
                        break;
                    }
                }
            }
        }

        // Update the diningHalls array
        diningHalls = newArrangement;

        // Enhanced debug: Show current location and arrangement
        android.util.Log.d("UserLocation",
            String.format("User at: %.6f, %.6f", userLatitude, userLongitude));

        android.util.Log.d("NewArrangement",
            String.format("Featured: %s (%.0fm), Grid order: %s, %s, %s...",
                diningHalls[0].getName(), diningHalls[0].getDistanceFromUser(),
                diningHalls[1].getName(), diningHalls[2].getName(), diningHalls[3].getName()));

        // Show user feedback with closest hall
        String closestHallName = closestHall.getName();
        double closestDistance = closestHall.getDistanceFromUser();
        Toast.makeText(this,
            String.format("Featured: %s (%.0fm away)", closestHallName, closestDistance),
            Toast.LENGTH_LONG).show();

        // Animate tiles to new positions
        animateTileReorganization();
    }

    /**
     * Animates the reorganization of tiles and updates content
     * Featured position gets the closest hall's content, grid gets the rest
     */
    private void animateTileReorganization() {
        // Update the featured position content with closest hall
        updateFeaturedHallContent(diningHalls[0]);

        // Clear the grid and rebuild with remaining halls
        diningHallGrid.removeAllViews();

        // Add halls 1-6 to the grid layout (skip index 0 which is the featured hall)
        for (int i = 1; i < diningHalls.length; i++) {
            final DiningHall hall = diningHalls[i];
            final FrameLayout frameLayout = hall.getFrameLayout();

            // Calculate grid position (i-1 because we skip index 0)
            int gridIndex = i - 1;
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(gridIndex / 2, 1f);
            params.columnSpec = GridLayout.spec(gridIndex % 2, 1f);

            // Set margins to match original layout
            int marginPx = (int) (8 * getResources().getDisplayMetrics().density);
            params.setMargins(marginPx, marginPx, marginPx, marginPx);

            // Set width and height to 0dp with weight 1 for proper grid behavior
            params.width = 0;
            params.height = 0;

            frameLayout.setLayoutParams(params);

            // Start with invisible and scale down
            frameLayout.setAlpha(0f);
            frameLayout.setScaleX(0.8f);
            frameLayout.setScaleY(0.8f);

            // Add to grid
            diningHallGrid.addView(frameLayout);

            // Animate in with delay based on position
            final int animationIndex = i;
            frameLayout.postDelayed(() -> {
                ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(frameLayout, "alpha", 0f, 1f);
                ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(frameLayout, "scaleX", 0.8f, 1f);
                ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(frameLayout, "scaleY", 0.8f, 1f);

                alphaAnimator.setDuration(300);
                scaleXAnimator.setDuration(300);
                scaleYAnimator.setDuration(300);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator);
                animatorSet.start();
            }, animationIndex * 100); // Stagger animations
        }

        // Animate the featured hall with a special highlight effect
        FrameLayout featuredFrame = findViewById(R.id.boxSnyderPhillips);
        featuredFrame.postDelayed(() -> {
            // Pulse animation for the featured hall
            ObjectAnimator pulseAnimator = ObjectAnimator.ofFloat(featuredFrame, "scaleX", 1f, 1.05f, 1f);
            ObjectAnimator pulseAnimatorY = ObjectAnimator.ofFloat(featuredFrame, "scaleY", 1f, 1.05f, 1f);

            pulseAnimator.setDuration(600);
            pulseAnimatorY.setDuration(600);

            AnimatorSet pulseSet = new AnimatorSet();
            pulseSet.playTogether(pulseAnimator, pulseAnimatorY);
            pulseSet.start();
        }, 200);

        // Show completion message after all animations
        diningHallGrid.postDelayed(() -> {
            String featuredHallName = diningHalls[0].getDisplayName();
            Toast.makeText(this,
                String.format("Featured: %s (closest to you)", featuredHallName),
                Toast.LENGTH_LONG).show();
            isReorganizing = false;
        }, diningHalls.length * 100 + 600);
    }

    /**
     * Updates the featured hall position with the content of the closest dining hall
     */
    private void updateFeaturedHallContent(DiningHall closestHall) {
        try {
            android.util.Log.d("FeaturedUpdate",
                String.format("STARTING updateFeaturedHallContent with %s", closestHall.getName()));

            // Find the featured frame and its child components directly
            FrameLayout featuredFrame = findViewById(R.id.boxSnyderPhillips);
            ImageView featuredImage = findViewById(R.id.imgSnyderPhillips);
            TextView featuredText = findViewById(R.id.txtSnyderPhillips);

            android.util.Log.d("FeaturedUpdate",
                String.format("UI components found - Frame: %s, Image: %s, Text: %s",
                    (featuredFrame != null), (featuredImage != null), (featuredText != null)));

            if (featuredFrame != null && featuredImage != null && featuredText != null) {
                // Get current values for debugging
                String currentText = featuredText.getText().toString();
                android.util.Log.d("FeaturedUpdate",
                    String.format("Current text: '%s', Changing to: '%s'",
                        currentText, closestHall.getDisplayName()));

                // Ensure UI updates happen on main thread
                runOnUiThread(() -> {
                    try {
                        // Update text to show the closest hall name
                        featuredText.setText(closestHall.getDisplayName());

                        // Update image based on the hall name
                        android.util.Log.d("FeaturedUpdate",
                            String.format("Updating image from current to %s", closestHall.getName()));
                        updateFeaturedImage(featuredImage, closestHall.getName());

                        // Force the views to refresh
                        featuredText.invalidate();
                        featuredImage.invalidate();
                        featuredFrame.invalidate();

                        // Request layout in case dimensions changed
                        featuredFrame.requestLayout();

                        // Add a brief animation to make the change visible
                        featuredFrame.setAlpha(0.3f);
                        featuredFrame.animate()
                            .alpha(1.0f)
                            .setDuration(300)
                            .start();

                        android.util.Log.d("FeaturedUpdate", "UI refresh commands sent with animation");

                    } catch (Exception e) {
                        android.util.Log.e("FeaturedUpdate", "Error in UI thread update", e);
                    }
                });

                // Update click listener to navigate to the closest hall
                featuredFrame.setOnClickListener(v -> onDiningHallClick(closestHall.getName()));

                // Verify the change took effect
                String newText = featuredText.getText().toString();
                android.util.Log.d("FeaturedUpdate",
                    String.format("COMPLETED updateFeaturedHallContent - Final text: '%s'", newText));

                // Show a toast to confirm the change
                Toast.makeText(this,
                    String.format("Featured tile updated to: %s", closestHall.getName()),
                    Toast.LENGTH_SHORT).show();

            } else {
                android.util.Log.e("FeaturedUpdate", "Could not find featured hall UI components");
                android.util.Log.e("FeaturedUpdate",
                    String.format("Missing components - Frame: %s, Image: %s, Text: %s",
                        (featuredFrame == null ? "NULL" : "OK"),
                        (featuredImage == null ? "NULL" : "OK"),
                        (featuredText == null ? "NULL" : "OK")));
            }
        } catch (Exception e) {
            android.util.Log.e("FeaturedUpdate", "Error updating featured hall content", e);
            e.printStackTrace();
        }
    }

    /**
     * Updates the featured hall image based on the dining hall name
     */
    private void updateFeaturedImage(ImageView imageView, String hallName) {
        // Map dining hall names to their images
        switch (hallName.toLowerCase()) {
            case "snyder-phillips":
                imageView.setImageResource(R.drawable.sny_phi);
                break;
            case "brody":
                imageView.setImageResource(R.drawable.brody);
                break;
            case "case":
                imageView.setImageResource(R.drawable.casehall);
                break;
            case "owen":
                imageView.setImageResource(R.drawable.owen);
                break;
            case "shaw":
                imageView.setImageResource(R.drawable.shaw);
                break;
            case "akers":
                imageView.setImageResource(R.drawable.akers);
                break;
            case "landon":
                imageView.setImageResource(R.drawable.landon);
                break;
            default:
                // Fallback to a default image
                imageView.setImageResource(R.drawable.sny_phi);
                break;
        }
    }

    /**
     * Handles permission request results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get device location for initialization
                android.util.Log.d("PermissionResult", "Location permission granted, getting device location");
                Toast.makeText(this, "Location permission granted. Getting your location...", Toast.LENGTH_SHORT).show();

                // Initialize with device location now that we have permission
                getDeviceLocationForInitialization();
            } else {
                // Permission denied, use fallback
                android.util.Log.d("PermissionResult", "Location permission denied, using fallback");
                Toast.makeText(this, "Location permission denied. Using campus center.",
                        Toast.LENGTH_SHORT).show();
                fallbackToDefaultLocation();
            }
        }
    }

    /**
     * Test method to cycle through different dining hall locations for debugging
     */
    private void testLocationSorting() {
        // Get all dining hall coordinates for testing
        DiningHall[] testHalls = DiningHall.getAllDiningHalls();

        // Create a simple rotation through different test locations
        int testIndex = (int) (System.currentTimeMillis() / 3000) % testHalls.length;
        DiningHall testHall = testHalls[testIndex];

        Toast.makeText(this,
            String.format("Testing from %s location\nCoords: %.6f, %.6f",
                testHall.getName(), testHall.getLatitude(), testHall.getLongitude()),
            Toast.LENGTH_LONG).show();

        android.util.Log.d("LocationTest",
            String.format("Testing from %s at coordinates: %.6f, %.6f",
                testHall.getName(), testHall.getLatitude(), testHall.getLongitude()));

        // Use the dining hall's coordinates as test location
        reorganizeTilesByDistance(testHall.getLatitude(), testHall.getLongitude());
    }

    /**
     * Safe manual testing of closest hall feature (no location permissions needed)
     */
    private void testClosestHallManually() {
        try {
            // Get all dining hall coordinates
            DiningHall[] testHalls = DiningHall.getAllDiningHalls();

            // Cycle through different test locations every 3 seconds
            int testIndex = (int) (System.currentTimeMillis() / 3000) % testHalls.length;
            DiningHall testLocation = testHalls[testIndex];

            android.util.Log.d("ManualTest",
                String.format("Testing from %s at coordinates: %.6f, %.6f",
                    testLocation.getName(), testLocation.getLatitude(), testLocation.getLongitude()));

            Toast.makeText(this,
                String.format("Testing from %s\nFinding closest hall...", testLocation.getName()),
                Toast.LENGTH_SHORT).show();

            // Find which hall would be closest from this test location
            DiningHall closestHall = findClosestHall(testLocation.getLatitude(), testLocation.getLongitude());

            if (closestHall != null) {
                android.util.Log.d("ManualTest",
                    String.format("Closest hall to %s is %s",
                        testLocation.getName(), closestHall.getName()));

                // Update the featured hall content manually
                updateFeaturedHallContent(closestHall);

                // Show results with more detail
                Toast.makeText(this,
                    String.format("From %s location:\nClosest hall is %s\nFeatured tile should now show %s",
                        testLocation.getName(), closestHall.getName(), closestHall.getDisplayName()),
                    Toast.LENGTH_LONG).show();
            } else {
                android.util.Log.e("ManualTest", "findClosestHall returned null");
                Toast.makeText(this, "Failed to find closest hall", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error in manual testing", e);
            Toast.makeText(this, "Testing failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Find the closest dining hall to given coordinates (safe version)
     */
    private DiningHall findClosestHall(double userLat, double userLon) {
        try {
            DiningHall[] halls = DiningHall.getAllDiningHalls();
            DiningHall closest = halls[0];
            double shortestDistance = DiningHall.calculateDistance(userLat, userLon, closest.getLatitude(), closest.getLongitude());

            for (DiningHall hall : halls) {
                double distance = DiningHall.calculateDistance(userLat, userLon, hall.getLatitude(), hall.getLongitude());
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    closest = hall;
                }
            }

            return closest;
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error finding closest hall", e);
            return null;
        }
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