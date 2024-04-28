package com.nhlanhlankosi.tablayoutdemo.infrastructure;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nhlanhlankosi.tablayoutdemo.models.CowLocation;
import com.nhlanhlankosi.tablayoutdemo.models.Notification;
import com.nhlanhlankosi.tablayoutdemo.models.User;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "preferences";
    private static final String NOTIFICATIONS_KEY = "notifications";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static <T> void saveObject(Context context, String key, T object) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(object);
        editor.putString(key, json);
        editor.apply();
    }

    private static <T> T getObject(Context context, String key, Class<T> classType) {
        String json = getSharedPreferences(context).getString(key, null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, classType);
        }
        return null;
    }

    public static void saveUser(Context context, User user) {
        saveObject(context, "user", user);
    }

    public static User getUser(Context context) {
        return getObject(context, "user", User.class);
    }

    public static void saveCowLocation(Context context, CowLocation location) {
        saveObject(context, "cow_location", location);
    }

    public static CowLocation getCowLocation(Context context) {
        return getObject(context, "cow_location", CowLocation.class);
    }

    public static void deleteUser(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove("user");
        editor.apply();
    }

    public static void deleteLocation(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove("location");
        editor.apply();
    }

    public static void saveNotifications(Context context, ArrayList<Notification> notifications) {
        saveObject(context, NOTIFICATIONS_KEY, notifications);
    }

    public static ArrayList<Notification> getNotifications(Context context) {
        String json = getSharedPreferences(context).getString(NOTIFICATIONS_KEY, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Notification>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return null;
    }

    public static void deleteNotifications(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(NOTIFICATIONS_KEY);
        editor.apply();
    }
}

