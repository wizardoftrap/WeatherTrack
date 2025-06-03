package com.shivprakash.weathertrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.shivprakash.weathertrack.data.model.Weather;
import com.shivprakash.weathertrack.data.model.GeocodingResponse;
import com.shivprakash.weathertrack.data.remote.GeocodingApiService;
import com.shivprakash.weathertrack.data.remote.RetrofitClient;
import com.shivprakash.weathertrack.databinding.ActivityMainBinding;
import com.shivprakash.weathertrack.ui.summary.SummaryActivity;
import com.shivprakash.weathertrack.utils.Constants;
import com.shivprakash.weathertrack.utils.Resource;
import com.shivprakash.weathertrack.viewmodel.WeatherViewModel;
import com.shivprakash.weathertrack.worker.WeatherSyncWorker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private WeatherViewModel viewModel;
    private SimpleDateFormat dateFormat;
    private SharedPreferences prefs;
    private GeocodingApiService geocodingService;
    private String currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);

        // Initialize geocoding service
        Retrofit geocodingRetrofit = new Retrofit.Builder()
                .baseUrl(Constants.GEOCODING_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geocodingService = geocodingRetrofit.create(GeocodingApiService.class);

        viewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        // Load saved location
        currentCity = prefs.getString(Constants.PREF_CITY, Constants.DEFAULT_CITY);
        binding.cityInput.setText(currentCity);

        setupObservers();
        setupListeners();
        scheduleWeatherSync();

        // Fetch weather on app start
        fetchWeatherForCurrentLocation();
    }

    private void setupObservers() {
        // Observe latest weather only for the current city
        viewModel.getLatestWeather().observe(this, weather -> {
            if (weather != null && weather.getCity().equals(currentCity)) {
                updateUI(weather);
                binding.currentWeatherCard.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::fetchWeatherForCurrentLocation);

        binding.refreshButton.setOnClickListener(v -> fetchWeatherForCurrentLocation());

        binding.searchButton.setOnClickListener(v -> searchCity());

        binding.cityInput.setOnEditorActionListener((v, actionId, event) -> {
            searchCity();
            return true;
        });

        binding.viewSummaryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
            startActivity(intent);
        });
    }

    private void searchCity() {
        String cityName = binding.cityInput.getText().toString().trim();
        if (cityName.isEmpty()) {
            binding.cityInputLayout.setError("Please enter a city name");
            return;
        }

        binding.cityInputLayout.setError(null);
        hideKeyboard();
        showLoading(true);

        geocodingService.searchCity(cityName, 1, "en", "json").enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GeocodingResponse geocodingResponse = response.body();
                    if (geocodingResponse.getResults() != null && !geocodingResponse.getResults().isEmpty()) {
                        GeocodingResponse.GeocodingResult result = geocodingResponse.getResults().get(0);

                        // Update current city
                        currentCity = result.getFullName();

                        // Save location to preferences
                        prefs.edit()
                                .putFloat(Constants.PREF_LATITUDE, (float) result.getLatitude())
                                .putFloat(Constants.PREF_LONGITUDE, (float) result.getLongitude())
                                .putString(Constants.PREF_CITY, currentCity)
                                .apply();

                        // Fetch current weather
                        fetchWeather(result.getLatitude(), result.getLongitude(), currentCity);

                        // Also fetch historical data
                        fetchHistoricalData(result.getLatitude(), result.getLongitude(), currentCity);
                    } else {
                        showLoading(false);
                        showError("City not found. Please check the spelling and try again.");
                        binding.currentWeatherCard.setVisibility(View.VISIBLE);
                    }
                } else {
                    showLoading(false);
                    showError("Failed to search city. Please try again.");
                    binding.currentWeatherCard.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                showLoading(false);
                showError("Network error. Please check your connection.");
                binding.currentWeatherCard.setVisibility(View.VISIBLE);
            }
        });
    }

    private void fetchWeatherForCurrentLocation() {
        double latitude = prefs.getFloat(Constants.PREF_LATITUDE, (float) Constants.DEFAULT_LATITUDE);
        double longitude = prefs.getFloat(Constants.PREF_LONGITUDE, (float) Constants.DEFAULT_LONGITUDE);
        currentCity = prefs.getString(Constants.PREF_CITY, Constants.DEFAULT_CITY);

        fetchWeather(latitude, longitude, currentCity);
    }

    private void fetchWeather(double latitude, double longitude, String city) {
        showLoading(true);

        viewModel.fetchWeather(latitude, longitude, city).observe(this, resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        showLoading(true);
                        break;

                    case SUCCESS:
                        showLoading(false);
                        hideError();
                        if (resource.getData() != null) {
                            updateUI(resource.getData());
                            binding.currentWeatherCard.setVisibility(View.VISIBLE);
                        }
                        break;

                    case ERROR:
                        showLoading(false);
                        showError(resource.getMessage());
                        binding.currentWeatherCard.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    private void updateUI(Weather weather) {
        binding.cityName.setText(weather.getCity());
        binding.temperature.setText(String.format(Locale.getDefault(), "%.1f°C", weather.getTemperature()));
        binding.weatherCondition.setText(weather.getCondition());
        binding.humidity.setText(String.format(Locale.getDefault(), "%d%%", weather.getHumidity()));
        binding.windSpeed.setText(String.format(Locale.getDefault(), "%.1f km/h", weather.getWindSpeed()));
        binding.lastUpdated.setText(String.format(getString(R.string.last_updated),
                dateFormat.format(weather.getTimestamp())));
    }

    private void showLoading(boolean show) {
        binding.swipeRefresh.setRefreshing(show);
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        // Don't hide the card during loading to prevent UI flashing
    }

    private void showError(String message) {
        binding.infoCard.setVisibility(View.VISIBLE);
        binding.infoText.setText(message != null ? message : getString(R.string.error_generic));
        binding.infoCard.setCardBackgroundColor(getResources().getColor(R.color.error_background));
    }

    private void hideError() {
        binding.infoCard.setVisibility(View.GONE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void scheduleWeatherSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                WeatherSyncWorker.class,
                Constants.SYNC_INTERVAL_HOURS,
                TimeUnit.HOURS)
                .setConstraints(constraints)
                .addTag(Constants.WORK_TAG)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                Constants.WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );
    }

    private void fetchHistoricalData(double latitude, double longitude, String city) {
        viewModel.fetchHistoricalWeather(latitude, longitude, city).observe(this, resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case SUCCESS:
                        // Historical data fetched successfully
                        Toast.makeText(this, "Historical data loaded", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        // Handle error silently or show a subtle notification
                        // You can optionally log the error or show a less intrusive message
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
