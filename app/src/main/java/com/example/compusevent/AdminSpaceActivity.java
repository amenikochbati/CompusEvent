package com.example.compusevent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

/** Espace dédié au rôle Administrateur (ADMIN) : création, gestion et suivi des événements. */
public class AdminSpaceActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            if (sessionManager.isLoggedIn()) {
                Toast.makeText(this, "Espace réservé aux administrateurs. Ouverture de l'espace étudiant.",
                        Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, UserSpaceActivity.class));
            } else {
                Toast.makeText(this, "Connecte-toi pour ouvrir l'espace administrateur.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_space);
        NavigationHelper.setup(this, "Espace Administrateur", "Votre rôle : Administrateur (ADMIN)");
        findViewById(R.id.app_bar).setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue));

        dbHelper = new DatabaseHelper(this);
        User user = sessionManager.getUser();

        ((TextView) findViewById(R.id.tv_admin_space_greeting)).setText("Bonjour, " + user.getName());
        ((TextView) findViewById(R.id.tv_total_events)).setText(
                "Événements : " + dbHelper.getAllEvents().size());
        ((TextView) findViewById(R.id.tv_upcoming_events)).setText(
                "À venir : " + dbHelper.getUpcomingEvents(Integer.MAX_VALUE).size());

        findViewById(R.id.btn_admin_space_create).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditEventActivity.class)));
        findViewById(R.id.btn_admin_space_manage).setOnClickListener(v ->
                startActivity(new Intent(this, EventsActivity.class)));
        findViewById(R.id.btn_admin_space_details).setOnClickListener(v -> openFirstEventDetails());
        findViewById(R.id.btn_admin_space_logout).setOnClickListener(v -> logout());
    }

    private void openFirstEventDetails() {
        List<Event> events = dbHelper.getUpcomingEvents(1);
        if (events.isEmpty()) {
            Toast.makeText(this, R.string.no_upcoming_events, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("EVENT_ID", events.get(0).getId());
        startActivity(intent);
    }

    private void logout() {
        sessionManager.logout();
        Toast.makeText(this, "Déconnexion réussie.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
