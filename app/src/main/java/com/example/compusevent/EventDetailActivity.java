package com.example.compusevent;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/** Fiche événement : participation contrôlée et météo Open-Meteo non bloquante. */
public class EventDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Event event;
    private int eventId = -1;

    private TextView tvCategory, tvTitle, tvDateTime, tvLocation, tvCapacity, tvDescription;
    private TextView tvStatus, tvPlaces;
    private MaterialButton btnFavorite, btnParticipate, btnRefreshWeather;

    private MaterialCardView weatherCard;
    private ProgressBar pbWeather;
    private TextView tvWeatherError;
    private LinearLayout layoutWeatherContent;
    private TextView tvWeatherTemp, tvWeatherDesc, tvWeatherTempMinMax, tvWeatherDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        dbHelper = new DatabaseHelper(this);
        eventId = getIntent().getIntExtra("EVENT_ID", -1);
        event = dbHelper.getEventById(eventId);
        if (event == null) {
            Toast.makeText(this, R.string.no_events_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        displayEventDetails();
        setupActionButtons();
        prepareWeather();
    }

    private void initViews() {
        tvCategory = findViewById(R.id.tv_detail_category);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvDateTime = findViewById(R.id.tv_detail_date_time);
        tvLocation = findViewById(R.id.tv_detail_location);
        tvCapacity = findViewById(R.id.tv_detail_capacity);
        tvDescription = findViewById(R.id.tv_detail_description);
        tvStatus = findViewById(R.id.tv_detail_status);
        tvPlaces = findViewById(R.id.tv_detail_places);

        btnFavorite = findViewById(R.id.btn_detail_favorite);
        btnParticipate = findViewById(R.id.btn_detail_participate);
        btnRefreshWeather = findViewById(R.id.btn_refresh_weather);

        weatherCard = findViewById(R.id.weather_card);
        pbWeather = findViewById(R.id.pb_weather);
        tvWeatherError = findViewById(R.id.tv_weather_error);
        layoutWeatherContent = findViewById(R.id.layout_weather_content);
        tvWeatherTemp = findViewById(R.id.tv_weather_temp);
        tvWeatherDesc = findViewById(R.id.tv_weather_desc);
        tvWeatherTempMinMax = findViewById(R.id.tv_weather_temp_minmax);
        tvWeatherDetails = findViewById(R.id.tv_weather_details);
    }

    private void displayEventDetails() {
        int active = dbHelper.getActiveRegistrationCount(event.getId());
        int available = EventUtils.getAvailablePlaces(event, active);
        String status = EventUtils.getStatusLabel(event, active);

        tvCategory.setText(event.getCategory());
        tvTitle.setText(event.getTitle());
        tvDateTime.setText("📅 " + EventUtils.formatFrenchDate(event.getDate()) + " à " + event.getTime());
        tvLocation.setText("📍 " + event.getLocation());
        tvCapacity.setText("👥 " + getString(R.string.label_available_places) + " : " + available
                + " / " + event.getCapacity());
        tvDescription.setText(event.getDescription());

        tvStatus.setText(status);
        int statusColor;
        if ("Terminé".equals(status)) {
            statusColor = R.color.text_muted;
        } else if ("Complet".equals(status)) {
            statusColor = R.color.error_red;
        } else if ("Places limitées".equals(status)) {
            statusColor = R.color.warning_orange;
        } else {
            statusColor = R.color.weather_turquoise;
        }
        tvStatus.setBackgroundColor(ContextCompat.getColor(this, statusColor));
        tvPlaces.setText(getString(R.string.label_available_places) + " : " + available);

        updateFavoriteButtonState();
        updateParticipateButtonState(available);
    }

    private void updateFavoriteButtonState() {
        btnFavorite.setText(event.isFavorite() ? "Retirer des favoris ⭐" : "Ajouter aux favoris ☆");
    }

    private void updateParticipateButtonState(int availablePlaces) {
        if (event.isParticipating()) {
            btnParticipate.setText(R.string.btn_cancel_participation);
            btnParticipate.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.error_red));
            btnParticipate.setEnabled(true);
        } else if (EventUtils.isPast(event)) {
            btnParticipate.setText(R.string.event_finished);
            btnParticipate.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.text_muted));
            btnParticipate.setEnabled(false);
        } else if (availablePlaces <= 0) {
            btnParticipate.setText(R.string.event_full);
            btnParticipate.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.text_muted));
            btnParticipate.setEnabled(false);
        } else {
            btnParticipate.setText(R.string.btn_participate);
            btnParticipate.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.action_blue));
            btnParticipate.setEnabled(true);
        }
    }

    private void setupActionButtons() {
        btnFavorite.setOnClickListener(v -> {
            boolean newFavorite = !event.isFavorite();
            event.setFavorite(newFavorite);
            if (newFavorite) {
                dbHelper.addFavorite(event.getId());
                Toast.makeText(this, "Ajouté aux favoris", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.removeFavorite(event.getId());
                Toast.makeText(this, "Retiré des favoris", Toast.LENGTH_SHORT).show();
            }
            updateFavoriteButtonState();
        });

        btnParticipate.setOnClickListener(v -> {
            if (event.isParticipating()) {
                boolean cancelled = dbHelper.cancelRegistration(event.getId());
                Toast.makeText(this, cancelled ? R.string.cancel_success : R.string.register_error,
                        Toast.LENGTH_SHORT).show();
            } else {
                int result = dbHelper.registerForEvent(event.getId());
                switch (result) {
                    case DatabaseHelper.REGISTER_SUCCESS:
                        Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                        break;
                    case DatabaseHelper.REGISTER_ALREADY_REGISTERED:
                        Toast.makeText(this, R.string.register_already, Toast.LENGTH_SHORT).show();
                        break;
                    case DatabaseHelper.REGISTER_FULL:
                        Toast.makeText(this, R.string.register_full, Toast.LENGTH_SHORT).show();
                        break;
                    case DatabaseHelper.REGISTER_PAST_EVENT:
                        Toast.makeText(this, R.string.register_past, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(this, R.string.register_error, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            reloadEvent();
            displayEventDetails();
            prepareWeather();
        });

        btnRefreshWeather.setOnClickListener(v -> loadWeatherInfo(true));
    }

    private void reloadEvent() {
        event = dbHelper.getEventById(eventId);
    }

    /** Un événement passé n'affiche pas la météo ; au-delà de 16 jours, message explicite. */
    private void prepareWeather() {
        if (EventUtils.isPast(event) || (event.getLatitude() == 0 && event.getLongitude() == 0)) {
            weatherCard.setVisibility(View.GONE);
            return;
        }
        weatherCard.setVisibility(View.VISIBLE);

        int days = EventUtils.daysUntil(event);
        if (days < 0 || days > EventUtils.MAX_FORECAST_DAYS) {
            pbWeather.setVisibility(View.GONE);
            layoutWeatherContent.setVisibility(View.GONE);
            btnRefreshWeather.setVisibility(View.GONE);
            tvWeatherError.setText(R.string.weather_not_available);
            tvWeatherError.setVisibility(View.VISIBLE);
            return;
        }

        btnRefreshWeather.setVisibility(View.VISIBLE);
        loadWeatherInfo(false);
    }

    private void loadWeatherInfo(boolean forceRefresh) {
        pbWeather.setVisibility(View.VISIBLE);
        tvWeatherError.setVisibility(View.GONE);
        layoutWeatherContent.setVisibility(View.GONE);

        WeatherService.fetchWeather(event.getLatitude(), event.getLongitude(),
                new WeatherService.WeatherCallback() {
                    @Override
                    public void onSuccess(WeatherData weatherData) {
                        pbWeather.setVisibility(View.GONE);
                        layoutWeatherContent.setVisibility(View.VISIBLE);
                        tvWeatherTemp.setText(String.format(Locale.getDefault(), "%.1f °C",
                                weatherData.getTemperature()));
                        tvWeatherDesc.setText(weatherData.getWeatherDescription());
                        tvWeatherTempMinMax.setText(String.format(Locale.getDefault(),
                                "Min : %.1f °C | Max : %.1f °C",
                                weatherData.getTempMin(), weatherData.getTempMax()));
                        tvWeatherDetails.setText(String.format(Locale.getDefault(),
                                "Vent : %.1f km/h | Humidité : %d%% | Prob. pluie : %d%%",
                                weatherData.getWindSpeed(), weatherData.getHumidity(),
                                weatherData.getRainProbability()));
                    }

                    @Override
                    public void onError(String errorMessage) {
                        pbWeather.setVisibility(View.GONE);
                        tvWeatherError.setText(errorMessage);
                        tvWeatherError.setVisibility(View.VISIBLE);
                    }
                }, forceRefresh);
    }
}
