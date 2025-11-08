# Sparty's Spreads - Navigation Flow Diagram

## App Flow Overview

```
[Main Screen]
    ↓ (tap dining hall)
[Menu Screen]
    ↓ (tap menu item)
[Menu Item Detail Screen]
```

## Detailed Navigation Flow

### 1. Main Activity (Landing Page)
**Screen Elements:**
- Title: "SPARTY'S SPREADS"
- 8 Dining Hall buttons with images (2x4 grid in portrait, 4x2 grid in landscape)
- Each hall: Brody, Case, Owen, Shaw, Akers, Landon, Holden, Hubbard

**User Actions:**
- Tap any dining hall → Navigate to Menu Activity

---

### 2. Menu Activity (Hall-Specific Menu)
**Screen Elements:**
- Title: "[Hall Name] Menu"
- Action buttons: Back, Seating Options, Directions
- Meal selection buttons: Breakfast, Lunch, Dinner
- Scrollable list of menu items for selected meal

**User Actions:**
- Tap "Back" → Return to Main Activity
- Tap "Directions" → Open Google Maps with navigation
- Tap "Seating Options" → (Placeholder for future feature)
- Tap meal button → Load different menu items
- Tap menu item → Navigate to Menu Item Detail Activity

**State Management:**
- Preserves selected meal time during orientation changes
- Loads hall-specific menu items from SQLite database

---

### 3. Menu Item Detail Activity (Nutrition Information)
**Screen Elements:**
- Back button
- Food image placeholder
- Food name, category, price
- Description
- Detailed nutrition information (calories, fat, protein, carbs, fiber, sugar)
- Allergen warnings
- Ingredient list

**User Actions:**
- Tap "Back" → Return to Menu Activity

**Data Source:**
- Detailed information pulled from SQLite database

---

## Responsive Design Features

### Portrait vs Landscape Layouts:
- **Main Activity**: 2x4 grid (portrait) → 4x2 grid (landscape)
- **Menu Activity**: Vertical layout (portrait) → Side-by-side layout (landscape)
- **Detail Activity**: Single column (portrait) → Two-column layout (landscape)

### State Preservation:
- Selected meal time maintained during orientation changes
- Smooth transitions between portrait and landscape modes
- No data loss during configuration changes

---

## Database Integration

### Tables:
1. **MenuItems**: Master table with detailed food information
2. **HallMenus**: Junction table linking halls + meal times to menu items

### Sample Data:
- 14 food items with complete nutrition information
- Hall-specific specialty items
- Allergen and ingredient information
- Pricing data
- Image placeholders for future implementation

---

## Key App Features:
1. **Interactive Navigation**: Touch-based navigation between all screens
2. **Dynamic Content**: Menu items change based on hall and meal selection
3. **Rich Information**: Detailed nutrition facts and allergen warnings
4. **External Integration**: Google Maps integration for directions
5. **Responsive UI**: Optimized layouts for both orientations
6. **State Management**: Preserves user selections during orientation changes