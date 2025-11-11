package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MenuUpdateService {
    private static final String TAG = "MenuUpdateService";
    private static final String PREFS_NAME = "MenuUpdatePrefs";
    private static final String LAST_UPDATE_KEY = "last_update_";
    private static final long UPDATE_INTERVAL_MS = 3600000;

    private final Context context;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private MenuUpdateListener listener;

    public interface MenuUpdateListener {
        void onMenuUpdated(String hallName, boolean success, String message);
        void onAllMenusUpdated();
    }

    public MenuUpdateService(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setMenuUpdateListener(MenuUpdateListener listener) {
        this.listener = listener;
    }

    public void updateMenuForHall(String hallName, boolean forceUpdate) {
        updateMenuForHallAndDate(hallName, new Date(), forceUpdate);
    }

    public void updateMenuForHallAndDate(String hallName, Date date, boolean forceUpdate) {
        executorService.execute(() -> {
            try {
                if (!forceUpdate && !shouldUpdateHall(hallName)) {
                    Log.d(TAG, "Menu for " + hallName + " is up to date, skipping fetch");
                    notifyListener(hallName, true, "Menu is up to date");
                    return;
                }

                Log.d(TAG, "Fetching menu for " + hallName + " for date: " + date);
                MSUMenuScraper.MenuResult result = MSUMenuScraper.fetchMenuData(hallName, date);

                if (result.success) {
                    MenuDatabaseHelper dbHelper = MenuDatabaseHelper.getInstance(context);
                    dbHelper.updateDynamicMenu(hallName, result);

                    updateLastFetchTime(hallName);
                    notifyListener(hallName, true, "Menu updated successfully");
                } else {
                    notifyListener(hallName, false, result.error);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating menu for " + hallName, e);
                notifyListener(hallName, false, "Update failed: " + e.getMessage());
            }
        });
    }

    public void updateAllHallMenus(boolean forceUpdate) {
        executorService.execute(() -> {
            DiningHall[] halls = DiningHall.getAllDiningHalls();
            for (DiningHall hall : halls) {
                try {
                    if (!forceUpdate && !shouldUpdateHall(hall.getName())) {
                        Log.d(TAG, "Menu for " + hall.getName() + " is up to date");
                        continue;
                    }

                    Log.d(TAG, "Fetching menu for " + hall.getName());
                    MSUMenuScraper.MenuResult result = MSUMenuScraper.fetchMenuData(hall.getName(), new Date());

                    if (result.success) {
                        MenuDatabaseHelper dbHelper = MenuDatabaseHelper.getInstance(context);
                        dbHelper.updateDynamicMenu(hall.getName(), result);
                        updateLastFetchTime(hall.getName());
                    } else {
                        Log.e(TAG, "Failed to fetch menu for " + hall.getName() + ": " + result.error);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error updating menu for " + hall.getName(), e);
                }
            }

            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onAllMenusUpdated();
                }
            });
        });
    }

    private boolean shouldUpdateHall(String hallName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong(LAST_UPDATE_KEY + hallName, 0);
        long currentTime = System.currentTimeMillis();

        if (lastUpdate == 0) {
            return true;
        }

        long timeSinceUpdate = currentTime - lastUpdate;
        if (timeSinceUpdate > UPDATE_INTERVAL_MS) {
            return true;
        }

        Date lastUpdateDate = new Date(lastUpdate);
        Date currentDate = new Date();
        return !isSameDay(lastUpdateDate, currentDate);
    }

    private boolean isSameDay(Date date1, Date date2) {
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMdd");
        return fmt.format(date1).equals(fmt.format(date2));
    }

    private void updateLastFetchTime(String hallName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(LAST_UPDATE_KEY + hallName, System.currentTimeMillis()).apply();
    }

    private void notifyListener(String hallName, boolean success, String message) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onMenuUpdated(hallName, success, message);
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}