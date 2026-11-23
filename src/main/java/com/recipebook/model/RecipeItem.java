package com.recipebook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Позиція у складі рецепта: посилання на інгредієнт та його кількість (у г або мл).
 */
public class RecipeItem {
    private String ingredientName;
    private double amount;

    // Посилання на об'єкт із довідника встановлюється після завантаження JSON
    @JsonIgnore
    private Ingredient ingredient;

    public RecipeItem() {
    }

    public RecipeItem(String ingredientName, double amount) {
        setIngredientName(ingredientName);
        setAmount(amount);
    }

    public RecipeItem(Ingredient ingredient, double amount) {
        if (ingredient == null) {
            throw new IllegalArgumentException("Інгредієнт не може бути null");
        }
        this.ingredient = ingredient;
        setIngredientName(ingredient.getName());
        setAmount(amount);
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        if (ingredientName == null || ingredientName.trim().isEmpty()) {
            throw new IllegalArgumentException("Назва інгредієнта у складі не може бути порожньою");
        }
        this.ingredientName = ingredientName.trim();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Кількість інгредієнта має бути додатною");
        }
        this.amount = amount;
    }

    @JsonIgnore
    public Ingredient getIngredient() {
        return ingredient;
    }

    @JsonIgnore
    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    /**
     * Обчислює калорійність цієї позиції рецепта.
     */
    @JsonIgnore
    public double getItemCalories() {
        if (ingredient == null) {
            throw new MissingIngredientException(ingredientName);
        }
        return (ingredient.getCaloriesPer100() * amount) / 100.0;
    }

    @Override
    public String toString() {
        String unit = (ingredient != null) ? ingredient.getUnit() : "г";
        return ingredientName + " — " + amount + " " + unit;
    }
}