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
import java.util.Set;
import java.util.HashSet;

public class StationItemsAdapter extends RecyclerView.Adapter<StationItemsAdapter.ItemViewHolder> {
    private List<String> items;
    private Set<String> userFavorites = new HashSet<>();

    public void setUserFavorites(Set<String> favorites) {
        this.userFavorites = (favorites != null) ? favorites : new HashSet<>();
        notifyDataSetChanged(); // Re-bind items to show new favorite status
    }
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

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemName;
        private final ImageView btnFavorite; // The favorite button

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            btnFavorite = itemView.findViewById(R.id.btnFavorite); // Find the favorite button
        }

        void bind(String item) {
            itemName.setText(item);

            // Check if the item is in the set
            if (userFavorites.contains(item)) {
                // --- STATE: IS A FAVORITE ---
                btnFavorite.setImageResource(android.R.drawable.star_on);
                btnFavorite.setColorFilter(0xFFFFC107); // Yellow
                btnFavorite.setEnabled(true); // <-- IMPORTANT: Set to true so it's clickable

                // Set the click listener to REMOVE the favorite
                btnFavorite.setOnClickListener(v -> {
                    removeFavoriteDataFromServer(item, v.getContext());
                });
            } else {
                // --- STATE: IS NOT A FAVORITE ---
                btnFavorite.setImageResource(android.R.drawable.star_off);
                btnFavorite.setColorFilter(0xFF888888); // Grey
                btnFavorite.setEnabled(true);

                // Set the click listener to ADD the favorite
                btnFavorite.setOnClickListener(v -> {
                    sendFavoriteDataToServer(item, v.getContext());
                });
            }
        }

        /**
         * Removes the selected favorite item from the Firestore server.
         */
        void removeFavoriteDataFromServer(String itemName, Context context) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(context, "Please log in to change favorites", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = currentUser.getUid();

            // --- OPTIMISTIC UI UPDATE ---
            // Change the star to "off" immediately.
            btnFavorite.setImageResource(android.R.drawable.star_off);
            btnFavorite.setColorFilter(0xFF888888); // Grey
            userFavorites.remove(itemName); // Remove from local set

            // Swap the listener back to "add"
            btnFavorite.setOnClickListener(v -> {
                sendFavoriteDataToServer(itemName, v.getContext());
            });

            // --- FIREBASE CALL ---
            // Get the Firestore instance and delete the document
            FirebaseFirestore.getInstance()
                    .collection("users").document(userId)
                    .collection("favorites").document(itemName)
                    .delete() // <-- This is the delete command
                    .addOnSuccessListener(aVoid -> {
                        // --- SUCCESS ---
                        // UI is already correct.
                        Log.d("Favorite", "Favorite removed from Firestore: " + itemName);
                        Toast.makeText(context, itemName + " removed from favorites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // --- FAILURE ---
                        // An error occurred (e.g., no network)
                        Log.w("Favorite", "Error removing favorite", e);
                        Toast.makeText(context, "Failed to remove favorite. Please try again.", Toast.LENGTH_SHORT).show();

                        // --- REVERT THE UI ---
                        // Since the delete failed, set the star back to "on"
                        btnFavorite.setImageResource(android.R.drawable.star_on);
                        btnFavorite.setColorFilter(0xFFFFC107); // Yellow
                        userFavorites.add(itemName); // Add it back to the local set

                        // Swap the listener back to "remove"
                        btnFavorite.setOnClickListener(v -> {
                            removeFavoriteDataFromServer(itemName, v.getContext());
                        });
                    });
        }

        /**
         * Sends the selected favorite item to the Firestore server.
         */
        void sendFavoriteDataToServer(String itemName, Context context) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(context, "Please log in to save favorites", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = currentUser.getUid();

            // --- OPTIMISTIC UI UPDATE ---
            // Change the star to "on" immediately.
            btnFavorite.setImageResource(android.R.drawable.star_on);
            btnFavorite.setColorFilter(0xFFFFC107); // Yellow
            userFavorites.add(itemName); // Add to local set

            // Swap the listener to "remove"
            btnFavorite.setOnClickListener(v -> {
                removeFavoriteDataFromServer(itemName, v.getContext());
            });

            // --- FIREBASE CALL ---
            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("name", itemName);
            favoriteData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

            FirebaseFirestore.getInstance()
                    .collection("users").document(userId)
                    .collection("favorites").document(itemName)
                    .set(favoriteData)
                    .addOnSuccessListener(aVoid -> {
                        // --- SUCCESS ---
                        // UI is already correct.
                        Log.d("Favorite", "Favorite added to Firestore: " + itemName);
                        Toast.makeText(context, itemName + " added to favorites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // --- FAILURE ---
                        Log.w("Favorite", "Error adding favorite to Firestore", e);
                        Toast.makeText(context, "Failed to save favorite. Please try again.", Toast.LENGTH_SHORT).show();

                        // --- REVERT THE UI ---
                        // Since the save failed, set the star back to "off"
                        btnFavorite.setImageResource(android.R.drawable.star_off);
                        btnFavorite.setColorFilter(0xFF888888); // Grey
                        userFavorites.remove(itemName); // Remove from local set

                        // Swap the listener back to "add"
                        btnFavorite.setOnClickListener(v -> {
                            sendFavoriteDataToServer(itemName, v.getContext());
                        });
                    });
        }
    }
}