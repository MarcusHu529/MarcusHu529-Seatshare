package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.myapplication.MenuCache;


/**
 * MenuActivity - Displays dining hall menu with meal time selection
 *
 * This activity shows the menu for a specific dining hall, allowing users to:
 * - Switch between Breakfast, Lunch, and Dinner menus
 * - View menu items in a scrollable list
 * - Navigate back to MainActivity
 * - Get directions to the dining hall via Google Maps
 * - Access seating options (future feature)
 *
 * State Management:
 * - Preserves selected meal time during orientation changes
 * - Loads hall-specific menu items from SQLite database
 * - Updates UI to highlight current meal selection
 *
 * Navigation:
 * - Receives hall name from MainActivity via Intent extra
 * - Menu item clicks navigate to MenuItemDetailActivity
 */
public class MenuActivity extends AppCompatActivity {

    // UI Components
    private TextView menuTitle;
    private TextView btnBreakfast, btnLunch, btnDinner;
    private Button btnBack, btnSeating, btnDirections;
    private RecyclerView recyclerViewMenu;
    private MenuAdapter menuAdapter;

    // Data and State
    private String hallName;
    private String currentMealTime = "Breakfast";
    private Map<String, String> hallAddresses;
    private MenuDatabaseHelper dbHelper;
    private MenuUpdateService menuUpdateService;
    private boolean isLoadingMenu = false;

    // Constants
    private static final String KEY_CURRENT_MEAL_TIME = "current_meal_time";
    private static final int SPARTAN_GREEN_COLOR = 0xFF18453B;

    /**
     * Initializes the menu activity with dining hall data and UI components
     * Handles state restoration for orientation changes
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_improved);

        // Extract dining hall name from intent
        extractHallNameFromIntent();

        // Restore saved state if available (orientation changes)
        restoreInstanceState(savedInstanceState);

        // Initialize components in order
        initializeHallAddresses();
        initializeDatabase();
        initializeViews();
        setupRecyclerView();
        setupUserInterface();
        setupEventListeners();

        // Load menu data for current meal time
        loadMenuForMealTime(currentMealTime);

        // Fetch latest menu data in background
        fetchLatestMenuData();
    }

    /**
     * Extracts the dining hall name from the intent passed by MainActivity
     */
    private void extractHallNameFromIntent() {
        Intent intent = getIntent();
        hallName = intent.getStringExtra(MainActivity.EXTRA_HALL_NAME);
        if (hallName == null) {
            hallName = "Unknown Hall";
        }
    }

