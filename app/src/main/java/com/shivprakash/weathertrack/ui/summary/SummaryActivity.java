package com.shivprakash.weathertrack.ui.summary;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.shivprakash.weathertrack.data.model.Weather;
import com.shivprakash.weathertrack.databinding.ActivitySummaryBinding;
import com.shivprakash.weathertrack.databinding.DialogWeatherDetailBinding;
import com.shivprakash.weathertrack.viewmodel.WeatherViewModel;
import com.shivprakash.weathertrack.worker.WeatherSyncWorker;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SummaryActivity extends AppCompatActivity implements WeatherDayAdapter.OnDayClickListener {

    private ActivitySummaryBinding binding;
    private WeatherViewModel viewModel;
    private WeatherDayAdapter adapter;
    private SimpleDateFormat dateTimeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Last 7 Days Weather");
        }

        dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        viewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        setupRecyclerView();
        observeWeatherData();

        // Generate sample data for last 7 days if no data exists
        generateSampleDataIfNeeded();
    }

    private void setupRecyclerView() {
        adapter = new WeatherDayAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void observeWeatherData() {
        binding.progressBar.setVisibility(View.VISIBLE);

        viewModel.getWeeklyWeatherData().observe(this, weatherList -> {
            binding.progressBar.setVisibility(View.GONE);

            if (weatherList != null && !weatherList.isEmpty()) {
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.emptyView.setVisibility(View.GONE);
                adapter.submitList(weatherList);
            } else {
                binding.recyclerView.setVisibility(View.GONE);
                binding.emptyView.setVisibility(View.VISIBLE);
                binding.emptyView.setText("No weather data available for the last 7 days.\nPlease refresh to fetch current weather.");
            }
        });
    }

    private void generateSampleDataIfNeeded() {
        // This method will help populate some sample historical data
        // In a real app, you would accumulate this data over time
        viewModel.getWeeklyWeatherData().observe(this, weatherList -> {
            if (weatherList == null || weatherList.isEmpty()) {
                // No data exists, you might want to show a message
                binding.emptyView.setText("Weather history will be available as data is collected.\nData is saved every 6 hours.");
            }
        });
    }

    @Override
    public void onDayClick(DaySummary daySummary, List<Weather> weatherList) {
        showWeatherDetailDialog(weatherList);
    }

    private void showWeatherDetailDialog(List<Weather> weatherList) {
        if (weatherList.isEmpty()) return;

        DialogWeatherDetailBinding dialogBinding = DialogWeatherDetailBinding.inflate(getLayoutInflater());

        // Show details of the first weather entry for that day
        Weather weather = weatherList.get(0);
        dialogBinding.dialogTitle.setText(String.format("Weather Details - %s",
                new SimpleDateFormat("MMM dd", Locale.getDefault()).format(weather.getTimestamp())));
        dialogBinding.detailTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", weather.getTemperature()));
        dialogBinding.detailHumidity.setText(String.format(Locale.getDefault(), "%d%%", weather.getHumidity()));
        dialogBinding.detailWindSpeed.setText(String.format(Locale.getDefault(), "%.1f km/h", weather.getWindSpeed()));
        dialogBinding.detailCondition.setText(weather.getCondition());
        dialogBinding.detailTime.setText(dateTimeFormat.format(weather.getTimestamp()));

        new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}