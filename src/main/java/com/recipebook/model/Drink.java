package com.recipebook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Напій: калорійність розраховується на 100 мл.
 */
public class Drink extends Recipe {
    private boolean servedCold = true;

    public Drink() {
    }

    public Drink(String name, String category, int prepTimeMinutes, int servings,
                 List<RecipeItem> items, boolean servedCold) {
        super(name, category, prepTimeMinutes, servings, items);
        this.servedCold = servedCold;
    }

    public boolean isServedCold() {
        return servedCold;
    }

    public void setServedCold(boolean servedCold) {
        this.servedCold = servedCold;
    }

    @Override
    public double calculateCalories() {
        double totalMl = getTotalAmount();
        if (totalMl <= 0) {
            return 0.0;
        }
        double per100ml = (getTotalCalories() / totalMl) * 100.0;
        return Math.round(per100ml * 10.0) / 10.0;
    }

    @Override
    @JsonIgnore
    public String getCalorieUnit() {
        return "ккал/100 мл";
    }

    @Override
    @JsonIgnore
    public String getTypeLabel() {
        return "Напій";
    }
}