    /**
     * Restores the selected meal time from saved instance state
     * This preserves user selection during configuration changes
     */
    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            currentMealTime = savedInstanceState.getString(KEY_CURRENT_MEAL_TIME, "Breakfast");
        }
    }

    /**
     * Initializes the database helper singleton
     */
    private void initializeDatabase() {
        dbHelper = MenuDatabaseHelper.getInstance(this);

        // Initialize menu update service
        menuUpdateService = new MenuUpdateService(this);
        menuUpdateService.setMenuUpdateListener(new MenuUpdateService.MenuUpdateListener() {
            @Override
            public void onMenuUpdated(String updatedHallName, boolean success, String message) {
                if (updatedHallName.equals(hallName)) {
                    runOnUiThread(() -> {
                        isLoadingMenu = false;
                        if (success) {
                            loadMenuForMealTime(currentMealTime);
                            Toast.makeText(MenuActivity.this, "Menu updated from MSU", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MenuActivity.this, "Using cached menu data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onAllMenusUpdated() {
                // Not used in this activity
            }
        });
    }

    /**
     * Sets up the user interface after all components are initialized
     */
    private void setupUserInterface() {
        updateMenuTitle();
        updateButtonStates();
    }

    /**
     * Sets up all event listeners for buttons and interactions
     */
    private void setupEventListeners() {
        setupMealButtons();
        setupBackButton();
        setupOtherButtons();
    }

    /**
     * Initializes the mapping of dining hall names to their physical addresses
     * Used for Google Maps navigation integration
     */
    private void initializeHallAddresses() {
        hallAddresses = new HashMap<>();

        // MSU Dining Hall addresses for Google Maps navigation
        hallAddresses.put("Brody", "280 Brody Square, East Lansing, MI 48824");
        hallAddresses.put("Case", "747 E Shaw Ln, East Lansing, MI 48825");
        hallAddresses.put("Owen", "5 Owen Graduate Center, East Lansing, MI 48824");
        hallAddresses.put("Shaw", "900 W Shaw Ln, East Lansing, MI 48824");
        hallAddresses.put("Akers", "140 Akers Hall, East Lansing, MI 48824");
        hallAddresses.put("Landon", "736 E Landon Dr, East Lansing, MI 48824");
        hallAddresses.put("Holden", "191 E Holden Hall, East Lansing, MI 48824");
        hallAddresses.put("Hubbard", "435 E Grand River Ave, East Lansing, MI 48823");
    }

    /**
     * Initializes all view components by finding them in the layout
     */
    private void initializeViews() {
        // Title and menu display
        menuTitle = findViewById(R.id.menuTitle);
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu);

        // Meal selection buttons
        btnBreakfast = findViewById(R.id.btnBreakfast);
        btnLunch = findViewById(R.id.btnLunch);
        btnDinner = findViewById(R.id.btnDinner);

        // Action buttons
        btnBack = findViewById(R.id.btnBack);
        btnSeating = findViewById(R.id.btnSeating);
        btnDirections = findViewById(R.id.btnDirections);
    }

    /**
     * Configures the RecyclerView for displaying menu items
     * Uses LinearLayoutManager for vertical scrolling list
     */
    private void setupRecyclerView() {
        menuAdapter = new MenuAdapter(this);
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMenu.setAdapter(menuAdapter);
    }

    /**
     * Updates the activity title to show the current dining hall name
     */
    private void updateMenuTitle() {
        menuTitle.setText(hallName + " Menu");
    }

    /**
     * Sets up click listeners for meal selection buttons (Breakfast, Lunch, Dinner)
     * Each button updates the current meal time and refreshes the menu display
     */
    private void setupMealButtons() {
        btnBreakfast.setOnClickListener(v -> {
            currentMealTime = "Breakfast";
            loadMenuForMealTime(currentMealTime);
            updateButtonStates();
        });

        btnLunch.setOnClickListener(v -> {
            currentMealTime = "Lunch";
            loadMenuForMealTime(currentMealTime);
            updateButtonStates();
        });

        btnDinner.setOnClickListener(v -> {
            currentMealTime = "Dinner";
            loadMenuForMealTime(currentMealTime);
            updateButtonStates();
        });
    }

    /**
     * Sets up the back button to return to MainActivity
     */
    private void setupBackButton() {
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Sets up click listeners for seating and directions buttons
     */
    private void setupOtherButtons() {
        btnSeating.setOnClickListener(v -> showSeatingOptions());
        btnDirections.setOnClickListener(v -> showDirections());
    }

    /**
     * Placeholder method for future seating options functionality
     * TODO: Implement real-time seating availability checking
     */
    private void showSeatingOptions() {
        // TODO: Implement seating options functionality
        // For now, this is a placeholder for future functionality
        Toast.makeText(this, "Seating options coming soon!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Opens Google Maps with directions to the selected dining hall
     * Falls back to web browser if Google Maps app is not installed
     */
    private void showDirections() {
        String hallAddress = hallAddresses.get(hallName);

        if (hallAddress != null) {
            // Create Google Maps navigation intent
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(hallAddress));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Check if Google Maps is installed
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback to web browser with Google Maps
                Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode(hallAddress));
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                startActivity(webIntent);
            }
        } else {
            Toast.makeText(this, "Address not found for " + hallName, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads menu items for the specified meal time from the database
     * Updates the RecyclerView adapter with the new data
     *
     * @param mealTime The meal time to load (Breakfast, Lunch, or Dinner)
     */
    private void loadMenuForMealTime(String mealTime) {
        // 1) Try to load dynamic menu from DB first
        List<MenuItem> menuItems = dbHelper.getDynamicMenuItemsForHall(hallName, mealTime);

        if (!menuItems.isEmpty()) {
            // We have data -> display it
            menuAdapter.updateMenuItems(menuItems);

            // 2) Save a JSON cache for offline fallback (do IO off main thread)
            new Thread(() -> MenuCache.saveMenu(
                    MenuActivity.this, hallName, mealTime, menuItems)).start();

            return;
        }

        // No DB rows yet -> If not already fetching, let the user know we’re trying
        if (!isLoadingMenu) {
            Toast.makeText(this, "Fetching live menu data...", Toast.LENGTH_SHORT).show();
        }

        // 3) Fallback to local JSON cache if available
        List<MenuItem> cached = MenuCache.loadMenu(this, hallName, mealTime);
        if (cached != null && !cached.isEmpty()) {
            Toast.makeText(this, "Offline: showing cached menu", Toast.LENGTH_SHORT).show();
            menuAdapter.updateMenuItems(cached);
        } else {
            // Nothing to show yet; keep adapter empty (or show a placeholder)
            menuAdapter.updateMenuItems(menuItems); // remains empty list
        }
    }


    /**
     * Fetches the latest menu data from MSU website
     */
    private void fetchLatestMenuData() {
        if (!isLoadingMenu) {
            isLoadingMenu = true;
            menuUpdateService.updateMenuForHall(hallName, false);
        }
    }

    /**
     * Updates the visual state of meal selection buttons
     * Highlights the currently selected meal and resets others to default state
     */
    private void updateButtonStates() {
        // Reset all buttons to default unselected state
        btnBreakfast.setBackgroundResource(R.drawable.button_unselected);
        btnBreakfast.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        btnLunch.setBackgroundResource(R.drawable.button_unselected);
        btnLunch.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        btnDinner.setBackgroundResource(R.drawable.button_unselected);
        btnDinner.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        // Highlight current meal button with Spartan green
        switch (currentMealTime) {
            case "Breakfast":
                btnBreakfast.setBackgroundResource(R.drawable.button_selected);
                btnBreakfast.setTextColor(SPARTAN_GREEN_COLOR);
                break;
            case "Lunch":
                btnLunch.setBackgroundResource(R.drawable.button_selected);
                btnLunch.setTextColor(SPARTAN_GREEN_COLOR);
                break;
            case "Dinner":
                btnDinner.setBackgroundResource(R.drawable.button_selected);
                btnDinner.setTextColor(SPARTAN_GREEN_COLOR);
                break;
        }
    }

    /**
     * Saves the current meal time selection to preserve user state during configuration changes
     * This ensures the selected meal time is maintained when the device is rotated
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_MEAL_TIME, currentMealTime);
    }
}