package com.shivprakash.weathertrack.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OpenMeteoResponse {
    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("current_weather")
    private CurrentWeather currentWeather;

    @SerializedName("hourly")
    private HourlyData hourly;

    // Inner class for current weather
    public static class CurrentWeather {
        @SerializedName("temperature")
        private double temperature;

        @SerializedName("windspeed")
        private double windSpeed;

        @SerializedName("weathercode")
        private int weatherCode;

        @SerializedName("time")
        private String time;

        // Getters
        public double getTemperature() {
            return temperature;
        }

        public double getWindSpeed() {
            return windSpeed;
        }

        public int getWeatherCode() {
            return weatherCode;
        }

        public String getTime() {
            return time;
        }
    }

    // Inner class for hourly data
    public static class HourlyData {
        @SerializedName("time")
        private List<String> time;

        @SerializedName("temperature_2m")
        private List<Double> temperature;

        @SerializedName("relativehumidity_2m")
        private List<Integer> humidity;

        // Getters
        public List<String> getTime() {
            return time;
        }

        public List<Double> getTemperature() {
            return temperature;
        }

        public List<Integer> getHumidity() {
            return humidity;
        }
    }

    // Getters
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public CurrentWeather getCurrentWeather() {
        return currentWeather;
    }

    public HourlyData getHourly() {
        return hourly;
    }
}