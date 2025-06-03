package com.shivprakash.weathertrack.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.shivprakash.weathertrack.data.model.Weather;
import java.util.Date;
import java.util.List;

@Dao
public interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Weather weather);

    @Query("SELECT * FROM weather_table ORDER BY timestamp DESC")
    LiveData<List<Weather>> getAllWeatherData();

    @Query("SELECT * FROM weather_table WHERE timestamp >= :startDate ORDER BY timestamp DESC")
    LiveData<List<Weather>> getWeatherDataFromDate(Date startDate);

    @Query("SELECT * FROM weather_table WHERE date(timestamp/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    LiveData<List<Weather>> getWeatherForDay(Date date);

    @Query("SELECT * FROM weather_table ORDER BY timestamp DESC LIMIT 1")
    LiveData<Weather> getLatestWeather();

    @Query("DELETE FROM weather_table")
    void deleteAll();

    @Query("DELETE FROM weather_table WHERE timestamp < :beforeDate")
    void deleteOldData(Date beforeDate);

    @Query("SELECT COUNT(*) FROM weather_table WHERE timestamp = :timestamp")
    int getCountForTimestamp(Date timestamp);

    @Query("SELECT * FROM weather_table WHERE city = :city ORDER BY timestamp DESC LIMIT 1")
    LiveData<Weather> getLatestWeatherForCity(String city);
}