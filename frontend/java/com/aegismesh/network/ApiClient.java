package com.aegismesh.network;

import android.util.Log;

import com.aegismesh.models.Emergency;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Handles HTTP network communication with the Aegis Mesh FastAPI backend.
 * All endpoint paths are derived dynamically from a base URL configured in BuildConfig.BASE_URL.
 */
public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final int TIMEOUT_MS = 10000; // 10 seconds timeout

    // Derive BASE_URL from BuildConfig.BASE_URL with safety checks.
    public static final String BASE_URL = getBaseUrl();

    // Derived Endpoints
    public static final String ENDPOINT_EMERGENCY = combinePath(BASE_URL, "emergency");
    public static final String ENDPOINT_LOGIN = combinePath(BASE_URL, "login");
    public static final String ENDPOINT_REGISTER = combinePath(BASE_URL, "register");
    public static final String ENDPOINT_HOSPITALS = combinePath(BASE_URL, "hospitals");
    public static final String ENDPOINT_TRIAGE = combinePath(BASE_URL, "triage");

    /**
     * Resolves the BASE_URL dynamically via reflection from BuildConfig.
     * Fallback to default emulator IP (10.0.2.2) if not defined or ifBuildConfig is missing at compile time.
     */
    private static String getBaseUrl() {
        try {
            Class<?> clazz = Class.forName("com.aegismesh.BuildConfig");
            String url = (String) clazz.getField("BASE_URL").get(null);
            if (url != null && !url.isEmpty()) {
                return url;
            }
        } catch (Exception e) {
            Log.w(TAG, "BuildConfig.BASE_URL not found, using development fallback.");
        }
        return "http://10.0.2.2:8000/";
    }

    /**
     * Safely combines base URL and endpoint path, resolving slash formatting issues.
     */
    private static String combinePath(String base, String path) {
        if (base.endsWith("/")) {
            return base + path;
        } else {
            return base + "/" + path;
        }
    }

    /**
     * Synchronously sends emergency data to the FastAPI backend.
     * MUST be run on a background thread.
     *
     * @param emergency The emergency model instance.
     * @throws Exception If connection fails, request times out, or server returns non-2xx code.
     */
    public static void sendEmergency(Emergency emergency) throws Exception {
        if (emergency == null) {
            throw new IllegalArgumentException("Emergency object cannot be null");
        }

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(ENDPOINT_EMERGENCY);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setConnectTimeout(TIMEOUT_MS);
            urlConnection.setReadTimeout(TIMEOUT_MS);
            urlConnection.setDoOutput(true);

            // Generate JSON payload matching backend structure
            String jsonPayload = emergency.toBackendJsonString();
            Log.d(TAG, "Sending JSON to " + ENDPOINT_EMERGENCY + ": " + jsonPayload);

            // Write payload
            try (OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = urlConnection.getResponseCode();
            Log.d(TAG, "Response Code: " + responseCode);

            if (responseCode >= 200 && responseCode < 300) {
                // Read response
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
                Log.d(TAG, "Emergency reported successfully: " + response.toString());
            } else {
                // Read error stream
                StringBuilder errorResponse = new StringBuilder();
                if (urlConnection.getErrorStream() != null) {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(urlConnection.getErrorStream(), "utf-8"))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            errorResponse.append(responseLine.trim());
                        }
                    }
                }
                throw new Exception("HTTP error code: " + responseCode + " - " + errorResponse.toString());
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
