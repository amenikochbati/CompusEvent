package com.example.compusevent;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private DatabaseHelper dbHelper;
    private RecyclerView rvUpcomingEvents;
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

        rvUpcomingEvents.setLayoutManager(new LinearLayoutManager(this));

        btnExplore.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EventsActivity.class);
            startActivity(intent);
        });

        btnMyEvents.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyEventsActivity.class);
            startActivity(intent);
        });

        btnAbout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUpcomingEvents();
    }

    private void loadUpcomingEvents() {
        List<Event> upcomingEvents = dbHelper.getUpcomingEvents(5);
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
        // Handled internally by DatabaseHelper in EventAdapter
    }
}
