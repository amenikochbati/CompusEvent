package com.example.compusevent;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client de l'API externe Open-Meteo.
 * Les réponses sont mises en cache 45 minutes : l'écran événement reste utilisable
 * même si le service externe est momentanément indisponible.
 */
public class WeatherService {

    public interface WeatherCallback {
        void onSuccess(WeatherData weatherData);

        void onError(String errorMessage);
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final long CACHE_TTL_MS = 45 * 60 * 1000L;
    private static final Map<String, CachedWeather> cache = new HashMap<>();

    private static class CachedWeather {
        final WeatherData data;
        final long timestamp;

        CachedWeather(WeatherData data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isFresh() {
            return System.currentTimeMillis() - timestamp < CACHE_TTL_MS;
        }
    }

    public static void fetchWeather(double latitude, double longitude, WeatherCallback callback) {
        fetchWeather(latitude, longitude, callback, false);
    }

    public static void fetchWeather(double latitude, double longitude,
                                    WeatherCallback callback, boolean forceRefresh) {
        final String key = String.format(Locale.US, "%.3f;%.3f", latitude, longitude);

        if (!forceRefresh) {
            synchronized (cache) {
                CachedWeather cached = cache.get(key);
                if (cached != null && cached.isFresh()) {
                    deliverSuccess(callback, cached.data);
                    return;
                }
            }
        }

        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String urlString = String.format(Locale.US,
                        "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f" +
                                "&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m" +
                                "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
                                "&forecast_days=%d",
                        latitude, longitude, EventUtils.MAX_FORECAST_DAYS)
                        + "&timezone=Africa%2FTunis";

                connection = (HttpURLConnection) new URL(urlString).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    WeatherData data = parseJsonResponse(new JSONObject(response.toString()));
                    synchronized (cache) {
                        cache.put(key, new CachedWeather(data));
                    }
                    deliverSuccess(callback, data);
                } else if (responseCode == 429 || responseCode == 503) {
                    deliverError(callback, "Service météo momentanément indisponible (quota). Réessayez plus tard.");
                } else {
                    deliverError(callback, "Erreur météo (code " + responseCode + ").");
                }
            } catch (Exception e) {
                Log.e("WeatherService", "Échec appel Open-Meteo", e);
                // Cache expiré mais présent : on l'utilise plutôt que de bloquer l'écran.
                synchronized (cache) {
                    CachedWeather cached = cache.get(key);
                    if (cached != null) {
                        deliverSuccess(callback, cached.data);
                        return;
                    }
                }
                deliverError(callback, "Prévisions météo indisponibles. Vérifiez votre connexion Internet.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private static void deliverSuccess(WeatherCallback callback, WeatherData data) {
        mainHandler.post(() -> callback.onSuccess(data));
    }

    private static void deliverError(WeatherCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
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
                    if (probArr.length() > 0 && !probArr.isNull(0)) {
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
