package com.example.myapplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiningHallMenu {
    private String hallName;
    private String date;
    private Map<String, List<MenuStation>> mealStations;
    private boolean hasData;
    private String errorMessage;

    public DiningHallMenu(String hallName) {
        this.hallName = hallName;
        this.mealStations = new HashMap<>();
        this.hasData = false;
    }

    public void addStation(String mealTime, MenuStation station) {
        if (!mealStations.containsKey(mealTime)) {
            mealStations.put(mealTime, new ArrayList<>());
        }
        mealStations.get(mealTime).add(station);
        hasData = true;
    }

    public List<MenuStation> getStationsForMeal(String mealTime) {
        return mealStations.getOrDefault(mealTime, new ArrayList<>());
    }

    public boolean hasStationsForMeal(String mealTime) {
        return mealStations.containsKey(mealTime) && !mealStations.get(mealTime).isEmpty();
    }

    public List<String> getAvailableMealTimes() {
        return new ArrayList<>(mealStations.keySet());
    }

    public int getTotalItemsForMeal(String mealTime) {
        List<MenuStation> stations = getStationsForMeal(mealTime);
        int total = 0;
        for (MenuStation station : stations) {
            total += station.getItemCount();
        }
        return total;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean hasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.hasData = false;
    }

    public void clearMealData(String mealTime) {
        mealStations.remove(mealTime);
    }

    public void clearAllData() {
        mealStations.clear();
        hasData = false;
    }
}