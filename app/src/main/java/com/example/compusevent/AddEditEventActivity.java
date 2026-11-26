package com.example.compusevent;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AddEditEventActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private TextView tvHeader;
    private EditText etTitle, etDesc, etDate, etTime, etLocation, etLat, etLon, etCapacity;
    private Spinner spCategory;
    private MaterialButton btnSave, btnDelete;

    private int eventId = -1;
    private Event existingEvent = null;

    private final String[] categories = {"Technologie", "Culture", "Sport", "Solidarité", "Orientation", "Environnement"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "Accès réservé aux administrateurs.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_add_edit_event);
        NavigationHelper.setup(this, "Administration", "Créer ou modifier un événement");

        dbHelper = new DatabaseHelper(this);

        tvHeader = findViewById(R.id.tv_add_edit_header);
        etTitle = findViewById(R.id.et_event_title);
        etDesc = findViewById(R.id.et_event_desc);
        etDate = findViewById(R.id.et_event_date);
        etTime = findViewById(R.id.et_event_time);
        etLocation = findViewById(R.id.et_event_location);
        etLat = findViewById(R.id.et_event_lat);
        etLon = findViewById(R.id.et_event_lon);
        etCapacity = findViewById(R.id.et_event_capacity);
        spCategory = findViewById(R.id.sp_event_category);
        btnSave = findViewById(R.id.btn_save_event);
        btnDelete = findViewById(R.id.btn_delete_event);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(spinnerAdapter);

        eventId = getIntent().getIntExtra("EVENT_ID", -1);
        if (eventId != -1) {
            existingEvent = dbHelper.getEventById(eventId);
            if (existingEvent != null) {
                populateFields(existingEvent);
            }
        }

        btnSave.setOnClickListener(v -> saveEvent());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void populateFields(Event event) {
        tvHeader.setText("Modifier l'Événement");
        etTitle.setText(event.getTitle());
        etDesc.setText(event.getDescription());
        etDate.setText(event.getDate());
        etTime.setText(event.getTime());
        etLocation.setText(event.getLocation());
        etLat.setText(String.valueOf(event.getLatitude()));
        etLon.setText(String.valueOf(event.getLongitude()));
        etCapacity.setText(String.valueOf(event.getCapacity()));

        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(event.getCategory())) {
                spCategory.setSelection(i);
                break;
            }
        }

        btnDelete.setVisibility(View.VISIBLE);
    }

    private void saveEvent() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = spCategory.getSelectedItem() != null ? spCategory.getSelectedItem().toString() : categories[0];
        String latStr = etLat.getText().toString().trim();
        String lonStr = etLon.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Titre obligatoire");
            etTitle.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            etDate.setError("Date obligatoire (YYYY-MM-DD)");
            etDate.requestFocus();
            return;
        }

        if (time.isEmpty()) {
            etTime.setError("Heure obligatoire (HH:MM)");
            etTime.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            etLocation.setError("Lieu obligatoire");
            etLocation.requestFocus();
            return;
        }

        double lat = 36.8065;
        double lon = 10.1815;
        int capacity = 100;

        try {
            if (!latStr.isEmpty()) lat = Double.parseDouble(latStr);
            if (!lonStr.isEmpty()) lon = Double.parseDouble(lonStr);
            if (!capacityStr.isEmpty()) capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Format de nombre invalide.", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event(
                eventId != -1 ? eventId : 0,
                title, desc, date, time, location, category, lat, lon, capacity
        );

        if (eventId != -1) {
            boolean updated = dbHelper.updateEvent(event);
            if (updated) {
                Toast.makeText(this, "Événement mis à jour !", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erreur lors de la mise à jour.", Toast.LENGTH_SHORT).show();
            }
        } else {
            long newId = dbHelper.addEvent(event);
            if (newId != -1) {
                Toast.makeText(this, "Événement créé avec succès !", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erreur lors de la création.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Suppression")
                .setMessage("Voulez-vous vraiment supprimer cet événement ?")
                .setPositiveButton("Oui, supprimer", (dialog, which) -> {
                    boolean deleted = dbHelper.deleteEvent(eventId);
                    if (deleted) {
                        Toast.makeText(this, "Événement supprimé.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Échec de la suppression.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}
