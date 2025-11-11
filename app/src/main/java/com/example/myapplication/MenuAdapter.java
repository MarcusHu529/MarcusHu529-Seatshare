package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuAdapter - RecyclerView adapter for displaying menu items in a list
 *
 * This adapter manages the display of menu items for a specific dining hall and meal time.
 * Each item shows basic information (name, description, category) and handles click events
 * to navigate to detailed nutrition information.
 *
 * Features:
 * - Displays menu items in card layout format
 * - Handles item click navigation to MenuItemDetailActivity
 * - Supports dynamic menu updates when meal time changes
 * - Uses ViewHolder pattern for efficient scrolling performance
 *
 * Layout: Uses menu_item_card.xml for individual item display
 */
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    // Data and Context
    private List<MenuItem> menuItems;
    private Context context;

    /**
     * Creates a new MenuAdapter with empty menu list
     *
     * @param context The context for creating intents and accessing resources
     */
    public MenuAdapter(Context context) {
        this.context = context;
        this.menuItems = new ArrayList<>();
    }

    /**
     * Creates ViewHolder instances for menu item cards
     *
     * @param parent   The parent ViewGroup
     * @param viewType The view type (not used, all items use same layout)
     * @return New MenuViewHolder instance
     */
    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_item_card, parent, false);
        return new MenuViewHolder(view);
    }

    /**
     * Binds menu item data to the ViewHolder at the specified position
     *
     * @param holder   The ViewHolder to bind data to
     * @param position The position in the menu items list
     */
    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);
        holder.bind(item, context);
    }

    /**
     * Returns the total number of menu items in the adapter
     *
     * @return The size of the menu items list
     */
    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    /**
     * Updates the adapter with a new list of menu items
     * This method is called when the user switches meal times (Breakfast/Lunch/Dinner)
     *
     * @param newItems The new list of menu items to display
     */
    public void updateMenuItems(List<MenuItem> newItems) {
        this.menuItems.clear();
        if (newItems != null) {
            this.menuItems.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for individual menu item cards
     * Handles the display and click events for each menu item
     */
    static class MenuViewHolder extends RecyclerView.ViewHolder {

        // UI Components
        private TextView tvItemName;
        private TextView tvItemDescription;
        private TextView tvItemCategory;

        /**
         * Creates a new ViewHolder and initializes its views
         *
         * @param itemView The card view for this menu item
         */
        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemDescription = itemView.findViewById(R.id.tvItemDescription);
            tvItemCategory = itemView.findViewById(R.id.tvItemCategory);
        }

        /**
         * Binds menu item data to the view components and sets up click listener
         *
         * @param item    The menu item to display
         * @param context The context for creating navigation intents
         */
        public void bind(MenuItem item, Context context) {
            // Populate text fields with menu item data
            tvItemName.setText(item.getName());
            tvItemDescription.setText(item.getDescription());
            tvItemCategory.setText(item.getCategory());

            // Set click listener to navigate to detailed view
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, MenuItemDetailActivity.class);
                intent.putExtra(MenuItemDetailActivity.EXTRA_ITEM_NAME, item.getName());
                context.startActivity(intent);
            });
        }
    }
}