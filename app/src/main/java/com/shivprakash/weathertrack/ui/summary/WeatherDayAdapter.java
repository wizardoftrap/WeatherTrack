package com.shivprakash.weathertrack.ui.summary;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.shivprakash.weathertrack.data.model.Weather;
import com.shivprakash.weathertrack.databinding.ItemWeatherDayBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WeatherDayAdapter extends ListAdapter<Weather, WeatherDayAdapter.WeatherDayViewHolder> {

    private final OnDayClickListener listener;
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

    public interface OnDayClickListener {
        void onDayClick(DaySummary daySummary, List<Weather> weatherList);
    }

    public WeatherDayAdapter(OnDayClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Weather> DIFF_CALLBACK = new DiffUtil.ItemCallback<Weather>() {
        @Override
        public boolean areItemsTheSame(@NonNull Weather oldItem, @NonNull Weather newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Weather oldItem, @NonNull Weather newItem) {
            return oldItem.getTimestamp().equals(newItem.getTimestamp()) &&
                    oldItem.getTemperature() == newItem.getTemperature();
        }
    };

    @Override
    public void submitList(List<Weather> list) {
        // Group weather data by day
        Map<String, DaySummary> dayMap = new HashMap<>();
        Map<String, List<Weather>> dayWeatherMap = new HashMap<>();

        for (Weather weather : list) {
            String dateKey = dateFormat.format(weather.getTimestamp());

            if (!dayMap.containsKey(dateKey)) {
                dayMap.put(dateKey, new DaySummary(weather.getTimestamp()));
                dayWeatherMap.put(dateKey, new ArrayList<>());
            }

            DaySummary summary = dayMap.get(dateKey);
            summary.addWeather(weather);
            dayWeatherMap.get(dateKey).add(weather);
        }

        // Convert to list and sort by date
        List<DaySummary> summaries = new ArrayList<>(dayMap.values());
        // Use Collections.sort instead of List.sort for API 21 compatibility
        Collections.sort(summaries, new Comparator<DaySummary>() {
            @Override
            public int compare(DaySummary a, DaySummary b) {
                return b.date.compareTo(a.date); // Most recent first
            }
        });

        // Store the weather lists for click handling
        this.dayWeatherMap = dayWeatherMap;

        // Submit the processed list
        super.submitList(convertToWeatherList(summaries));
    }

    private Map<String, List<Weather>> dayWeatherMap = new HashMap<>();

    private List<Weather> convertToWeatherList(List<DaySummary> summaries) {
        List<Weather> result = new ArrayList<>();
        for (DaySummary summary : summaries) {
            // Create a dummy Weather object to represent the day
            Weather dayWeather = new Weather(
                    summary.avgTemp,
                    summary.avgHumidity,
                    summary.mostCommonCondition,
                    summary.date,
                    "",
                    0, 0, 0
            );
            result.add(dayWeather);
        }
        return result;
    }

    @NonNull
    @Override
    public WeatherDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWeatherDayBinding binding = ItemWeatherDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WeatherDayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherDayViewHolder holder, int position) {
        Weather weather = getItem(position);
        holder.bind(weather, position);
    }

    class WeatherDayViewHolder extends RecyclerView.ViewHolder {
        private final ItemWeatherDayBinding binding;

        WeatherDayViewHolder(ItemWeatherDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Weather weather, int position) {
            // Check if it's today
            Calendar today = Calendar.getInstance();
            Calendar weatherDate = Calendar.getInstance();
            weatherDate.setTime(weather.getTimestamp());

            boolean isToday = today.get(Calendar.YEAR) == weatherDate.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == weatherDate.get(Calendar.DAY_OF_YEAR);

            binding.dateText.setText(isToday ? "Today" : dateFormat.format(weather.getTimestamp()));
            binding.dayText.setText(dayFormat.format(weather.getTimestamp()));
            binding.avgTempText.setText(String.format(Locale.getDefault(), "%.1f°C", weather.getTemperature()));
            binding.avgHumidityText.setText(String.format(Locale.getDefault(), "%d%%", weather.getHumidity()));
            binding.conditionText.setText(weather.getCondition());

            // Get the day's weather list for min/max calculation
            String dateKey = dateFormat.format(weather.getTimestamp());
            List<Weather> dayWeathers = dayWeatherMap.get(dateKey);

            if (dayWeathers != null && !dayWeathers.isEmpty()) {
                // Replace stream API with traditional loop
                double minTemp = Double.MAX_VALUE;
                double maxTemp = Double.MIN_VALUE;

                for (Weather w : dayWeathers) {
                    minTemp = Math.min(minTemp, w.getTemperature());
                    maxTemp = Math.max(maxTemp, w.getTemperature());
                }

                binding.minTempText.setText(String.format(Locale.getDefault(), "%.1f°C", minTemp));
                binding.maxTempText.setText(String.format(Locale.getDefault(), "%.1f°C", maxTemp));
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null && dayWeathers != null) {
                    DaySummary summary = new DaySummary(weather.getTimestamp());
                    for (Weather w : dayWeathers) {
                        summary.addWeather(w);
                    }
                    listener.onDayClick(summary, dayWeathers);
                }
            });
        }
    }
}