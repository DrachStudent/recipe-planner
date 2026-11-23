package com.recipebook.model;

/**
 * Виняток, що виникає, якщо інгредієнт рецепта відсутній у довіднику.
 */
public class MissingIngredientException extends RuntimeException {
    public MissingIngredientException(String ingredientName) {
        super("Відсутній інгредієнт у довіднику: " + ingredientName);
    }
}