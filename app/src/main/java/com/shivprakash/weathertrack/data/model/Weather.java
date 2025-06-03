package com.shivprakash.weathertrack.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "weather_table")
public class Weather {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private double temperature;
    private int humidity;
    private String condition;
    private Date timestamp;
    private String city;
    private double latitude;
    private double longitude;
    private double windSpeed;

    // Constructor
    public Weather(double temperature, int humidity, String condition,
                   Date timestamp, String city, double latitude, double longitude, double windSpeed) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.condition = condition;
        this.timestamp = timestamp;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.windSpeed = windSpeed;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }
}