package com.example.compusevent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MyEventsActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private DatabaseHelper dbHelper;
    private RecyclerView rvMyEvents;
    private TextView tvEmptyMyEvents;
    private MaterialButton btnTabFavorites, btnTabParticipations;
    private EventAdapter adapter;

    private boolean isFavoritesTab = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        dbHelper = new DatabaseHelper(this);

        rvMyEvents = findViewById(R.id.rv_my_events);
        tvEmptyMyEvents = findViewById(R.id.tv_empty_my_events);
        btnTabFavorites = findViewById(R.id.btn_tab_favorites);
        btnTabParticipations = findViewById(R.id.btn_tab_participations);

        rvMyEvents.setLayoutManager(new LinearLayoutManager(this));

        btnTabFavorites.setOnClickListener(v -> {
            isFavoritesTab = true;
            updateTabStyles();
            loadEvents();
        });

        btnTabParticipations.setOnClickListener(v -> {
            isFavoritesTab = false;
            updateTabStyles();
            loadEvents();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void updateTabStyles() {
        if (isFavoritesTab) {
            btnTabFavorites.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.action_blue));
            btnTabFavorites.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnTabFavorites.setStrokeWidth(0);

            btnTabParticipations.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.transparent));
            btnTabParticipations.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnTabParticipations.setStrokeColorResource(R.color.white);
            btnTabParticipations.setStrokeWidth(2);
        } else {
            btnTabParticipations.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.action_blue));
            btnTabParticipations.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnTabParticipations.setStrokeWidth(0);

            btnTabFavorites.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.transparent));
            btnTabFavorites.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnTabFavorites.setStrokeColorResource(R.color.white);
            btnTabFavorites.setStrokeWidth(2);
        }
    }

    private void loadEvents() {
        List<Event> events;
        if (isFavoritesTab) {
            events = dbHelper.getFavoriteEvents();
            tvEmptyMyEvents.setText(R.string.no_favorites);
        } else {
            // Historique complet : inscriptions confirmées ET annulées (T07).
            events = dbHelper.getRegistrationHistory();
            tvEmptyMyEvents.setText(R.string.no_participations);
        }

        if (events.isEmpty()) {
            tvEmptyMyEvents.setVisibility(View.VISIBLE);
            rvMyEvents.setVisibility(View.GONE);
        } else {
            tvEmptyMyEvents.setVisibility(View.GONE);
            rvMyEvents.setVisibility(View.VISIBLE);

            if (adapter == null) {
                adapter = new EventAdapter(this, events, this);
                rvMyEvents.setAdapter(adapter);
            } else {
                adapter.updateEvents(events);
            }
        }
    }

    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("EVENT_ID", event.getId());
        startActivity(intent);
    }

    @Override
    public void onFavoriteToggle(Event event, boolean newFavoriteStatus) {
        loadEvents();
    }
}
