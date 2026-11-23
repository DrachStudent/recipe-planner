package com.recipebook.model;

/**
 * Один запис у тижневому плані меню (день, прийом їжі, рецепт, кількість порцій).
 */
public class MenuEntry {
    private String dayOfWeek;
    private String mealType;
    private String recipeName;
    private int servings;

    public MenuEntry() {
    }

    public MenuEntry(String dayOfWeek, String mealType, String recipeName, int servings) {
        setDayOfWeek(dayOfWeek);
        setMealType(mealType);
        setRecipeName(recipeName);
        setServings(servings);
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek.trim().isEmpty()) {
            throw new IllegalArgumentException("День тижня є обов'язковим");
        }
        this.dayOfWeek = dayOfWeek.trim();
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        if (mealType == null || mealType.trim().isEmpty()) {
            throw new IllegalArgumentException("Прийом їжі є обов'язковим");
        }
        this.mealType = mealType.trim();
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        if (recipeName == null || recipeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Назва рецепта у меню є обов'язковою");
        }
        this.recipeName = recipeName.trim();
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
}