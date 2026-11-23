package com.recipebook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Десерт: калорійність розраховується на 100 г продукту.
 */
public class Dessert extends Recipe {
    private boolean containsSugar = true;

    public Dessert() {
    }

    public Dessert(String name, String category, int prepTimeMinutes, int servings,
                   List<RecipeItem> items, boolean containsSugar) {
        super(name, category, prepTimeMinutes, servings, items);
        this.containsSugar = containsSugar;
    }

    public boolean isContainsSugar() {
        return containsSugar;
    }

    public void setContainsSugar(boolean containsSugar) {
        this.containsSugar = containsSugar;
    }

    @Override
    public double calculateCalories() {
        double totalGrams = getTotalAmount();
        if (totalGrams <= 0) {
            return 0.0;
        }
        double per100g = (getTotalCalories() / totalGrams) * 100.0;
        return Math.round(per100g * 10.0) / 10.0;
    }

    @Override
    @JsonIgnore
    public String getCalorieUnit() {
        return "ккал/100 г";
    }

    @Override
    @JsonIgnore
    public String getTypeLabel() {
        return "Десерт";
    }
}