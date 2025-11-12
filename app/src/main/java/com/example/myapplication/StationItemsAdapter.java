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
import androidx.core.content.ContextCompat; // --- NEW IMPORT
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StationItemsAdapter extends RecyclerView.Adapter<StationItemsAdapter.ItemViewHolder> {
    private List<String> items = new ArrayList<>();

    private Set<String> favoritesSet = new HashSet<>();
    private Context context;

    public StationItemsAdapter(Context context) {
        this.context = context;
    }

    /**
     * Receives both the list of items for this station AND the user's
     * complete list of favorites.
     */
    public void setItems(List<String> items, Set<String> favorites) {
        this.items = (items != null) ? items : new ArrayList<>();
        this.favoritesSet = (favorites != null) ? favorites : new HashSet<>();
        notifyDataSetChanged(); // Refresh the list
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
        // Pass all necessary data to the bind method
        holder.bind(items.get(position), favoritesSet, context);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemName;
        private final ImageView btnFavorite;

        private FirebaseFirestore db;
        private FirebaseUser currentUser;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);

            db = FirebaseFirestore.getInstance();
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
        }

        /**
         * Binds item data and sets up the favorite toggle logic.
         */
        void bind(String item, Set<String> favoritesSet, Context context) {
            itemName.setText(item);

            if (favoritesSet.contains(item)) {
                setFavorited(true, false);
            } else {
                setFavorited(false, false);
            }

            btnFavorite.setOnClickListener(v -> {
                if (currentUser == null) {
                    Toast.makeText(context, "You must be logged in to add favorites", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean isCurrentlyFavorite = "favorited".equals(btnFavorite.getTag());

                if (isCurrentlyFavorite) {
                    removeFromFavorites(item, favoritesSet, context);
                } else {
                    addToFavorites(item, favoritesSet, context);
                }
            });
        }

        /**
         * Helper method to update the button's appearance.
         */
        private void setFavorited(boolean isFavorite, boolean animate) {
            if (isFavorite) {
                btnFavorite.setImageResource(android.R.drawable.star_on);
                btnFavorite.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.MSU));
                btnFavorite.setTag("favorited");
            } else {
                btnFavorite.setImageResource(android.R.drawable.star_off);
                btnFavorite.setColorFilter(0xFF888888);
                btnFavorite.setTag("unfavorited");
            }
            btnFavorite.setEnabled(true);

            if (animate) {
                btnFavorite.setScaleX(0.5f);
                btnFavorite.setScaleY(0.5f);
                btnFavorite.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        }

        /**
         * Adds this item to Firestore favorites.
         */
        private void addToFavorites(String itemName, Set<String> favoritesSet, Context context) {
            btnFavorite.setEnabled(false);
            String userId = currentUser.getUid();

            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("itemName", itemName);
            favoriteData.put("addedAt", FieldValue.serverTimestamp());

            db.collection("users").document(userId)
                    .collection("favorites").document(itemName)
                    .set(favoriteData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Favorite added: " + itemName);
                        // --- FIXED ---
                        Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                        favoritesSet.add(itemName);
                        setFavorited(true, true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error adding favorite", e);
                        Toast.makeText(context, "Error adding favorite", Toast.LENGTH_LONG).show();
                        btnFavorite.setEnabled(true);
                    });
        }

        /**
         * Removes this item from Firestore favorites.
         */
        private void removeFromFavorites(String itemName, Set<String> favoritesSet, Context context) {
            btnFavorite.setEnabled(false);
            String userId = currentUser.getUid();

            db.collection("users").document(userId)
                    .collection("favorites").document(itemName)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Favorite removed: " + itemName);
                        // --- FIXED ---
                        Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                        favoritesSet.remove(itemName);
                        setFavorited(false, true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error removing favorite", e);
                        Toast.makeText(context, "Error removing favorite", Toast.LENGTH_LONG).show();
                        btnFavorite.setEnabled(true);
                    });
        }
    }
}