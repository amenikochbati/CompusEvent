package com.example.compusevent;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherService {

    public interface WeatherCallback {
        void onSuccess(WeatherData weatherData);
        void onError(String errorMessage);
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void fetchWeather(double latitude, double longitude, WeatherCallback callback) {
        executor.execute(() -> {
            try {
                String urlString = String.format(Locale.US,
                        "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=Africa/Tunis",
                        latitude, longitude);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    WeatherData weatherData = parseJsonResponse(jsonObject);

                    mainHandler.post(() -> callback.onSuccess(weatherData));
                } else {
                    mainHandler.post(() -> callback.onError("Erreur serveur : code " + responseCode));
                }
                conn.disconnect();
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Échec de la connexion. Vérifiez votre accès Internet."));
            }
        });
    }

    private static WeatherData parseJsonResponse(JSONObject json) {
        WeatherData data = new WeatherData();
        try {
            if (json.has("current")) {
                JSONObject current = json.getJSONObject("current");
                if (current.has("temperature_2m")) {
                    data.setTemperature(current.getDouble("temperature_2m"));
                }
                if (current.has("relative_humidity_2m")) {
                    data.setHumidity(current.getInt("relative_humidity_2m"));
                }
                if (current.has("precipitation")) {
                    data.setPrecipitation(current.getDouble("precipitation"));
                }
                if (current.has("wind_speed_10m")) {
                    data.setWindSpeed(current.getDouble("wind_speed_10m"));
                }
                if (current.has("weather_code")) {
                    data.setWeatherCode(current.getInt("weather_code"));
                }
            }

            if (json.has("daily")) {
                JSONObject daily = json.getJSONObject("daily");
                if (daily.has("temperature_2m_max")) {
                    JSONArray maxArr = daily.getJSONArray("temperature_2m_max");
                    if (maxArr.length() > 0) {
                        data.setTempMax(maxArr.getDouble(0));
                    }
                }
                if (daily.has("temperature_2m_min")) {
                    JSONArray minArr = daily.getJSONArray("temperature_2m_min");
                    if (minArr.length() > 0) {
                        data.setTempMin(minArr.getDouble(0));
                    }
                }
                if (daily.has("precipitation_probability_max")) {
                    JSONArray probArr = daily.getJSONArray("precipitation_probability_max");
                    if (probArr.length() > 0) {
                        data.setRainProbability(probArr.getInt(0));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
