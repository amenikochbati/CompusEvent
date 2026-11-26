package com.example.compusevent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
        void onFavoriteToggle(Event event, boolean newFavoriteStatus);
    }

    private Context context;
    private List<Event> eventList;
    private OnEventClickListener listener;
    private DatabaseHelper dbHelper;

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

        holder.tvTitle.setText(event.getTitle());
        holder.tvCategory.setText(event.getCategory());
        holder.tvDate.setText("📅 " + event.getDate() + " (" + event.getTime() + ")");
        holder.tvLocation.setText("📍 " + event.getLocation());
        holder.tvDescription.setText(event.getDescription());

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
        TextView tvCategory, tvTitle, tvDate, tvLocation, tvDescription;
        ImageView btnFavoriteIcon;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvDescription = itemView.findViewById(R.id.tv_description);
            btnFavoriteIcon = itemView.findViewById(R.id.btn_favorite_icon);
        }
    }
}
