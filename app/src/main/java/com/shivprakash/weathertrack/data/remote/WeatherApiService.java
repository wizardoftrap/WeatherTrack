package com.shivprakash.weathertrack.data.remote;

import com.shivprakash.weathertrack.data.model.OpenMeteoResponse;
import com.shivprakash.weathertrack.data.model.HistoricalWeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("v1/forecast")
    Call<OpenMeteoResponse> getCurrentWeather(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current_weather") boolean currentWeather,
            @Query("hourly") String hourlyParams,
            @Query("timezone") String timezone
    );

    // Add this new method for historical data
    @GET("v1/forecast")
    Call<HistoricalWeatherResponse> getHistoricalWeather(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Query("hourly") String hourlyParams,
            @Query("daily") String dailyParams,
            @Query("timezone") String timezone
    );
}