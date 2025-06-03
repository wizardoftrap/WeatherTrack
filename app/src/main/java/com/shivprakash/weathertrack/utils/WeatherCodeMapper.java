package com.shivprakash.weathertrack.utils;

public class WeatherCodeMapper {
    public static String getWeatherCondition(int code) {
        if (code == 0) return "Clear sky";
        else if (code <= 3) return "Partly cloudy";
        else if (code <= 48) return "Foggy";
        else if (code <= 57) return "Drizzle";
        else if (code <= 67) return "Rain";
        else if (code <= 77) return "Snow";
        else if (code <= 82) return "Rain showers";
        else if (code <= 86) return "Snow showers";
        else if (code <= 99) return "Thunderstorm";
        else return "Unknown";
    }
}