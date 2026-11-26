package com.example.compusevent;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/** Liste des événements avec recherche multicritère, filtres et ajout Admin. */
public class EventsActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private RecyclerView rvEvents;
    private TextView tvEmptyState;
    private EditText etSearch;
    private ChipGroup chipGroupCategories;
    private MaterialCheckBox cbShowPast;
    private ImageButton btnBack, btnClearSearch;
    private MaterialButton btnFilter;
    private FloatingActionButton fabAddEvent;
    private EventAdapter adapter;

    private String selectedCategory = "Toutes";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        rvEvents = findViewById(R.id.rv_events);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        etSearch = findViewById(R.id.et_search);
        chipGroupCategories = findViewById(R.id.chip_group_categories);
        cbShowPast = findViewById(R.id.cb_show_past);
        btnBack = findViewById(R.id.btn_back_events);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        btnFilter = findViewById(R.id.btn_filter);
        fabAddEvent = findViewById(R.id.fab_add_event);

        rvEvents.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());
        btnClearSearch.setOnClickListener(v -> etSearch.setText(""));
        btnFilter.setOnClickListener(v -> {
            cbShowPast.setChecked(!cbShowPast.isChecked());
            updateFilterButton();
        });

        if (sessionManager.isAdmin()) {
            fabAddEvent.setVisibility(View.VISIBLE);
            fabAddEvent.setOnClickListener(v -> {
                Intent intent = new Intent(EventsActivity.this, AddEditEventActivity.class);
                startActivity(intent);
            });
        } else {
            fabAddEvent.setVisibility(View.GONE);
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                filterEvents();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedCategory = "Toutes";
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedCategory = chip.getText().toString();
                }
            }
            filterEvents();
        });

        cbShowPast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateFilterButton();
            filterEvents();
        });
        updateFilterButton();
    }

    private void updateFilterButton() {
        boolean showPast = cbShowPast.isChecked();
        btnFilter.setText(showPast ? "Passés affichés" : getString(R.string.btn_filter));
        btnFilter.setBackgroundTintList(ContextCompat.getColorStateList(this,
                showPast ? R.color.action_blue : android.R.color.transparent));
        btnFilter.setTextColor(ContextCompat.getColor(this, R.color.white));
        btnFilter.setStrokeColorResource(showPast ? R.color.action_blue : R.color.overlay_white_strong);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.isAdmin()) {
            fabAddEvent.setVisibility(View.VISIBLE);
        } else {
            fabAddEvent.setVisibility(View.GONE);
        }
        filterEvents();
    }

    private void filterEvents() {
        boolean upcomingOnly = !cbShowPast.isChecked();
        List<Event> filteredList = dbHelper.searchAndFilterEvents(searchQuery, selectedCategory, upcomingOnly);

        if (filteredList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvEvents.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvEvents.setVisibility(View.VISIBLE);

            if (adapter == null) {
                adapter = new EventAdapter(this, filteredList, this);
                rvEvents.setAdapter(adapter);
            } else {
                adapter.updateEvents(filteredList);
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
        if (cbShowPast.isChecked()) {
            filterEvents();
        }
    }
}
