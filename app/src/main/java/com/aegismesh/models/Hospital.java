package com.aegismesh.models;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

<<<<<<< HEAD
public class Hospital implements Serializable {
=======
/**
 * Represents a medical facility capable of receiving emergency patients.
 */
public class Hospital implements Serializable {
<<<<<<< HEAD
=======
>>>>>>> origin/main
    private static final long serialVersionUID = 1L;

    public String name;
    public String inventory;
    public String distance;
    public double latitude;
    public double longitude;
    
    public String routingReason = "Specialized trauma care unit";
    public double distanceKm = 0.0;
<<<<<<< HEAD

    public Hospital(String name, String inventory, String distance, double latitude, double longitude) {
        this.name = name;
        this.inventory = inventory;
        this.distance = distance;
=======
>>>>>>> origin/main

    private static final long serialVersionUID = 1L;

    public String name;
    public String routingReason;
    public double distanceKm;
    public String inventory;
    public double latitude;
    public double longitude;

    public Hospital() {
    }

    public Hospital(String name, String inventory, double distanceKm, double latitude, double longitude) {
        this.name = name;
        this.inventory = inventory;
        this.distanceKm = distanceKm;
>>>>>>> origin/main
        this.latitude = latitude;
        this.longitude = longitude;
        
        try {
            String clean = distance.replaceAll("[^0-9.]", "");
            this.distanceKm = Double.parseDouble(clean);
        } catch (Exception e) {
            this.distanceKm = 0.0;
        }
    }

    public String getName() { return name; }
<<<<<<< HEAD
    public String getDistance() { return distance; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
=======
    public String getDistance() { return String.format("%.1f km", distanceKm); }
>>>>>>> origin/main

    /**
     * Helper to parse the JSON response returned by our FastAPI web scraper.
     */
    public static Hospital fromBackendJson(JSONObject json) throws JSONException {
        JSONObject coords = json.getJSONObject("coordinates");
<<<<<<< HEAD
        return new Hospital(
                json.getString("name"),
                json.getString("inventory"),
                json.getString("distance"),
                coords.getDouble("lat"),
                coords.getDouble("lon")
        );
    }
}
=======
        Hospital h = new Hospital(
                json.getString("name"),
                json.getString("inventory"),
                json.optDouble("distance_km", 0.0),
                coords.getDouble("lat"),
                coords.getDouble("lon")
        );
        h.routingReason = json.optString("routing_reason", "Nearest facility with available inventory");
        return h;
    }
}
>>>>>>> origin/main
