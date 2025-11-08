package com.example.myapplication;

public class MenuItemDetailed {
    private int id;
    private String name;
    private String description;
    private String category;
    private int calories;
    private double fat;
    private double protein;
    private double carbs;
    private double fiber;
    private double sugar;
    private String allergens;
    private String ingredients;
    private String imagePath;
    private double price;

    public MenuItemDetailed() {}

    public MenuItemDetailed(int id, String name, String description, String category,
                           int calories, double fat, double protein, double carbs,
                           double fiber, double sugar, String allergens, String ingredients,
                           String imagePath, double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.calories = calories;
        this.fat = fat;
        this.protein = protein;
        this.carbs = carbs;
        this.fiber = fiber;
        this.sugar = sugar;
        this.allergens = allergens;
        this.ingredients = ingredients;
        this.imagePath = imagePath;
        this.price = price;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFiber() { return fiber; }
    public void setFiber(double fiber) { this.fiber = fiber; }

    public double getSugar() { return sugar; }
    public void setSugar(double sugar) { this.sugar = sugar; }

    public String getAllergens() { return allergens; }
    public void setAllergens(String allergens) { this.allergens = allergens; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}