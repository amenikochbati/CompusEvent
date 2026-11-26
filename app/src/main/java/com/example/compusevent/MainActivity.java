package com.example.compusevent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

/** Accueil : navigation principale, session utilisateur/admin et événements à venir. */
public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private RecyclerView rvUpcomingEvents;
    private TextView tvEmptyUpcoming, tvUserGreeting, tvUserRoleBadge;
    private MaterialButton btnAuthAction, btnAdminCreateEvent;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        tvUserGreeting = findViewById(R.id.tv_user_greeting);
        tvUserRoleBadge = findViewById(R.id.tv_user_role_badge);
        btnAuthAction = findViewById(R.id.btn_auth_action);
        btnAdminCreateEvent = findViewById(R.id.btn_admin_create_event);

        View btnExplore = findViewById(R.id.btn_explore);
        View btnMyEvents = findViewById(R.id.btn_my_events);
        View btnAbout = findViewById(R.id.btn_about);
        View btnSeeAll = findViewById(R.id.btn_see_all);
        rvUpcomingEvents = findViewById(R.id.rv_upcoming_events);
        tvEmptyUpcoming = findViewById(R.id.tv_empty_upcoming);
        NavigationHelper.forwardScrollFromHeader(this);

        rvUpcomingEvents.setLayoutManager(new LinearLayoutManager(this));

        btnExplore.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EventsActivity.class)));
        btnMyEvents.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MyEventsActivity.class)));
        btnAbout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AboutActivity.class)));
        btnSeeAll.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EventsActivity.class)));

        btnAdminCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditEventActivity.class);
            startActivity(intent);
        });

        btnAuthAction.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                sessionManager.logout();
                Toast.makeText(this, "Déconnexion réussie.", Toast.LENGTH_SHORT).show();
                updateUserHeader();
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserHeader();
        loadUpcomingEvents();
    }

    private void updateUserHeader() {
        if (sessionManager.isLoggedIn()) {
            User user = sessionManager.getUser();
            tvUserGreeting.setText("Bonjour, " + user.getName());
            if (user.isAdmin()) {
                tvUserRoleBadge.setText("Mode Administrateur 👑");
                btnAdminCreateEvent.setVisibility(View.VISIBLE);
            } else {
                tvUserRoleBadge.setText("Mode Étudiant (User)");
                btnAdminCreateEvent.setVisibility(View.GONE);
            }
            btnAuthAction.setText("Déconnexion");
        } else {
            tvUserGreeting.setText("Bonjour, Invité");
            tvUserRoleBadge.setText("Mode Invité");
            btnAdminCreateEvent.setVisibility(View.GONE);
            btnAuthAction.setText("Se connecter");
        }
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
