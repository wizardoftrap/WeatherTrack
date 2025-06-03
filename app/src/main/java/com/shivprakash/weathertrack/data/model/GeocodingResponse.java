package com.shivprakash.weathertrack.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeocodingResponse {

    @SerializedName("results")
    private List<GeocodingResult> results;

    public List<GeocodingResult> getResults() {
        return results;
    }

    public static class GeocodingResult {
        @SerializedName("name")
        private String name;

        @SerializedName("latitude")
        private double latitude;

        @SerializedName("longitude")
        private double longitude;

        @SerializedName("country")
        private String country;

        @SerializedName("admin1")
        private String admin1; // State/Province

        @SerializedName("admin2")
        private String admin2; // County/District

        @SerializedName("country_code")
        private String countryCode;

        @SerializedName("timezone")
        private String timezone;

        @SerializedName("population")
        private Integer population;

        @SerializedName("elevation")
        private Double elevation;

        // Getters
        public String getName() {
            return name;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getCountry() {
            return country;
        }

        public String getAdmin1() {
            return admin1;
        }

        public String getAdmin2() {
            return admin2;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public String getTimezone() {
            return timezone;
        }

        public Integer getPopulation() {
            return population;
        }

        public Double getElevation() {
            return elevation;
        }

        // Helper method to get full location name
        public String getFullName() {
            StringBuilder fullName = new StringBuilder(name);

            if (admin1 != null && !admin1.isEmpty() && !admin1.equals(name)) {
                fullName.append(", ").append(admin1);
            }

            if (country != null && !country.isEmpty()) {
                fullName.append(", ").append(country);
            }

            return fullName.toString();
        }

        // Alternative format for display
        public String getShortName() {
            if (admin1 != null && !admin1.isEmpty() && !admin1.equals(name)) {
                return name + ", " + admin1;
            }
            return name;
        }
    }
}