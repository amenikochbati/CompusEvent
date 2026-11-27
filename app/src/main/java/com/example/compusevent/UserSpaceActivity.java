package com.example.compusevent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/** Espace dédié au rôle Étudiant (USER) : favoris, inscriptions et accès aux événements. */
public class UserSpaceActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Connecte-toi pour ouvrir ton espace étudiant.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        if (sessionManager.isAdmin()) {
            Toast.makeText(this, "Espace réservé aux étudiants. Ouverture de l'espace administrateur.",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, AdminSpaceActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_user_space);
        NavigationHelper.setup(this, "Espace Étudiant", "Votre rôle : Étudiant (USER)");

        dbHelper = new DatabaseHelper(this);
        User user = sessionManager.getUser();

        ((TextView) findViewById(R.id.tv_user_space_greeting)).setText("Bonjour, " + user.getName());
        ((TextView) findViewById(R.id.tv_favorites_count)).setText(
                "Favoris : " + dbHelper.getFavoriteEvents().size());
        ((TextView) findViewById(R.id.tv_registrations_count)).setText(
                "Inscriptions actives : " + dbHelper.getParticipatedEvents().size());

        findViewById(R.id.btn_user_space_explore).setOnClickListener(v ->
                startActivity(new Intent(this, EventsActivity.class)));
        findViewById(R.id.btn_user_space_details).setOnClickListener(v -> openFirstEventDetails());
        findViewById(R.id.btn_user_space_logout).setOnClickListener(v -> logout());
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
