package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MenuStationAdapter extends RecyclerView.Adapter<MenuStationAdapter.StationViewHolder> {
    private List<MenuStation> stations;
    private boolean expandAll = false;

    // --- NEW ---
    private Set<String> favoritesSet = new HashSet<>();
    private Context context;

    public MenuStationAdapter(Context context) {
        this.stations = new ArrayList<>();
        this.context = context; // Store context
    }

    public void setStations(List<MenuStation> stations) {
        this.stations = stations != null ? stations : new ArrayList<>();
        notifyDataSetChanged();
    }

    // --- NEW FUNCTION ---
    /**
     * Receives the list of favorites from the Activity.
     */
    public void setFavorites(Set<String> favorites) {
        this.favoritesSet = (favorites != null) ? favorites : new HashSet<>();
        // We don't need to notifyDataSetChanged() here,
        // because setStations() will be called right after, which does.
    }

    public void toggleExpandAll() {
        expandAll = !expandAll;
        for (MenuStation station : stations) {
            station.setExpanded(expandAll);
        }
        notifyDataSetChanged();
    }

    public boolean areAllExpanded() {
        if (stations.isEmpty()) return false;
        for (MenuStation station : stations) {
            if (!station.isExpanded()) return false;
        }
        return true;
    }

    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_station, parent, false);
        // --- UPDATED ---
        // Pass context to the ViewHolder so it can create the child adapter
        return new StationViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        // --- UPDATED ---
        // Pass the favorites list to the bind method
        holder.bind(stations.get(position), favoritesSet);
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    static class StationViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout stationHeader;
        private final ImageView expandArrow;
        private final TextView stationName;
        private final TextView itemCount;
        private final LinearLayout itemsContainer;
        private final RecyclerView itemsRecyclerView;
        private final StationItemsAdapter itemsAdapter; // This is the child adapter

        StationViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            stationHeader = itemView.findViewById(R.id.stationHeader);
            expandArrow = itemView.findViewById(R.id.expandArrow);
            stationName = itemView.findViewById(R.id.stationName);
            itemCount = itemView.findViewById(R.id.itemCount);
            itemsContainer = itemView.findViewById(R.id.itemsContainer);
            itemsRecyclerView = itemView.findViewById(R.id.itemsRecyclerView);

            // Pass context to the child adapter
            itemsAdapter = new StationItemsAdapter(context);
            itemsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            itemsRecyclerView.setAdapter(itemsAdapter);
        }

        void bind(MenuStation station, Set<String> favorites) {
            stationName.setText(station.getStationName());

            int count = station.getItemCount();
            itemCount.setText(count + (count == 1 ? " item" : " items"));

            // Pass both the items AND the favorites list to the child adapter
            itemsAdapter.setItems(station.getItems(), favorites);

            updateExpandedState(station.isExpanded());

            stationHeader.setOnClickListener(v -> {
                station.toggleExpanded();
                animateExpansion(station.isExpanded());
            });
        }

        private void updateExpandedState(boolean isExpanded) {
            itemsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            expandArrow.setRotation(isExpanded ? 0 : 270);
        }

        private void animateExpansion(boolean expand) {
            float targetRotation = expand ? 0 : 270;
            ObjectAnimator rotation = ObjectAnimator.ofFloat(expandArrow, "rotation", targetRotation);
            rotation.setDuration(200);
            rotation.start();

            if (expand) {
                itemsContainer.setVisibility(View.VISIBLE);
                itemsContainer.setAlpha(0f);
                itemsContainer.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start();
            } else {
                itemsContainer.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> itemsContainer.setVisibility(View.GONE))
                        .start();
            }
        }
    }
}