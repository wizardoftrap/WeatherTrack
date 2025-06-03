package com.shivprakash.weathertrack.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HistoricalWeatherResponse {

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("hourly")
    private HourlyData hourly;

    @SerializedName("daily")
    private DailyData daily;

    public static class HourlyData {
        @SerializedName("time")
        private List<String> time;

        @SerializedName("temperature_2m")
        private List<Double> temperature;

        @SerializedName("relative_humidity_2m")
        private List<Integer> humidity;

        @SerializedName("wind_speed_10m")
        private List<Double> windSpeed;

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

        public List<Double> getWindSpeed() {
            return windSpeed;
        }
    }

    public static class DailyData {
        @SerializedName("time")
        private List<String> time;

        @SerializedName("temperature_2m_max")
        private List<Double> temperatureMax;

        @SerializedName("temperature_2m_min")
        private List<Double> temperatureMin;

        @SerializedName("weather_code")
        private List<Integer> weatherCode;

        // Getters
        public List<String> getTime() {
            return time;
        }

        public List<Double> getTemperatureMax() {
            return temperatureMax;
        }

        public List<Double> getTemperatureMin() {
            return temperatureMin;
        }

        public List<Integer> getWeatherCode() {
            return weatherCode;
        }
    }

    // Getters
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public HourlyData getHourly() {
        return hourly;
    }

    public DailyData getDaily() {
        return daily;
    }
}