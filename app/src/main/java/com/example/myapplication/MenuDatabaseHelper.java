package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuDatabaseHelper - SQLite database manager for Sparty's Spreads menu data
 *
 * This singleton class manages the local SQLite database containing:
 * - Menu items with detailed nutrition information
 * - Hall-specific menu associations for different meal times
 * - Sample data for all 8 MSU dining halls
 *
 * Database Schema:
 * 1. menu_items table: Core food items with nutrition, allergen, and price data
 * 2. hall_menus table: Junction table linking halls + meal times to menu items
 *
 * Features:
 * - Singleton pattern for efficient database access
 * - Automatic sample data population
 * - Support for Breakfast, Lunch, and Dinner menus
 * - Hall-specific specialty items and menu variations
 *
 * Threading: All database operations should be called from background threads
 * except for getInstance() which is thread-safe
 */
public class MenuDatabaseHelper extends SQLiteOpenHelper {

    // Database Configuration
    private static final String DATABASE_NAME = "MenuDatabase.db";
    private static final int DATABASE_VERSION = 1;

    // MenuItems Table Schema
    private static final String TABLE_MENU_ITEMS = "menu_items";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_CALORIES = "calories";
    private static final String COLUMN_FAT = "fat";
    private static final String COLUMN_PROTEIN = "protein";
    private static final String COLUMN_CARBS = "carbs";
    private static final String COLUMN_FIBER = "fiber";
    private static final String COLUMN_SUGAR = "sugar";
    private static final String COLUMN_ALLERGENS = "allergens";
    private static final String COLUMN_INGREDIENTS = "ingredients";
    private static final String COLUMN_IMAGE_PATH = "image_path";
    private static final String COLUMN_PRICE = "price";

    // HallMenus Table Schema
    private static final String TABLE_HALL_MENUS = "hall_menus";
    private static final String COLUMN_HALL_NAME = "hall_name";
    private static final String COLUMN_MEAL_TIME = "meal_time";
    private static final String COLUMN_MENU_ITEM_ID = "menu_item_id";

    // Singleton instance
    private static MenuDatabaseHelper instance;

    /**
     * Returns the singleton instance of MenuDatabaseHelper
     * Thread-safe implementation using synchronized keyword
     *
     * @param context Application context (will use application context to prevent leaks)
     * @return Singleton instance of MenuDatabaseHelper
     */
    public static synchronized MenuDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MenuDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Private constructor to enforce singleton pattern
     *
     * @param context Application context for database creation
     */
    private MenuDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create MenuItems table
        String createMenuItemsTable = "CREATE TABLE " + TABLE_MENU_ITEMS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_CALORIES + " INTEGER, " +
                COLUMN_FAT + " REAL, " +
                COLUMN_PROTEIN + " REAL, " +
                COLUMN_CARBS + " REAL, " +
                COLUMN_FIBER + " REAL, " +
                COLUMN_SUGAR + " REAL, " +
                COLUMN_ALLERGENS + " TEXT, " +
                COLUMN_INGREDIENTS + " TEXT, " +
                COLUMN_IMAGE_PATH + " TEXT, " +
                COLUMN_PRICE + " REAL" +
                ")";
        db.execSQL(createMenuItemsTable);

        // Create HallMenus table
        String createHallMenusTable = "CREATE TABLE " + TABLE_HALL_MENUS + " (" +
                COLUMN_HALL_NAME + " TEXT, " +
                COLUMN_MEAL_TIME + " TEXT, " +
                COLUMN_MENU_ITEM_ID + " INTEGER, " +
                "PRIMARY KEY (" + COLUMN_HALL_NAME + ", " + COLUMN_MEAL_TIME + ", " + COLUMN_MENU_ITEM_ID + "), " +
                "FOREIGN KEY (" + COLUMN_MENU_ITEM_ID + ") REFERENCES " + TABLE_MENU_ITEMS + "(" + COLUMN_ID + ")" +
                ")";
        db.execSQL(createHallMenusTable);

