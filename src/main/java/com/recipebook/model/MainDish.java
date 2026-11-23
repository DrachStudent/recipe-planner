package com.recipebook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Основна страва: калорійність розраховується на одну порцію.
 */
public class MainDish extends Recipe {
    private String cookingMethod = "Приготування";

    public MainDish() {
    }

    public MainDish(String name, String category, int prepTimeMinutes, int servings,
                    List<RecipeItem> items, String cookingMethod) {
        super(name, category, prepTimeMinutes, servings, items);
        setCookingMethod(cookingMethod);
    }

    public String getCookingMethod() {
        return cookingMethod;
    }

    public void setCookingMethod(String cookingMethod) {
        this.cookingMethod = (cookingMethod == null || cookingMethod.trim().isEmpty())
                ? "Приготування" : cookingMethod.trim();
    }

    @Override
    public double calculateCalories() {
        double perServing = getTotalCalories() / getServings();
        return Math.round(perServing * 10.0) / 10.0;
    }

    @Override
    @JsonIgnore
    public String getCalorieUnit() {
        return "ккал/порція";
    }

    @Override
    @JsonIgnore
    public String getTypeLabel() {
        return "Основна страва";
    }
}