package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ImprovedMenuActivity extends AppCompatActivity {

    // UI Components
    private TextView menuTitle;
    private TabLayout tabLayout;
    private RecyclerView recyclerViewStations;
    private MenuStationAdapter stationAdapter;
    private ProgressBar progressBar;
    private TextView tvNoData;
    private TextView tvMenuDate;
    private TextView tvMenuSummary;
    private FloatingActionButton fabExpandCollapse;
    private MaterialButton btnBack, btnSelectDate, btnDirections, btnSeating;

    // Data
    private String hallName;
    private String currentMealTime = "Breakfast";
    private Date selectedDate;
    private DiningHallMenu diningHallMenu;
    private MenuUpdateService menuUpdateService;
    private Map<String, String> hallAddresses;
    private boolean isLoadingMenu = false;

    // Firebase
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_improved);

        // Initialize Firebase
        firebaseManager = FirebaseManager.getInstance();

        extractHallNameFromIntent();
        initializeHallAddresses();
        initializeViews();
        setupTabLayout();
        setupRecyclerView();
        setupClickListeners();
        initializeServices();

        // Initialize with today's date
        selectedDate = new Date();
        updateDateButtonText();
        updateMenuDateDisplay();

        loadMenuForMealTime(currentMealTime);
        fetchLatestMenuData();
    }

    private void extractHallNameFromIntent() {
        Intent intent = getIntent();
        hallName = intent.getStringExtra(MainActivity.EXTRA_HALL_NAME);
        if (hallName == null) {
            hallName = "Unknown Hall";
        }
    }

    private void initializeHallAddresses() {
        hallAddresses = new HashMap<>();
        hallAddresses.put("Brody", "280 Brody Square, East Lansing, MI 48824");
        hallAddresses.put("Case", "747 E Shaw Ln, East Lansing, MI 48825");
        hallAddresses.put("Owen", "5 Owen Graduate Center, East Lansing, MI 48824");
        hallAddresses.put("Shaw", "900 W Shaw Ln, East Lansing, MI 48824");
        hallAddresses.put("Akers", "140 Akers Hall, East Lansing, MI 48824");
        hallAddresses.put("Landon", "736 E Landon Dr, East Lansing, MI 48824");
        hallAddresses.put("Snyder-Phillips", "362 W Circle Dr, East Lansing, MI 48824");
    }

    private void initializeViews() {
        menuTitle = findViewById(R.id.menuTitle);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerViewStations = findViewById(R.id.recyclerViewStations);
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);
        tvMenuDate = findViewById(R.id.tvMenuDate);
        tvMenuSummary = findViewById(R.id.tvMenuSummary);
        fabExpandCollapse = findViewById(R.id.fabExpandCollapse);
        btnBack = findViewById(R.id.btnBack);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnDirections = findViewById(R.id.btnDirections);
        btnSeating = findViewById(R.id.btnSeating);

        menuTitle.setText(hallName + " Menu");
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentMealTime = "Breakfast";
                        break;
                    case 1:
                        currentMealTime = "Lunch";
                        break;
                    case 2:
                        currentMealTime = "Dinner";
                        break;
                }

                // Log menu view analytics
                if (firebaseManager != null) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    firebaseManager.logMenuView(hallName, currentMealTime, formatter.format(selectedDate));
                }

                loadMenuForMealTime(currentMealTime);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        stationAdapter = new MenuStationAdapter();
        recyclerViewStations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewStations.setAdapter(stationAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSelectDate.setOnClickListener(v -> showDatePicker());

        btnDirections.setOnClickListener(v -> openDirections());

        btnSeating.setOnClickListener(v -> showSeatingOptions());

        fabExpandCollapse.setOnClickListener(v -> {
            stationAdapter.toggleExpandAll();
            updateFabIcon();
        });
    }

    private void updateFabIcon() {
        if (stationAdapter.areAllExpanded()) {
            fabExpandCollapse.setImageResource(android.R.drawable.arrow_up_float);
        } else {
            fabExpandCollapse.setImageResource(android.R.drawable.arrow_down_float);
        }
    }

    private void initializeServices() {
        menuUpdateService = new MenuUpdateService(this);
        menuUpdateService.setMenuUpdateListener(new MenuUpdateService.MenuUpdateListener() {
            @Override
            public void onMenuUpdated(String updatedHallName, boolean success, String message) {
                if (updatedHallName.equals(hallName)) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        isLoadingMenu = false;
                        if (success) {
                            loadMenuForMealTime(currentMealTime);
                            Toast.makeText(ImprovedMenuActivity.this,
                                         "Menu updated from MSU", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ImprovedMenuActivity.this,
                                         "Using cached menu data", Toast.LENGTH_SHORT).show();
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

    private void loadMenuForMealTime(String mealTime) {
        // Fetch from database
        MenuDatabaseHelper dbHelper = MenuDatabaseHelper.getInstance(this);
        List<MenuStation> stations = getStationsFromDatabase(dbHelper, mealTime);

        if (stations.isEmpty()) {
            showNoDataMessage(true);
            tvMenuSummary.setText("No menu available");
        } else {
            showNoDataMessage(false);
            stationAdapter.setStations(stations);
            updateMenuSummary(stations);
        }
    }

    private List<MenuStation> getStationsFromDatabase(MenuDatabaseHelper dbHelper, String mealTime) {
        // Try to get station-based menu data
        Map<String, List<String>> stationItems = dbHelper.getStationMenuItemsForHall(hallName, mealTime);

        List<MenuStation> stations = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : stationItems.entrySet()) {
            MenuStation station = new MenuStation(entry.getKey(), entry.getValue());
            stations.add(station);
        }

        // If no dynamic data, fall back to regular menu items
        if (stations.isEmpty()) {
            Map<String, MenuStation> stationMap = new HashMap<>();
            List<MenuItem> items = dbHelper.getDynamicMenuItemsForHall(hallName, mealTime);

            for (MenuItem item : items) {
                String stationName = item.getCategory();
                if (stationName == null || stationName.isEmpty()) {
                    stationName = "General";
                }

                if (!stationMap.containsKey(stationName)) {
                    stationMap.put(stationName, new MenuStation(stationName));
                }
                stationMap.get(stationName).addItem(item.getName());
            }
            stations = new ArrayList<>(stationMap.values());
        }

        return stations;
    }

    private void updateMenuSummary(List<MenuStation> stations) {
        int totalItems = 0;
        for (MenuStation station : stations) {
            totalItems += station.getItemCount();
        }
        tvMenuSummary.setText(String.format("%d stations • %d items",
                                           stations.size(), totalItems));
    }

    private void showNoDataMessage(boolean show) {
        tvNoData.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewStations.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void fetchLatestMenuData() {
        fetchLatestMenuData(false);
    }

    private void fetchLatestMenuData(boolean forceUpdate) {
        if (!isLoadingMenu) {
            isLoadingMenu = true;
            progressBar.setVisibility(View.VISIBLE);
            menuUpdateService.updateMenuForHall(hallName, forceUpdate);
        }
    }

    private void showSeatingOptions() {
        // TODO: Implement real-time seating availability checking
        // For now, this is a placeholder for future functionality
        Toast.makeText(this, "Seating options coming soon for " + hallName + "!",
                     Toast.LENGTH_SHORT).show();
    }

    private void showDatePicker() {
        // Create date picker with current selected date
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Menu Date")
                .setSelection(selectedDate.getTime())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = new Date(selection);
            updateDateButtonText();
            updateMenuDateDisplay();

            // Log date selection analytics
            if (firebaseManager != null) {
                Bundle params = new Bundle();
                params.putString("hall_name", hallName);
                params.putString("meal_time", currentMealTime);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                params.putString("selected_date", formatter.format(selectedDate));
                firebaseManager.logEvent("date_selected", params);
            }

            // Load menu for the new date
            loadMenuForMealTime(currentMealTime);
            fetchMenuForSelectedDate();
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void updateDateButtonText() {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d", Locale.US);
        String dateText = formatter.format(selectedDate);

        // Check if it's today
        Calendar today = Calendar.getInstance();
        Calendar selected = Calendar.getInstance();
        selected.setTime(selectedDate);

        if (today.get(Calendar.YEAR) == selected.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == selected.get(Calendar.DAY_OF_YEAR)) {
            btnSelectDate.setText("Today");
        } else {
            btnSelectDate.setText(dateText);
        }
    }

    private void updateMenuDateDisplay() {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d", Locale.US);
        tvMenuDate.setText(formatter.format(selectedDate));
    }

    private void fetchMenuForSelectedDate() {
        if (!isLoadingMenu) {
            isLoadingMenu = true;
            progressBar.setVisibility(View.VISIBLE);
            // Use the new method that supports date selection
            menuUpdateService.updateMenuForHallAndDate(hallName, selectedDate, true);
        }
    }

    private void openDirections() {
        String hallAddress = hallAddresses.get(hallName);
        if (hallAddress == null) {
            for (String key : hallAddresses.keySet()) {
                if (hallName.contains(key)) {
                    hallAddress = hallAddresses.get(key);
                    break;
                }
            }
        }

        if (hallAddress != null) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(hallAddress));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                                     + Uri.encode(hallAddress));
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                startActivity(webIntent);
            }
        } else {
            Toast.makeText(this, "Address not found for " + hallName,
                         Toast.LENGTH_SHORT).show();
        }
    }
}