package com.recipebook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Абстрактний базовий клас рецепта. Реалізує інтерфейс Nutritional
 * та містить спільні поля і валідацію для всіх видів страв.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MainDish.class, name = "MAIN_DISH"),
        @JsonSubTypes.Type(value = Dessert.class, name = "DESSERT"),
        @JsonSubTypes.Type(value = Drink.class, name = "DRINK")
})
public abstract class Recipe implements Nutritional {
    private String name;
    private String category;
    private int prepTimeMinutes;
    private int servings;
    private List<RecipeItem> items = new ArrayList<>();

    protected Recipe() {
    }

    protected Recipe(String name, String category, int prepTimeMinutes, int servings, List<RecipeItem> items) {
        setName(name);
        setCategory(category);
        setPrepTimeMinutes(prepTimeMinutes);
        setServings(servings);
        setItems(items);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Назва рецепта є обов'язковою");
        }
        this.name = name.trim();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Категорія рецепта є обов'язковою");
        }
        this.category = category.trim();
    }

    public int getPrepTimeMinutes() {
        return prepTimeMinutes;
    }

    public void setPrepTimeMinutes(int prepTimeMinutes) {
        if (prepTimeMinutes <= 0) {
            throw new IllegalArgumentException("Час приготування має бути додатним");
        }
        this.prepTimeMinutes = prepTimeMinutes;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        if (servings <= 0) {
            throw new IllegalArgumentException("Кількість порцій має бути додатною");
        }
        this.servings = servings;
    }

    public List<RecipeItem> getItems() {
        return items;
    }

    public void setItems(List<RecipeItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Склад рецепта не може бути порожнім");
        }
        this.items = new ArrayList<>(items);
    }

    /**
     * Повна перевірка коректності даних рецепта.
     */
    public void validate() {
        setName(this.name);
        setCategory(this.category);
        setPrepTimeMinutes(this.prepTimeMinutes);
        setServings(this.servings);
        if (this.items == null || this.items.isEmpty()) {
            throw new IllegalArgumentException("Склад рецепта не може бути порожнім");
        }
    }

    /**
     * Загальна калорійність усіх інгредієнтів рецепта.
     */
    @JsonIgnore
    public double getTotalCalories() {
        double sum = 0.0;
        for (RecipeItem item : items) {
            sum += item.getItemCalories();
        }
        return sum;
    }

    /**
     * Загальна маса або об'єм усіх інгредієнтів рецепта.
     */
    @JsonIgnore
    public double getTotalAmount() {
        double sum = 0.0;
        for (RecipeItem item : items) {
            sum += item.getAmount();
        }
        return sum;
    }

    /**
     * Назва типу рецепта для відображення в інтерфейсі.
     */
    @JsonIgnore
    public abstract String getTypeLabel();

    @JsonIgnore
    public String getFormattedCalories() {
        return String.format("%.1f %s", calculateCalories(), getCalorieUnit());
    }

    @Override
    public String toString() {
        return name + " (" + getTypeLabel() + ")";
    }
}