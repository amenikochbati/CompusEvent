package com.example.compusevent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

/** Accueil : navigation principale et événements à venir. */
public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private DatabaseHelper dbHelper;
    private RecyclerView rvUpcomingEvents;
    private TextView tvEmptyUpcoming;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        MaterialButton btnExplore = findViewById(R.id.btn_explore);
        MaterialButton btnMyEvents = findViewById(R.id.btn_my_events);
        MaterialButton btnAbout = findViewById(R.id.btn_about);
        rvUpcomingEvents = findViewById(R.id.rv_upcoming_events);
        tvEmptyUpcoming = findViewById(R.id.tv_empty_upcoming);

        rvUpcomingEvents.setLayoutManager(new LinearLayoutManager(this));

        btnExplore.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EventsActivity.class)));
        btnMyEvents.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MyEventsActivity.class)));
        btnAbout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AboutActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUpcomingEvents();
    }

    private void loadUpcomingEvents() {
        List<Event> upcomingEvents = dbHelper.getUpcomingEvents(5);
        boolean isEmpty = upcomingEvents.isEmpty();
        tvEmptyUpcoming.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvUpcomingEvents.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (adapter == null) {
            adapter = new EventAdapter(this, upcomingEvents, this);
            rvUpcomingEvents.setAdapter(adapter);
        } else {
            adapter.updateEvents(upcomingEvents);
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
        // La base est mise à jour directement par l'adaptateur.
    }
}
