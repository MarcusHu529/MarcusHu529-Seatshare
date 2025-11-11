package com.example.myapplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuDatabase {
    private static MenuDatabase instance;
    private Map<String, Map<String, List<MenuItem>>> menuData;

    private MenuDatabase() {
        initializeMenuData();
    }

    public static MenuDatabase getInstance() {
        if (instance == null) {
            instance = new MenuDatabase();
        }
        return instance;
    }

    private void initializeMenuData() {
        menuData = new HashMap<>();

        // Initialize menus for all halls
        String[] halls = {"Brody", "Case", "Owen", "Shaw", "Akers", "Landon", "Holden", "Hubbard"};
        String[] mealTimes = {"Breakfast", "Lunch", "Dinner"};

        for (String hall : halls) {
            Map<String, List<MenuItem>> hallMenus = new HashMap<>();

            for (String mealTime : mealTimes) {
                List<MenuItem> items = createSampleMenu(hall, mealTime);
                hallMenus.put(mealTime, items);
            }

            menuData.put(hall, hallMenus);
        }
    }

    private List<MenuItem> createSampleMenu(String hall, String mealTime) {
        List<MenuItem> items = new ArrayList<>();

        switch (mealTime) {
            case "Breakfast":
                items.add(new MenuItem("Scrambled Eggs", "Fresh scrambled eggs with herbs", "Main"));
                items.add(new MenuItem("Pancakes", "Fluffy buttermilk pancakes", "Main"));
                items.add(new MenuItem("Bacon", "Crispy bacon strips", "Protein"));
                items.add(new MenuItem("Fresh Fruit", "Seasonal fresh fruit selection", "Healthy"));
                items.add(new MenuItem("Coffee", "Freshly brewed coffee", "Beverage"));
                items.add(new MenuItem("Orange Juice", "100% pure orange juice", "Beverage"));
                if (hall.equals("Brody")) {
                    items.add(new MenuItem("Belgian Waffles", "Authentic Belgian waffles with syrup", "Specialty"));
                } else if (hall.equals("Case")) {
                    items.add(new MenuItem("Breakfast Burrito", "Eggs, cheese, and potato burrito", "Specialty"));
                }
                break;

            case "Lunch":
                items.add(new MenuItem("Grilled Chicken", "Herb-seasoned grilled chicken breast", "Main"));
                items.add(new MenuItem("Caesar Salad", "Crisp romaine with Caesar dressing", "Salad"));
                items.add(new MenuItem("Pizza", "Fresh made pizza with various toppings", "Main"));
                items.add(new MenuItem("French Fries", "Golden crispy french fries", "Side"));
                items.add(new MenuItem("Soup of the Day", "Chef's daily soup selection", "Soup"));
                items.add(new MenuItem("Iced Tea", "Refreshing iced tea", "Beverage"));
                if (hall.equals("Owen")) {
                    items.add(new MenuItem("Sushi Bar", "Fresh sushi and sashimi selection", "Specialty"));
                } else if (hall.equals("Shaw")) {
                    items.add(new MenuItem("Taco Bar", "Build your own tacos", "Specialty"));
                }
                break;

            case "Dinner":
                items.add(new MenuItem("Grilled Salmon", "Atlantic salmon with lemon herbs", "Main"));
                items.add(new MenuItem("Beef Stir Fry", "Tender beef with mixed vegetables", "Main"));
                items.add(new MenuItem("Garlic Mashed Potatoes", "Creamy mashed potatoes with garlic", "Side"));
                items.add(new MenuItem("Steamed Broccoli", "Fresh steamed broccoli", "Vegetable"));
                items.add(new MenuItem("Dinner Rolls", "Warm dinner rolls with butter", "Bread"));
                items.add(new MenuItem("Chocolate Cake", "Rich chocolate layer cake", "Dessert"));
                if (hall.equals("Akers")) {
                    items.add(new MenuItem("Prime Rib", "Slow-roasted prime rib", "Specialty"));
                } else if (hall.equals("Landon")) {
                    items.add(new MenuItem("Pasta Station", "Made-to-order pasta dishes", "Specialty"));
                }
                break;
        }

        return items;
    }

    public List<MenuItem> getMenuItems(String hall, String mealTime) {
        Map<String, List<MenuItem>> hallMenus = menuData.get(hall);
        if (hallMenus != null) {
            List<MenuItem> items = hallMenus.get(mealTime);
            return items != null ? items : new ArrayList<>();
        }
        return new ArrayList<>();
    }
}