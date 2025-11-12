package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StationItemsAdapter extends RecyclerView.Adapter<StationItemsAdapter.ItemViewHolder> {
    private List<String> items;

    public StationItemsAdapter() {
        this.items = new ArrayList<>();
    }

    public void setItems(List<String> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_station_menu_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemName;
        private final ImageView btnFavorite; // The favorite button

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            btnFavorite = itemView.findViewById(R.id.btnFavorite); // Find the favorite button
        }

        void bind(String item) {
            itemName.setText(item);

            btnFavorite.setOnClickListener(v -> {
                sendFavoriteDataToServer(item, v.getContext());
                btnFavorite.setImageResource(android.R.drawable.star_on);
                btnFavorite.setColorFilter(0xFFFFC107); // Set tint to yellow
                btnFavorite.setEnabled(false);
            });
        }

        void sendFavoriteDataToServer(String itemName, Context context) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(context, "You must be logged in to add favorites", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = currentUser.getUid();
            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("itemName", itemName);
            favoriteData.put("addedAt", FieldValue.serverTimestamp());
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .collection("favorites").document(itemName)
                    .set(favoriteData)
                    .addOnSuccessListener(aVoid -> {

                        Log.d("Firestore", "Favorite added successfully!");
                        Toast.makeText(context,
                                itemName + " added to favorites!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error adding favorite", e);
                        Toast.makeText(context,
                                "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnFavorite.setImageResource(android.R.drawable.star_off);
                        btnFavorite.setColorFilter(0xFF888888); // Set tint back to grey
                        btnFavorite.setEnabled(true);
                    });
        }
    }
}