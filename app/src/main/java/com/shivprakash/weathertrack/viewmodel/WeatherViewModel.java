package com.shivprakash.weathertrack.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.shivprakash.weathertrack.data.model.Weather;
import com.shivprakash.weathertrack.data.repository.WeatherRepository;
import com.shivprakash.weathertrack.utils.Constants;
import com.shivprakash.weathertrack.utils.Resource;
import java.util.Date;
import java.util.List;

public class WeatherViewModel extends AndroidViewModel {

    private final WeatherRepository repository;
    private final LiveData<List<Weather>> allWeatherData;
    private final LiveData<List<Weather>> weeklyWeatherData;

    public WeatherViewModel(@NonNull Application application) {
        super(application);
        repository = new WeatherRepository(application);
        allWeatherData = repository.getAllWeatherData();
        weeklyWeatherData = repository.getWeeklyWeatherData();
    }

    public LiveData<List<Weather>> getAllWeatherData() {
        return allWeatherData;
    }

    public LiveData<List<Weather>> getWeeklyWeatherData() {
        return weeklyWeatherData;
    }

    public LiveData<Weather> getLatestWeather() {
        return repository.getLatestWeather();
    }

    public LiveData<List<Weather>> getWeatherForDay(Date date) {
        return repository.getWeatherForDay(date);
    }

    public LiveData<Resource<Weather>> fetchWeather() {
        // Using default location for now
        return repository.fetchAndSaveWeather(
                Constants.DEFAULT_LATITUDE,
                Constants.DEFAULT_LONGITUDE,
                Constants.DEFAULT_CITY
        );
    }

    public LiveData<Resource<Weather>> fetchWeather(double latitude, double longitude, String city) {
        return repository.fetchAndSaveWeather(latitude, longitude, city);
    }

    // Add this new method
    public LiveData<Resource<List<Weather>>> fetchHistoricalWeather(double latitude, double longitude, String city) {
        return repository.fetchHistoricalWeather(latitude, longitude, city);
    }

    public void deleteOldData() {
        repository.deleteOldData();
    }
    public LiveData<Weather> getLatestWeatherForCity(String city) {
        return repository.getLatestWeatherForCity(city);
    }
}