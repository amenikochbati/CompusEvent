package com.example.compusevent;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Event event;

    private TextView tvCategory, tvTitle, tvDateTime, tvLocation, tvCapacity, tvDescription;
    private MaterialButton btnFavorite, btnParticipate, btnRefreshWeather;

    // Weather UI
    private ProgressBar pbWeather;
    private TextView tvWeatherError;
    private LinearLayout layoutWeatherContent;
    private TextView tvWeatherTemp, tvWeatherDesc, tvWeatherTempMinMax, tvWeatherDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        dbHelper = new DatabaseHelper(this);

        int eventId = getIntent().getIntExtra("EVENT_ID", -1);
        if (eventId == -1) {
            Toast.makeText(this, "Événement introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        event = dbHelper.getEventById(eventId);
        if (event == null) {
            Toast.makeText(this, "Événement introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        displayEventDetails();
        setupActionButtons();
        loadWeatherInfo();
    }

    private void initViews() {
        tvCategory = findViewById(R.id.tv_detail_category);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvDateTime = findViewById(R.id.tv_detail_date_time);
        tvLocation = findViewById(R.id.tv_detail_location);
        tvCapacity = findViewById(R.id.tv_detail_capacity);
        tvDescription = findViewById(R.id.tv_detail_description);

        btnFavorite = findViewById(R.id.btn_detail_favorite);
        btnParticipate = findViewById(R.id.btn_detail_participate);
        btnRefreshWeather = findViewById(R.id.btn_refresh_weather);

        pbWeather = findViewById(R.id.pb_weather);
        tvWeatherError = findViewById(R.id.tv_weather_error);
        layoutWeatherContent = findViewById(R.id.layout_weather_content);

        tvWeatherTemp = findViewById(R.id.tv_weather_temp);
        tvWeatherDesc = findViewById(R.id.tv_weather_desc);
        tvWeatherTempMinMax = findViewById(R.id.tv_weather_temp_minmax);
        tvWeatherDetails = findViewById(R.id.tv_weather_details);
    }

    private void displayEventDetails() {
        tvCategory.setText(event.getCategory());
        tvTitle.setText(event.getTitle());
        tvDateTime.setText("📅 " + event.getDate() + " à " + event.getTime());
        tvLocation.setText("📍 " + event.getLocation());
        tvCapacity.setText("👥 Capacité : " + event.getCapacity() + " places");
        tvDescription.setText(event.getDescription());

        updateFavoriteButtonState();
        updateParticipateButtonState();
    }

    private void updateFavoriteButtonState() {
        if (event.isFavorite()) {
            btnFavorite.setText("Retirer des favoris ⭐");
        } else {
            btnFavorite.setText("Ajouter aux favoris ☆");
        }
    }

    private void updateParticipateButtonState() {
        if (event.isParticipating()) {
            btnParticipate.setText("Annuler ma participation");
            btnParticipate.setBackgroundTintList(getColorStateList(R.color.error_red));
        } else {
            btnParticipate.setText("Participer");
            btnParticipate.setBackgroundTintList(getColorStateList(R.color.action_blue));
        }
    }

    private void setupActionButtons() {
        btnFavorite.setOnClickListener(v -> {
            boolean newFav = !event.isFavorite();
            event.setFavorite(newFav);
            if (newFav) {
                dbHelper.addFavorite(event.getId());
                Toast.makeText(this, "Ajouté aux favoris", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.removeFavorite(event.getId());
                Toast.makeText(this, "Retiré des favoris", Toast.LENGTH_SHORT).show();
            }
            updateFavoriteButtonState();
        });

        btnParticipate.setOnClickListener(v -> {
            boolean newPart = !event.isParticipating();
            event.setParticipating(newPart);
            if (newPart) {
                dbHelper.addParticipation(event.getId());
                Toast.makeText(this, "Participation enregistrée !", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.removeParticipation(event.getId());
                Toast.makeText(this, "Participation annulée", Toast.LENGTH_SHORT).show();
            }
            updateParticipateButtonState();
        });

        btnRefreshWeather.setOnClickListener(v -> loadWeatherInfo());
    }

    private void loadWeatherInfo() {
        pbWeather.setVisibility(View.VISIBLE);
        tvWeatherError.setVisibility(View.GONE);
        layoutWeatherContent.setVisibility(View.GONE);

        WeatherService.fetchWeather(event.getLatitude(), event.getLongitude(), new WeatherService.WeatherCallback() {
            @Override
            public void onSuccess(WeatherData weatherData) {
                pbWeather.setVisibility(View.GONE);
                layoutWeatherContent.setVisibility(View.VISIBLE);

                tvWeatherTemp.setText(String.format(Locale.getDefault(), "%.1f °C", weatherData.getTemperature()));
                tvWeatherDesc.setText(weatherData.getWeatherDescription());
                tvWeatherTempMinMax.setText(String.format(Locale.getDefault(), "Min : %.1f °C | Max : %.1f °C",
                        weatherData.getTempMin(), weatherData.getTempMax()));
                tvWeatherDetails.setText(String.format(Locale.getDefault(), "Vent : %.1f km/h | Humidité : %d%% | Prob. pluie : %d%%",
                        weatherData.getWindSpeed(), weatherData.getHumidity(), weatherData.getRainProbability()));
            }

            @Override
            public void onError(String errorMessage) {
                pbWeather.setVisibility(View.GONE);
                tvWeatherError.setText(errorMessage);
                tvWeatherError.setVisibility(View.VISIBLE);
            }
        });
    }
}
