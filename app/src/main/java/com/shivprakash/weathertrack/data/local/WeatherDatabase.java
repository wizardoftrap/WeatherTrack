package com.shivprakash.weathertrack.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.shivprakash.weathertrack.data.model.Weather;
import com.shivprakash.weathertrack.utils.Constants;
import com.shivprakash.weathertrack.utils.DateConverter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Weather.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class WeatherDatabase extends RoomDatabase {

    public abstract WeatherDao weatherDao();

    private static volatile WeatherDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static WeatherDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WeatherDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    WeatherDatabase.class, Constants.DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static ExecutorService getDatabaseWriteExecutor() {
        return databaseWriteExecutor;
    }
}