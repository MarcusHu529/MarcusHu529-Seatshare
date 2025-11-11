package com.example.myapplication;

import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MSUMenuScraper {
    private static final String TAG = "MSUMenuScraper";
    private static final String BASE_URL = "https://eatatstate.msu.edu/menu/";

    private static final Map<String, String[]> DINING_HALLS_MAP = new HashMap<>();

    static {
        DINING_HALLS_MAP.put("akers", new String[]{"The Edge at Akers", "The%20Edge%20at%20Akers"});
        DINING_HALLS_MAP.put("brody", new String[]{"Brody Square", "Brody%20Square"});
        DINING_HALLS_MAP.put("case", new String[]{"Case Hall (South Pointe)", "South%20Pointe%20at%20Case"});
        DINING_HALLS_MAP.put("holden", new String[]{"Holden Hall (Sparty's Market)", "Sparty%27s%20Market%20at%20Holden"});
        DINING_HALLS_MAP.put("holmes", new String[]{"Holmes Hall (Sparty's Market)", "Sparty%27s%20Market%20at%20Holmes"});
        DINING_HALLS_MAP.put("landon", new String[]{"Landon Hall (Heritage Commons)", "Heritage%20Commons%20at%20Landon"});
        DINING_HALLS_MAP.put("owen", new String[]{"Owen Hall (Thrive)", "Thrive%20at%20Owen"});
        DINING_HALLS_MAP.put("shaw", new String[]{"Shaw Hall (The Vista)", "The%20Vista%20at%20Shaw"});
        DINING_HALLS_MAP.put("snyphi", new String[]{"Snyder-Phillips (The Gallery)", "The%20Gallery%20at%20Snyder%20Phillips"});
    }

    private static final Map<String, Integer> MEAL_SORT_ORDER = new HashMap<>();
    static {
        MEAL_SORT_ORDER.put("Breakfast", 0);
        MEAL_SORT_ORDER.put("Lunch", 1);
        MEAL_SORT_ORDER.put("Dinner", 2);
        MEAL_SORT_ORDER.put("Late Night", 3);
    }

    public static class Station {
        public String stationName;
        public List<Meal> meals = new ArrayList<>();

        public Station(String name) {
            this.stationName = name;
        }
    }

    public static class Meal {
        public String mealName;
        public List<String> items = new ArrayList<>();

        public Meal(String name) {
            this.mealName = name;
        }
    }

    public static class MenuResult {
        public boolean success;
        public String error;
        public List<Station> stations;
        public String hallName;
        public String date;

        private MenuResult(boolean success) {
            this.success = success;
            if (success) {
                this.stations = new ArrayList<>();
            }
        }

        public static MenuResult success() {
            return new MenuResult(true);
        }

        public static MenuResult error(String error, String hall, String date) {
            MenuResult result = new MenuResult(false);
            result.error = error;
            result.hallName = hall;
            result.date = date;
            return result;
        }
    }

    public static String getHallSlugFromName(String hallName) {
        for (Map.Entry<String, String[]> entry : DINING_HALLS_MAP.entrySet()) {
            if (entry.getValue()[0].contains(hallName) ||
                hallName.toLowerCase().contains(entry.getKey())) {
                return entry.getValue()[1];
            }
        }

        if (hallName.equalsIgnoreCase("Snyder-Phillips")) {
            return DINING_HALLS_MAP.get("snyphi")[1];
        }

        return null;
    }

    public static MenuResult fetchMenuData(String hallName, Date date) {
        String hallSlug = getHallSlugFromName(hallName);
        if (hallSlug == null) {
            return MenuResult.error("Unknown dining hall: " + hallName, hallName,
                                   new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date));
        }

        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
        String url = BASE_URL + hallSlug + "/all/" + dateStr;

        Log.d(TAG, "Fetching menu from: " + url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            MenuResult result = MenuResult.success();
            result.hallName = hallName;
            result.date = dateStr;

            Elements stationGroups = doc.select("div.eas-view-group");

            if (stationGroups.isEmpty()) {
                return MenuResult.error("No menu data found for this date", hallName, dateStr);
            }

            for (Element group : stationGroups) {
                Element stationTag = group.selectFirst("h3.venue-title");
                if (stationTag == null) continue;

                String stationName = stationTag.text().trim();
                Station station = new Station(stationName);

                Elements mealLists = group.select("div.eas-list");

                for (Element mealList : mealLists) {
                    Element mealTag = mealList.selectFirst("div.meal-time");
                    String mealName = mealTag != null ? mealTag.text().trim() : "Unknown Meal";

                    Meal meal = new Meal(mealName);

                    Element itemUl = mealList.selectFirst("ul");
                    if (itemUl != null) {
                        Elements items = itemUl.select("li.menu-item");
                        for (Element item : items) {
                            Element itemTitleTag = item.selectFirst("div.meal-title");
                            if (itemTitleTag != null) {
                                meal.items.add(itemTitleTag.text().trim());
                            }
                        }
                    }

                    if (!meal.items.isEmpty()) {
                        station.meals.add(meal);
                    }
                }

                Collections.sort(station.meals, new Comparator<Meal>() {
                    @Override
                    public int compare(Meal m1, Meal m2) {
                        int order1 = MEAL_SORT_ORDER.getOrDefault(m1.mealName, 99);
                        int order2 = MEAL_SORT_ORDER.getOrDefault(m2.mealName, 99);
                        return Integer.compare(order1, order2);
                    }
                });

                if (!station.meals.isEmpty()) {
                    result.stations.add(station);
                }
            }

            return result;

        } catch (IOException e) {
            Log.e(TAG, "Network error fetching menu", e);
            return MenuResult.error("Network error: " + e.getMessage(), hallName, dateStr);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing menu data", e);
            return MenuResult.error("Parse error: " + e.getMessage(), hallName, dateStr);
        }
    }

    public static List<String> getAllItemsForMealTime(MenuResult menuResult, String mealTime) {
        List<String> allItems = new ArrayList<>();
        if (menuResult.success && menuResult.stations != null) {
            for (Station station : menuResult.stations) {
                for (Meal meal : station.meals) {
                    if (meal.mealName.equalsIgnoreCase(mealTime)) {
                        allItems.addAll(meal.items);
                    }
                }
            }
        }
        return allItems;
    }

    public static Map<String, List<String>> getItemsByStation(MenuResult menuResult, String mealTime) {
        Map<String, List<String>> itemsByStation = new HashMap<>();
        if (menuResult.success && menuResult.stations != null) {
            for (Station station : menuResult.stations) {
                for (Meal meal : station.meals) {
                    if (meal.mealName.equalsIgnoreCase(mealTime)) {
                        itemsByStation.put(station.stationName, new ArrayList<>(meal.items));
                    }
                }
            }
        }
        return itemsByStation;
    }
}