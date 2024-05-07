package com.fenris.motion2coach.util;


import android.content.Context;
import android.content.SharedPreferences;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Cookie;

public class SessionManager {


    private static final String PREF_NAME = "m2cpref";

    public static void  clearsession(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        editor.commit();
    }

    public static void putStringPref(String key, String value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
        editor.commit();
    }
    public static void putBoolPref(String key, Boolean value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
        editor.commit();
    }

    public static String getStringPref(String key, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }
    public static Boolean getBoolPref(String key, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(key, false);
    }


    public static void saveArrayList(List<Cookie> list, String key,@NotNull Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();

    }


    public static List<Cookie> getArrayList(String key, Context context){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(key, null);
        Type type = new TypeToken<List<Cookie>>() {}.getType();
        return gson.fromJson(json, type);
    }


}