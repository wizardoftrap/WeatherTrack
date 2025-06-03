package com.shivprakash.weathertrack.worker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.shivprakash.weathertrack.data.local.WeatherDao;
import com.shivprakash.weathertrack.data.local.WeatherDatabase;
import com.shivprakash.weathertrack.data.model.OpenMeteoResponse;
import com.shivprakash.weathertrack.data.model.Weather;
import com.shivprakash.weathertrack.data.remote.RetrofitClient;
import com.shivprakash.weathertrack.data.remote.WeatherApiService;
import com.shivprakash.weathertrack.utils.Constants;
import com.shivprakash.weathertrack.utils.WeatherCodeMapper;
import retrofit2.Call;
import retrofit2.Response;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WeatherSyncWorker extends Worker {

    private static final String TAG = "WeatherSyncWorker";
    private final WeatherDao weatherDao;
    private final WeatherApiService apiService;
    private final SharedPreferences prefs;

    public WeatherSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        WeatherDatabase database = WeatherDatabase.getDatabase(context);
        weatherDao = database.weatherDao();
        apiService = RetrofitClient.getClient().create(WeatherApiService.class);
        prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting weather sync work");

        // Get saved location or use default
        double latitude = prefs.getFloat(Constants.PREF_LATITUDE, (float) Constants.DEFAULT_LATITUDE);
        double longitude = prefs.getFloat(Constants.PREF_LONGITUDE, (float) Constants.DEFAULT_LONGITUDE);
        String city = prefs.getString(Constants.PREF_CITY, Constants.DEFAULT_CITY);

        try {
            // Make synchronous API call
            Call<OpenMeteoResponse> call = apiService.getCurrentWeather(
                    latitude,
                    longitude,
                    true,
                    "temperature_2m,relativehumidity_2m",
                    "auto"
            );

            Response<OpenMeteoResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                OpenMeteoResponse apiResponse = response.body();

                // Get current hour index
                int currentHourIndex = 0;
                if (apiResponse.getHourly() != null &&
                        apiResponse.getHourly().getTime() != null &&
                        !apiResponse.getHourly().getTime().isEmpty()) {

                    String currentTime = apiResponse.getCurrentWeather().getTime();
                    List<String> times = apiResponse.getHourly().getTime();
                    for (int i = 0; i < times.size(); i++) {
                        if (times.get(i).startsWith(currentTime.substring(0, 13))) {
                            currentHourIndex = i;
                            break;
                        }
                    }
                }

                // Extract humidity from hourly data
                int humidity = 0;
                if (apiResponse.getHourly() != null &&
                        apiResponse.getHourly().getHumidity() != null &&
                        apiResponse.getHourly().getHumidity().size() > currentHourIndex) {
                    humidity = apiResponse.getHourly().getHumidity().get(currentHourIndex);
                }

                // Create Weather object
                Weather weather = new Weather(
                        apiResponse.getCurrentWeather().getTemperature(),
                        humidity,
                        WeatherCodeMapper.getWeatherCondition(apiResponse.getCurrentWeather().getWeatherCode()),
                        new Date(),
                        city,
                        apiResponse.getLatitude(),
                        apiResponse.getLongitude(),
                        apiResponse.getCurrentWeather().getWindSpeed()
                );

                // Save to database synchronously
                weatherDao.insert(weather);

                // Clean up old data
                deleteOldData();

                Log.d(TAG, "Weather sync successful for " + city);
                return Result.success();
            } else {
                Log.e(TAG, "Weather sync failed: Invalid response");
                return Result.retry();
            }

        } catch (Exception e) {
            Log.e(TAG, "Weather sync failed", e);
            return Result.retry();
        }
    }

    private void deleteOldData() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30); // Keep only last 30 days
        Date thirtyDaysAgo = calendar.getTime();
        weatherDao.deleteOldData(thirtyDaysAgo);
    }
}