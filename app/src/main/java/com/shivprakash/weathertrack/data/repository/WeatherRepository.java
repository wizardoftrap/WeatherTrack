package com.shivprakash.weathertrack.data.repository;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.shivprakash.weathertrack.data.local.WeatherDao;
import com.shivprakash.weathertrack.data.local.WeatherDatabase;
import com.shivprakash.weathertrack.data.model.HistoricalWeatherResponse;
import com.shivprakash.weathertrack.data.model.OpenMeteoResponse;
import com.shivprakash.weathertrack.data.model.Weather;
import com.shivprakash.weathertrack.data.remote.HistoricalRetrofitClient;
import com.shivprakash.weathertrack.data.remote.RetrofitClient;
import com.shivprakash.weathertrack.data.remote.WeatherApiService;
import com.shivprakash.weathertrack.utils.Constants;
import com.shivprakash.weathertrack.utils.NetworkUtils;
import com.shivprakash.weathertrack.utils.Resource;
import com.shivprakash.weathertrack.utils.WeatherCodeMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WeatherRepository {

    private final WeatherDao weatherDao;
    private final WeatherApiService apiService;
    private final WeatherApiService historicalApiService;
    private final Context application;
    private final LiveData<List<Weather>> allWeatherData;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public WeatherRepository(Context context) {
        this.application = context.getApplicationContext();
        WeatherDatabase database = WeatherDatabase.getDatabase(context);
        weatherDao = database.weatherDao();
        apiService = RetrofitClient.getClient().create(WeatherApiService.class);
        historicalApiService = HistoricalRetrofitClient.getClient().create(WeatherApiService.class);
        allWeatherData = weatherDao.getAllWeatherData();
    }

    public LiveData<List<Weather>> getAllWeatherData() {
        return allWeatherData;
    }

    public LiveData<List<Weather>> getWeeklyWeatherData() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date sevenDaysAgo = calendar.getTime();
        return weatherDao.getWeatherDataFromDate(sevenDaysAgo);
    }

    public LiveData<List<Weather>> getWeatherForDay(Date date) {
        return weatherDao.getWeatherForDay(date);
    }

    public LiveData<Weather> getLatestWeather() {
        return weatherDao.getLatestWeather();
    }

    // Add this new method to fetch historical data
    public LiveData<Resource<List<Weather>>> fetchHistoricalWeather(double latitude, double longitude, String city) {
        MutableLiveData<Resource<List<Weather>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        if (!NetworkUtils.isNetworkAvailable(application)) {
            result.setValue(Resource.error("No internet connection", null));
            return result;
        }

        // Calculate date range (last 7 days)
        Calendar calendar = Calendar.getInstance();
        String endDate = dateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        String startDate = dateFormat.format(calendar.getTime());

        Call<HistoricalWeatherResponse> call = historicalApiService.getHistoricalWeather(
                latitude,
                longitude,
                startDate,
                endDate,
                "temperature_2m,relative_humidity_2m,wind_speed_10m",
                "temperature_2m_max,temperature_2m_min,weather_code",
                "auto"
        );

        call.enqueue(new Callback<HistoricalWeatherResponse>() {
            @Override
            public void onResponse(Call<HistoricalWeatherResponse> call, Response<HistoricalWeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HistoricalWeatherResponse historicalResponse = response.body();
                    List<Weather> weatherList = new ArrayList<>();

                    if (historicalResponse.getHourly() != null &&
                            historicalResponse.getHourly().getTime() != null &&
                            historicalResponse.getDaily() != null) {

                        List<String> times = historicalResponse.getHourly().getTime();
                        List<Double> temperatures = historicalResponse.getHourly().getTemperature();
                        List<Integer> humidities = historicalResponse.getHourly().getHumidity();
                        List<Double> windSpeeds = historicalResponse.getHourly().getWindSpeed();

                        // Get daily weather codes
                        List<String> dailyTimes = historicalResponse.getDaily().getTime();
                        List<Integer> dailyWeatherCodes = historicalResponse.getDaily().getWeatherCode();

                        // Create a map of date to weather code
                        Map<String, Integer> dateToWeatherCode = new HashMap<>();
                        SimpleDateFormat dailyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        for (int i = 0; i < dailyTimes.size() && i < dailyWeatherCodes.size(); i++) {
                            dateToWeatherCode.put(dailyTimes.get(i), dailyWeatherCodes.get(i));
                        }

                        // Process ALL hourly data
                        for (int i = 0; i < times.size(); i++) {
                            try {
                                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
                                Date timestamp = isoFormat.parse(times.get(i));

                                // Get the date part to look up weather code
                                String dateKey = dailyFormat.format(timestamp);
                                Integer weatherCode = dateToWeatherCode.get(dateKey);

                                // Use weather code to get condition
                                String condition = weatherCode != null ?
                                        WeatherCodeMapper.getWeatherCondition(weatherCode) : "Unknown";

                                Weather weather = new Weather(
                                        temperatures.get(i),
                                        humidities.get(i),
                                        condition,
                                        timestamp,
                                        city,
                                        latitude,
                                        longitude,
                                        windSpeeds.get(i)
                                );

                                weatherList.add(weather);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // Save all historical data to database
                        WeatherDatabase.getDatabaseWriteExecutor().execute(() -> {
                            for (Weather weather : weatherList) {
                                // Check if this timestamp already exists to avoid duplicates
                                int count = weatherDao.getCountForTimestamp(weather.getTimestamp());
                                if (count == 0) {
                                    weatherDao.insert(weather);
                                }
                            }
                        });

                        result.setValue(Resource.success(weatherList));
                    } else {
                        result.setValue(Resource.error("No historical data available", null));
                    }
                } else {
                    result.setValue(Resource.error("Failed to get historical weather data", null));
                }
            }

            @Override
            public void onFailure(Call<HistoricalWeatherResponse> call, Throwable t) {
                result.setValue(Resource.error("Error: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Weather>> fetchAndSaveWeather(double latitude, double longitude, String city) {
        MutableLiveData<Resource<Weather>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        if (!NetworkUtils.isNetworkAvailable(application)) {
            result.setValue(Resource.error("No internet connection", null));
            return result;
        }

        Call<OpenMeteoResponse> call = apiService.getCurrentWeather(
                latitude,
                longitude,
                true,
                "temperature_2m,relativehumidity_2m",
                "auto"
        );

        call.enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoResponse> call, Response<OpenMeteoResponse> response) {
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

                    // Save to database
                    WeatherDatabase.getDatabaseWriteExecutor().execute(() -> {
                        weatherDao.insert(weather);
                    });

                    result.setValue(Resource.success(weather));

                    // Also fetch historical data when current weather is fetched
                    fetchHistoricalWeather(latitude, longitude, city);

                } else {
                    result.setValue(Resource.error("Failed to get weather data", null));
                }
            }

            @Override
            public void onFailure(Call<OpenMeteoResponse> call, Throwable t) {
                result.setValue(Resource.error("Error: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public void deleteOldData() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30); // Keep only last 30 days
        Date thirtyDaysAgo = calendar.getTime();

        WeatherDatabase.getDatabaseWriteExecutor().execute(() -> {
            weatherDao.deleteOldData(thirtyDaysAgo);
        });
    }
    public LiveData<Weather> getLatestWeatherForCity(String city) {
        return weatherDao.getLatestWeatherForCity(city);
    }
}