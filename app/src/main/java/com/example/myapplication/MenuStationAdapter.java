package com.example.myapplication;

import android.animation.ObjectAnimator;
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
import java.util.List;

public class MenuStationAdapter extends RecyclerView.Adapter<MenuStationAdapter.StationViewHolder> {
    private List<MenuStation> stations;
    private boolean expandAll = false;

    public MenuStationAdapter() {
        this.stations = new ArrayList<>();
    }

    public void setStations(List<MenuStation> stations) {
        this.stations = stations != null ? stations : new ArrayList<>();
        notifyDataSetChanged();
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
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        holder.bind(stations.get(position));
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    class StationViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout stationHeader;
        private final ImageView expandArrow;
        private final TextView stationName;
        private final TextView itemCount;
        private final LinearLayout itemsContainer;
        private final RecyclerView itemsRecyclerView;
        private final StationItemsAdapter itemsAdapter;

        StationViewHolder(@NonNull View itemView) {
            super(itemView);
            stationHeader = itemView.findViewById(R.id.stationHeader);
            expandArrow = itemView.findViewById(R.id.expandArrow);
            stationName = itemView.findViewById(R.id.stationName);
            itemCount = itemView.findViewById(R.id.itemCount);
            itemsContainer = itemView.findViewById(R.id.itemsContainer);
            itemsRecyclerView = itemView.findViewById(R.id.itemsRecyclerView);

            itemsAdapter = new StationItemsAdapter();
            itemsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            itemsRecyclerView.setAdapter(itemsAdapter);
        }

        void bind(MenuStation station) {
            stationName.setText(station.getStationName());

            int count = station.getItemCount();
            itemCount.setText(count + (count == 1 ? " item" : " items"));

            itemsAdapter.setItems(station.getItems());

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