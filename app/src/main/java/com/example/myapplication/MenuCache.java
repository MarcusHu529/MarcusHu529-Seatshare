package com.example.myapplication;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

public final class MenuCache {
    private static final Gson gson = new Gson();

    private MenuCache() {}

    // Build a filename like: menu_Brody_Breakfast.json
    private static String fileNameFor(String hallName, String mealTime) {
        String safeHall = hallName == null ? "Unknown" : hallName.replaceAll("\\W+", "_");
        String safeMeal = mealTime == null ? "Meal" : mealTime.replaceAll("\\W+", "_");
        return "menu_" + safeHall + "_" + safeMeal + ".json";
    }

    public static void saveMenu(Context ctx, String hallName, String mealTime, List<MenuItem> items) {
        if (items == null) return;
        String fn = fileNameFor(hallName, mealTime);
        try (FileOutputStream fos = ctx.openFileOutput(fn, Context.MODE_PRIVATE);
             OutputStreamWriter osw = new OutputStreamWriter(fos);
             BufferedWriter bw = new BufferedWriter(osw)) {
            String json = gson.toJson(items);
            bw.write(json);
        } catch (IOException ignored) { }
    }

    public static List<MenuItem> loadMenu(Context ctx, String hallName, String mealTime) {
        String fn = fileNameFor(hallName, mealTime);
        File f = new File(ctx.getFilesDir(), fn);
        if (!f.exists()) return null;

        try (FileInputStream fis = ctx.openFileInput(fn);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            String json = sb.toString();
            if (TextUtils.isEmpty(json)) return null;

            Type listType = new TypeToken<List<MenuItem>>(){}.getType();
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            return null;
        }
    }
}
