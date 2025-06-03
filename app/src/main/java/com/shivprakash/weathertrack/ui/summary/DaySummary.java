package com.shivprakash.weathertrack.ui.summary;

import com.shivprakash.weathertrack.data.model.Weather;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DaySummary {
    public Date date;
    public double avgTemp = 0;
    public int avgHumidity = 0;
    public double minTemp = Double.MAX_VALUE;
    public double maxTemp = Double.MIN_VALUE;
    public String mostCommonCondition = "";
    private int count = 0;
    private Map<String, Integer> conditionCount = new HashMap<>();

    public DaySummary(Date date) {
        this.date = date;
    }

    public void addWeather(Weather weather) {
        // Update temperature stats
        avgTemp = (avgTemp * count + weather.getTemperature()) / (count + 1);
        minTemp = Math.min(minTemp, weather.getTemperature());
        maxTemp = Math.max(maxTemp, weather.getTemperature());

        // Update humidity
        avgHumidity = (int) ((avgHumidity * count + weather.getHumidity()) / (count + 1));

        // Track conditions
        String condition = weather.getCondition();
        // Replace getOrDefault with compatible code
        Integer currentCount = conditionCount.get(condition);
        if (currentCount == null) {
            currentCount = 0;
        }
        conditionCount.put(condition, currentCount + 1);

        // Find most common condition
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : conditionCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommonCondition = entry.getKey();
            }
        }

        count++;
    }
}