/*
* Homework 06
* SharedPreferences.java
* Hozefa Haveliwala, Nikhil Nagori Group 29
* */

package com.inclass.weatherapp.SharedPreference;

import android.content.Context;

public class MySharedPreferences {
    private static final String USER_PREFS = "WEATHER";
    private static final String USER_PREFS_CITY_NAME = "CITY_NAME";
    private static final String USER_PREFS_COUNTRY_NAME = "COUNTRY_NAME";
    private static final String USER_PREFS_CITY_KEY = "CITY_KEY";
    private static final String USER_PREFS_TEMP = "TEMP";
    private android.content.SharedPreferences.Editor editor = null;
    private android.content.SharedPreferences preferences = null;
    private Context c;

    public MySharedPreferences(Context context) {
        c = context;
        preferences = context.getApplicationContext().getSharedPreferences(
                USER_PREFS, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void setUserPrefsCityName(String s) {
        editor.putString(USER_PREFS_CITY_NAME, s);
        editor.commit();
    }

    public void setUserPrefsCountryName(String s) {
        editor.putString(USER_PREFS_COUNTRY_NAME, s);
        editor.commit();
    }

    public void setUserPrefsCityKey(String s) {
        editor.putString(USER_PREFS_CITY_KEY, s);
        editor.commit();
    }

    public void setUserPrefsTemp(String s) {
        editor.putString(USER_PREFS_TEMP, s);
        editor.commit();
    }

    public String getUserPrefsCityName(){
        return preferences.getString(USER_PREFS_CITY_NAME,"");
    }

    public String getUserPrefsCountryName(){
        return preferences.getString(USER_PREFS_COUNTRY_NAME,"");
    }

    public String getUserPrefsCityKey(){
        return preferences.getString(USER_PREFS_CITY_KEY,"");
    }

    public String getUserPrefsTemp(){
        return preferences.getString(USER_PREFS_TEMP,"");
    }

    public void clearPreference(){
        editor.clear();
        editor.commit();
    }
}
