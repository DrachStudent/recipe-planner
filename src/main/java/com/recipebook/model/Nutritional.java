package com.recipebook.model;

/**
 * Інтерфейс для об'єктів, що мають харчову цінність (калорійність).
 */
public interface Nutritional {
    /**
     * Розраховує калорійність згідно з типом страви.
     */
    double calculateCalories();

    /**
     * Повертає одиницю виміру калорійності (на порцію, на 100 г або на 100 мл).
     */
    String getCalorieUnit();
}