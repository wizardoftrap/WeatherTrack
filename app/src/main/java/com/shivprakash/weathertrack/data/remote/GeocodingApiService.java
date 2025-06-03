package com.shivprakash.weathertrack.data.remote;

import com.shivprakash.weathertrack.data.model.GeocodingResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingApiService {

    /**
     * Search for locations by name using Open-Meteo Geocoding API
     *
     * @param name The city/location name to search for
     * @param count Maximum number of results to return (default: 10, max: 100)
     * @param language Language for results (e.g., "en" for English)
     * @param format Response format (should be "json")
     * @return Call object with GeocodingResponse
     */
    @GET("v1/search")
    Call<GeocodingResponse> searchCity(
            @Query("name") String name,
            @Query("count") int count,
            @Query("language") String language,
            @Query("format") String format
    );

    /**
     * Alternative method with fewer parameters
     * Uses default values for count (10) and language (en)
     */
    @GET("v1/search")
    Call<GeocodingResponse> searchCity(
            @Query("name") String name
    );
}