        // Populate with sample data
        populateSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HALL_MENUS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU_ITEMS);
        onCreate(db);
    }

    private void populateSampleData(SQLiteDatabase db) {
        // Insert sample menu items
        insertMenuItem(db, "Scrambled Eggs", "Fresh scrambled eggs with herbs", "Main", 155, 10.6, 13.6, 1.1, 0.0, 1.1, "Eggs", "Eggs, butter, milk, salt, pepper, herbs", "placeholder_eggs.jpg", 3.50);
        insertMenuItem(db, "Pancakes", "Fluffy buttermilk pancakes", "Main", 227, 9.0, 6.0, 28.0, 1.4, 5.0, "Gluten, Eggs, Milk", "Flour, eggs, milk, butter, baking powder, sugar", "placeholder_pancakes.jpg", 4.25);
        insertMenuItem(db, "Bacon", "Crispy bacon strips", "Protein", 43, 3.3, 3.0, 0.1, 0.0, 0.0, "None", "Pork belly, salt, sodium nitrite", "placeholder_bacon.jpg", 2.75);
        insertMenuItem(db, "Fresh Fruit", "Seasonal fresh fruit selection", "Healthy", 62, 0.2, 0.9, 15.6, 2.4, 12.2, "None", "Mixed seasonal fruits", "placeholder_fruit.jpg", 2.50);

        insertMenuItem(db, "Grilled Chicken", "Herb-seasoned grilled chicken breast", "Main", 231, 5.0, 43.5, 0.0, 0.0, 0.0, "None", "Chicken breast, olive oil, herbs, spices", "placeholder_chicken.jpg", 6.75);
        insertMenuItem(db, "Caesar Salad", "Crisp romaine with Caesar dressing", "Salad", 470, 40.0, 10.0, 15.0, 3.0, 4.0, "Eggs, Fish, Milk", "Romaine lettuce, parmesan, croutons, caesar dressing", "placeholder_salad.jpg", 5.25);
        insertMenuItem(db, "Pizza", "Fresh made pizza with various toppings", "Main", 285, 10.4, 12.2, 35.6, 2.3, 3.8, "Gluten, Milk", "Pizza dough, tomato sauce, mozzarella, toppings", "placeholder_pizza.jpg", 4.50);

        insertMenuItem(db, "Grilled Salmon", "Atlantic salmon with lemon herbs", "Main", 231, 11.0, 31.0, 0.0, 0.0, 0.0, "Fish", "Salmon fillet, lemon, herbs, olive oil", "placeholder_salmon.jpg", 8.75);
        insertMenuItem(db, "Beef Stir Fry", "Tender beef with mixed vegetables", "Main", 250, 12.0, 26.0, 8.0, 3.0, 5.0, "Soy", "Beef strips, mixed vegetables, soy sauce, garlic", "placeholder_stirfry.jpg", 7.25);
        insertMenuItem(db, "Chocolate Cake", "Rich chocolate layer cake", "Dessert", 352, 14.0, 5.0, 56.0, 3.0, 45.0, "Gluten, Eggs, Milk", "Flour, cocoa, eggs, butter, sugar, milk", "placeholder_cake.jpg", 3.75);

        // Specialty items for specific halls
        insertMenuItem(db, "Belgian Waffles", "Authentic Belgian waffles with syrup", "Specialty", 310, 12.0, 8.0, 44.0, 2.0, 15.0, "Gluten, Eggs, Milk", "Waffle batter, maple syrup", "placeholder_waffles.jpg", 5.50);
        insertMenuItem(db, "Breakfast Burrito", "Eggs, cheese, and potato burrito", "Specialty", 380, 18.0, 16.0, 38.0, 4.0, 2.0, "Gluten, Eggs, Milk", "Tortilla, eggs, cheese, potatoes, peppers", "placeholder_burrito.jpg", 4.75);
        insertMenuItem(db, "Sushi Bar", "Fresh sushi and sashimi selection", "Specialty", 200, 2.0, 20.0, 30.0, 1.0, 5.0, "Fish, Soy", "Sushi rice, nori, fresh fish, wasabi", "placeholder_sushi.jpg", 12.50);
        insertMenuItem(db, "Taco Bar", "Build your own tacos", "Specialty", 320, 15.0, 18.0, 28.0, 5.0, 3.0, "Gluten, Milk", "Tortillas, meat, cheese, lettuce, tomatoes", "placeholder_tacos.jpg", 6.25);

        // Now populate hall menus
        populateHallMenus(db);
    }

    private void insertMenuItem(SQLiteDatabase db, String name, String description, String category,
                               int calories, double fat, double protein, double carbs, double fiber,
                               double sugar, String allergens, String ingredients, String imagePath, double price) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_CALORIES, calories);
        values.put(COLUMN_FAT, fat);
        values.put(COLUMN_PROTEIN, protein);
        values.put(COLUMN_CARBS, carbs);
        values.put(COLUMN_FIBER, fiber);
        values.put(COLUMN_SUGAR, sugar);
        values.put(COLUMN_ALLERGENS, allergens);
        values.put(COLUMN_INGREDIENTS, ingredients);
        values.put(COLUMN_IMAGE_PATH, imagePath);
        values.put(COLUMN_PRICE, price);
        db.insert(TABLE_MENU_ITEMS, null, values);
    }

    private void populateHallMenus(SQLiteDatabase db) {
        String[] halls = {"Brody", "Case", "Owen", "Shaw", "Akers", "Landon", "Holden", "Hubbard"};

        // Breakfast items (IDs 1-4 + specialty items)
        for (String hall : halls) {
            insertHallMenu(db, hall, "Breakfast", 1); // Scrambled Eggs
            insertHallMenu(db, hall, "Breakfast", 2); // Pancakes
            insertHallMenu(db, hall, "Breakfast", 3); // Bacon
            insertHallMenu(db, hall, "Breakfast", 4); // Fresh Fruit
        }

        // Specialty breakfast items
        insertHallMenu(db, "Brody", "Breakfast", 11); // Belgian Waffles
        insertHallMenu(db, "Case", "Breakfast", 12); // Breakfast Burrito

        // Lunch items (IDs 5-7 + specialty)
        for (String hall : halls) {
            insertHallMenu(db, hall, "Lunch", 5); // Grilled Chicken
            insertHallMenu(db, hall, "Lunch", 6); // Caesar Salad
            insertHallMenu(db, hall, "Lunch", 7); // Pizza
        }

        // Specialty lunch items
        insertHallMenu(db, "Owen", "Lunch", 13); // Sushi Bar
        insertHallMenu(db, "Shaw", "Lunch", 14); // Taco Bar

        // Dinner items (IDs 8-10)
        for (String hall : halls) {
            insertHallMenu(db, hall, "Dinner", 8); // Grilled Salmon
            insertHallMenu(db, hall, "Dinner", 9); // Beef Stir Fry
            insertHallMenu(db, hall, "Dinner", 10); // Chocolate Cake
        }
    }

    private void insertHallMenu(SQLiteDatabase db, String hallName, String mealTime, int menuItemId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_HALL_NAME, hallName);
        values.put(COLUMN_MEAL_TIME, mealTime);
        values.put(COLUMN_MENU_ITEM_ID, menuItemId);
        db.insert(TABLE_HALL_MENUS, null, values);
    }

    public List<MenuItem> getMenuItemsForHall(String hallName, String mealTime) {
        List<MenuItem> menuItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT mi." + COLUMN_NAME + ", mi." + COLUMN_DESCRIPTION + ", mi." + COLUMN_CATEGORY +
                      " FROM " + TABLE_MENU_ITEMS + " mi " +
                      "INNER JOIN " + TABLE_HALL_MENUS + " hm ON mi." + COLUMN_ID + " = hm." + COLUMN_MENU_ITEM_ID +
                      " WHERE hm." + COLUMN_HALL_NAME + " = ? AND hm." + COLUMN_MEAL_TIME + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{hallName, mealTime});

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                String description = cursor.getString(1);
                String category = cursor.getString(2);
                menuItems.add(new MenuItem(name, description, category));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return menuItems;
    }

    public MenuItemDetailed getMenuItemDetails(String itemName) {
        SQLiteDatabase db = this.getReadableDatabase();
        MenuItemDetailed item = null;

        String query = "SELECT * FROM " + TABLE_MENU_ITEMS + " WHERE " + COLUMN_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{itemName});

        if (cursor.moveToFirst()) {
            item = new MenuItemDetailed(
                cursor.getInt(0),      // id
                cursor.getString(1),   // name
                cursor.getString(2),   // description
                cursor.getString(3),   // category
                cursor.getInt(4),      // calories
                cursor.getDouble(5),   // fat
                cursor.getDouble(6),   // protein
                cursor.getDouble(7),   // carbs
                cursor.getDouble(8),   // fiber
                cursor.getDouble(9),   // sugar
                cursor.getString(10),  // allergens
                cursor.getString(11),  // ingredients
                cursor.getString(12),  // imagePath
                cursor.getDouble(13)   // price
            );
        }

        cursor.close();
        return item;
    }
}