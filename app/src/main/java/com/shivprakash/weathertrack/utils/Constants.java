package com.shivprakash.weathertrack.utils;

public class Constants {
    public static final String DATABASE_NAME = "weather_database";
    public static final String BASE_URL = "https://api.open-meteo.com/";
    public static final String GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/";
    public static final String WORK_TAG = "weather_sync_work";
    public static final int SYNC_INTERVAL_HOURS = 6;

    // Default location (New York City)
    public static final double DEFAULT_LATITUDE = 40.7128;
    public static final double DEFAULT_LONGITUDE = -74.0060;
    public static final String DEFAULT_CITY = "New York";

    // SharedPreferences keys
    public static final String PREF_NAME = "WeatherTrackPrefs";
    public static final String PREF_LATITUDE = "latitude";
    public static final String PREF_LONGITUDE = "longitude";
    public static final String PREF_CITY = "city";
}