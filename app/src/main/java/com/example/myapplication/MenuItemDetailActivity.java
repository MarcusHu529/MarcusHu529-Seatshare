package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;

/**
 * MenuItemDetailActivity - Displays detailed nutrition information for a specific menu item
 *
 * This activity shows comprehensive information about a selected food item including:
 * - Basic details: name, category, price, description
 * - Nutrition facts: calories, fat, protein, carbohydrates, fiber, sugar
 * - Allergen warnings and ingredient list
 * - Food image (placeholder for now)
 *
 * Navigation:
 * - Receives item name from MenuActivity via Intent extra
 * - Back button returns to MenuActivity
 *
 * Data Source:
 * - Fetches detailed item information from SQLite database
 * - Uses MenuItemDetailed model for comprehensive nutrition data
 */
public class MenuItemDetailActivity extends AppCompatActivity {

    // Intent extra key for menu item name
    public static final String EXTRA_ITEM_NAME = "item_name";

    // UI Components - Basic Information
    private TextView tvFoodName, tvCategory, tvPrice, tvDescription;
    private ImageView ivFoodImage;
    private Button btnBack;

    // UI Components - Nutrition Information
    private TextView tvCalories, tvFat, tvProtein, tvCarbs, tvFiber, tvSugar;

    // UI Components - Allergens and Ingredients
    private TextView tvAllergens, tvIngredients;

    // Data Access
    private MenuDatabaseHelper dbHelper;

    // Formatting
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("$#.##");
    private static final DecimalFormat NUTRITION_FORMAT = new DecimalFormat("#.#");

    /**
     * Initializes the detail activity and loads menu item information
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_item_detail);

        // Initialize components
        initializeDatabase();
        initializeViews();
        setupEventListeners();

        // Load menu item data from intent
        loadMenuItemFromIntent();
    }

    /**
     * Initializes the database helper singleton
     */
    private void initializeDatabase() {
        dbHelper = MenuDatabaseHelper.getInstance(this);
    }

    /**
     * Initializes all view components by finding them in the layout
     */
    private void initializeViews() {
        // Basic information views
        tvFoodName = findViewById(R.id.tvFoodName);
        tvCategory = findViewById(R.id.tvCategory);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        ivFoodImage = findViewById(R.id.ivFoodImage);
        btnBack = findViewById(R.id.btnBack);

        // Nutrition information views
        tvCalories = findViewById(R.id.tvCalories);
        tvFat = findViewById(R.id.tvFat);
        tvProtein = findViewById(R.id.tvProtein);
        tvCarbs = findViewById(R.id.tvCarbs);
        tvFiber = findViewById(R.id.tvFiber);
        tvSugar = findViewById(R.id.tvSugar);

        // Allergen and ingredient views
        tvAllergens = findViewById(R.id.tvAllergens);
        tvIngredients = findViewById(R.id.tvIngredients);
    }

    /**
     * Sets up event listeners for interactive components
     */
    private void setupEventListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Extracts menu item name from intent and loads detailed information
     */
    private void loadMenuItemFromIntent() {
        Intent intent = getIntent();
        String itemName = intent.getStringExtra(EXTRA_ITEM_NAME);

        if (itemName != null) {
            loadMenuItemDetails(itemName);
        } else {
            // Handle case where no item name was provided
            tvFoodName.setText("Unknown Item");
            tvDescription.setText("No item information available");
        }
    }

    /**
     * Loads detailed menu item information from the database and populates the UI
     *
     * @param itemName The name of the menu item to load details for
     */
    private void loadMenuItemDetails(String itemName) {
        MenuItemDetailed item = dbHelper.getMenuItemDetails(itemName);

        if (item != null) {
            populateBasicInformation(item);
            populateNutritionInformation(item);
            populateAllergenInformation(item);
        } else {
            // Handle case where item is not found in database
            tvFoodName.setText(itemName);
            tvDescription.setText("Detailed information not available for this item");
        }
    }

    /**
     * Populates the basic information section (name, category, price, description)
     *
     * @param item The detailed menu item data
     */
    private void populateBasicInformation(MenuItemDetailed item) {
        tvFoodName.setText(item.getName());
        tvCategory.setText(item.getCategory());
        tvDescription.setText(item.getDescription());
        tvPrice.setText(PRICE_FORMAT.format(item.getPrice()));

        // TODO: Load actual image from item.getImagePath()
        // For now, keeping the placeholder image
        // Picasso.get().load(item.getImagePath()).into(ivFoodImage);
    }

    /**
     * Populates the nutrition information section
     *
     * @param item The detailed menu item data
     */
    private void populateNutritionInformation(MenuItemDetailed item) {
        tvCalories.setText(String.valueOf(item.getCalories()));
        tvFat.setText(formatNutritionValue(item.getFat()) + "g");
        tvProtein.setText(formatNutritionValue(item.getProtein()) + "g");
        tvCarbs.setText(formatNutritionValue(item.getCarbs()) + "g");
        tvFiber.setText(formatNutritionValue(item.getFiber()) + "g");
        tvSugar.setText(formatNutritionValue(item.getSugar()) + "g");
    }

    /**
     * Populates the allergen and ingredient information
     *
     * @param item The detailed menu item data
     */
    private void populateAllergenInformation(MenuItemDetailed item) {
        tvAllergens.setText(item.getAllergens());
        tvIngredients.setText(item.getIngredients());
    }

    /**
     * Formats nutrition values to display with appropriate decimal places
     *
     * @param value The nutrition value to format
     * @return Formatted string representation of the value
     */
    private String formatNutritionValue(double value) {
        return NUTRITION_FORMAT.format(value);
    }
}