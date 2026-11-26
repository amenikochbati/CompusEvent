package com.example.compusevent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Carte d'événement : informations, statut métier et accès rapide aux favoris. */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);

        void onFavoriteToggle(Event event, boolean newFavoriteStatus);
    }

    private final Context context;
    private List<Event> eventList;
    private final OnEventClickListener listener;
    private final DatabaseHelper dbHelper;

    public EventAdapter(Context context, List<Event> eventList, OnEventClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
        this.dbHelper = new DatabaseHelper(context);
    }

    public void updateEvents(List<Event> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        int active = dbHelper.getActiveRegistrationCount(event.getId());
        int available = EventUtils.getAvailablePlaces(event, active);

        holder.tvTitle.setText(event.getTitle());
        holder.tvCategory.setText(event.getCategory());
        holder.tvDate.setText("📅 " + EventUtils.formatFrenchDate(event.getDate()) + " (" + event.getTime() + ")");
        holder.tvLocation.setText("📍 " + event.getLocation());
        holder.tvDescription.setText(event.getDescription());

        String status = EventUtils.getStatusLabel(event, active);
        // L'historique affiche l'état de l'inscription plutôt que l'état de l'événement.
        if (DatabaseHelper.STATUS_CANCELLED.equals(event.getRegistrationStatus())) {
            status = "Annulée";
        } else if (DatabaseHelper.STATUS_ACTIVE.equals(event.getRegistrationStatus())) {
            status = "Confirmée";
        }
        holder.tvStatus.setText(status);
        int statusColor;
        if ("Annulée".equals(status)) {
            statusColor = R.color.error_red;
        } else if ("Confirmée".equals(status)) {
            statusColor = R.color.action_blue;
        } else if ("Terminé".equals(status)) {
            statusColor = R.color.text_muted;
        } else if ("Complet".equals(status)) {
            statusColor = R.color.error_red;
        } else if ("Places limitées".equals(status)) {
            statusColor = R.color.warning_orange;
        } else {
            statusColor = R.color.weather_turquoise;
        }
        holder.tvStatus.setBackgroundColor(ContextCompat.getColor(context, statusColor));
        holder.tvPlaces.setText(context.getString(R.string.label_available_places) + " : " + available
                + " / " + event.getCapacity());

        if (event.isFavorite()) {
            holder.btnFavoriteIcon.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.btnFavoriteIcon.setImageResource(android.R.drawable.btn_star_big_off);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });

        holder.btnFavoriteIcon.setOnClickListener(v -> {
            boolean newStatus = !event.isFavorite();
            event.setFavorite(newStatus);
            if (newStatus) {
                dbHelper.addFavorite(event.getId());
                Toast.makeText(context, "Ajouté aux favoris", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.removeFavorite(event.getId());
                Toast.makeText(context, "Retiré des favoris", Toast.LENGTH_SHORT).show();
            }
            notifyItemChanged(position);
            if (listener != null) {
                listener.onFavoriteToggle(event, newStatus);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvTitle, tvDate, tvLocation, tvDescription, tvStatus, tvPlaces;
        ImageView btnFavoriteIcon;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvPlaces = itemView.findViewById(R.id.tv_places);
            btnFavoriteIcon = itemView.findViewById(R.id.btn_favorite_icon);
        }
    }
}
