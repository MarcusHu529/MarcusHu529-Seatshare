# Sparty's Spreads - MSU Dining Hall Menu App

An Android application for Michigan State University students to browse dining hall menus, view nutrition information, and get directions to dining locations.

## 📱 App Overview

Sparty's Spreads provides an intuitive interface for MSU students to:
- Browse menus for 8 campus dining halls
- View detailed nutrition information for food items
- Get directions to dining halls via Google Maps integration
- Access breakfast, lunch, and dinner menus

## 🏗️ Project Structure

```
├── app/
│   ├── src/main/
│   │   ├── java/com/example/myapplication/
│   │   │   ├── MainActivity.java              # Main landing page with dining hall grid
│   │   │   ├── MenuActivity.java              # Hall-specific menu with meal selection
│   │   │   ├── MenuItemDetailActivity.java    # Detailed nutrition information
│   │   │   ├── MenuAdapter.java               # RecyclerView adapter for menu items
│   │   │   ├── MenuDatabaseHelper.java        # SQLite database management
│   │   │   ├── MenuItem.java                  # Menu item data model
│   │   │   └── MenuItemDetailed.java          # Detailed menu item model
│   │   ├── res/
│   │   │   ├── layout/                        # UI layouts (portrait)
│   │   │   ├── layout-land/                   # Landscape-specific layouts
│   │   │   ├── drawable/                      # Images and vector drawables
│   │   │   ├── values/                        # Strings, colors, dimensions
│   │   │   └── mipmap-*/                      # App icons
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts                       # App-level build configuration
│   └── proguard-rules.pro                     # ProGuard configuration
├── gradle/                                    # Gradle wrapper and version catalog
├── build.gradle.kts                           # Project-level build configuration
├── settings.gradle.kts                        # Project settings
├── navigation_flow.md                         # Detailed app navigation documentation
└── README.md                                  # This file
```

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Electric Eel (2022.1.1) or newer
- **Java**: JDK 17 (required for Android Gradle Plugin 8.12.3)
- **Android SDK**: API level 28 (minimum) to 34 (target)
- **Git**: For version control

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://gitlab.msu.edu/siddavar/cse476_seat_share.git
   cd cse476_seat_share
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory and select it

3. **Sync project**
   - Android Studio will automatically prompt to sync Gradle
   - If not, click "Sync Now" or go to File → Sync Project with Gradle Files

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click the "Run" button or press Shift+F10

### Java Version Setup
If you encounter Java version issues, ensure Java 17 is installed:

**Using SDKMAN (Recommended)**
```bash
curl -s "https://get.sdkman.io" | bash
source ~/.sdkman/bin/sdkman-init.sh
sdk install java 17.0.13-tem
sdk use java 17.0.13-tem
```

**Using Homebrew (macOS)**
```bash
brew install openjdk@17
```

## 🏛️ App Architecture

### Navigation Flow
```
Main Activity → Menu Activity → Menu Item Detail Activity
     ↑              ↓                    ↓
     └──────── Back Button ──────────────┘
```

### Key Components

1. **MainActivity**: Landing page with 8 dining hall options in a responsive grid layout
2. **MenuActivity**: Displays hall-specific menus with meal time selection (Breakfast/Lunch/Dinner)
3. **MenuItemDetailActivity**: Shows detailed nutrition information, allergens, and ingredients
4. **MenuDatabaseHelper**: Manages SQLite database with menu items and hall associations

### Database Schema
- **menu_items**: Core table with food details, nutrition info, allergens
- **hall_menus**: Junction table linking dining halls to menu items by meal time

## 🔧 Development Guidelines

### Code Style
- Follow standard Java conventions
- Use meaningful variable and method names
- Keep methods focused and concise
- Add comments for complex logic

### Git Workflow
1. Create feature branches from `main`
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Make commits with descriptive messages
3. Push changes and create merge requests
4. Ensure code passes lint checks before merging

### Testing
- Run lint checks: `./gradlew lint`
- Run unit tests: `./gradlew test`
- Test on both portrait and landscape orientations
- Verify on different screen sizes

### Lint and Code Quality
This project maintains high code quality standards:
- ✅ No deprecated API usage
- ✅ Proper accessibility support (contentDescription attributes)
- ✅ Internationalization ready (no hardcoded strings)
- ✅ Package visibility declarations for external intents

## 📋 Features

### Current Features
- **8 MSU Dining Halls**: Brody, Case, Owen, Shaw, Akers, Landon, Holden, Hubbard
- **Meal Time Selection**: Breakfast, Lunch, Dinner menus
- **Detailed Nutrition Info**: Calories, fat, protein, carbs, fiber, sugar
- **Allergen Warnings**: Clear display of food allergens
- **Google Maps Integration**: One-tap directions to dining halls
- **Responsive Design**: Optimized for portrait and landscape orientations
- **State Preservation**: Maintains user selections during orientation changes

### Planned Features
- [ ] Seating availability checker
- [ ] Menu favoriting and meal planning
- [ ] Real-time menu updates
- [ ] Push notifications for special meals
- [ ] Dietary filter options (vegetarian, vegan, gluten-free)

## 🤝 Contributing

### Setting Up for Development
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests and lint checks
5. Submit a merge request

### Code Review Process
- All changes require code review
- Ensure lint checks pass
- Test on multiple devices/orientations
- Update documentation if needed

### Issue Reporting
When reporting issues, please include:
- Android version and device model
- Steps to reproduce
- Expected vs actual behavior
- Screenshots if applicable

## 📄 License

This project is developed for CSE 476 - Mobile Application Development at Michigan State University.

## 👥 Team

- **Repository Owner**: siddavar
- **Course**: CSE 476 - Mobile Application Development
- **Institution**: Michigan State University

## 📞 Support

For technical issues or questions:
- Create an issue in this repository
- Contact the development team
- Check the [navigation flow documentation](navigation_flow.md) for detailed app behavior

---

**Note**: This app uses sample data for demonstration. In a production environment, menu data would be fetched from MSU's dining services API.