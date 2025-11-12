package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class MenuStation {
    private String stationName;
    private List<String> items;
    private boolean isExpanded;

    public MenuStation(String stationName) {
        this.stationName = stationName;
        this.items = new ArrayList<>();
        this.isExpanded = false;
    }

    public MenuStation(String stationName, List<String> items) {
        this.stationName = stationName;
        this.items = items != null ? items : new ArrayList<>();
        this.isExpanded = false;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public void addItem(String item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public void toggleExpanded() {
        isExpanded = !isExpanded;
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public String toString() {
        return stationName + " (" + getItemCount() + " items)";
    }
}