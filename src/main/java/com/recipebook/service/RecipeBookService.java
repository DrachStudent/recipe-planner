package com.recipebook.service;

import com.recipebook.model.*;
import com.recipebook.repository.RecipeBookData;

import java.util.*;
import java.util.logging.Logger;

/**
 * Основний сервіс керування даними довідника (CRUD-операції, перевірка цілісності та зв'язків).
 */
public class RecipeBookService {
    private static final Logger LOGGER = AppLogger.getLogger();

    private final List<Ingredient> ingredients = new ArrayList<>();
    private final List<Recipe> recipes = new ArrayList<>();
    private final MenuPlan menuPlan = new MenuPlan();

    /**
     * Завантажує повний набір даних у сервіс (після імпорту або генерації).
     */
    public void loadData(RecipeBookData data) {
        ingredients.clear();
        recipes.clear();
        menuPlan.getEntries().clear();

        if (data != null) {
            ingredients.addAll(data.getIngredients());
            recipes.addAll(data.getRecipes());
            if (data.getMenuPlan() != null) {
                menuPlan.getEntries().addAll(data.getMenuPlan().getEntries());
            }
        }
        relinkAllRecipeItems();
    }

    /**
     * Повертає знімок усіх даних для експорту у JSON.
     */
    public RecipeBookData exportData() {
        return new RecipeBookData(
                new ArrayList<>(ingredients),
                new ArrayList<>(recipes),
                new MenuPlan(new ArrayList<>(menuPlan.getEntries()))
        );
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public MenuPlan getMenuPlan() {
        return menuPlan;
    }

    // --- CRUD для інгредієнтів ---

    public void addIngredient(Ingredient ingredient) {
        if (findIngredientByName(ingredient.getName()).isPresent()) {
            throw new IllegalArgumentException("Інгредієнт із такою назвою вже існує: " + ingredient.getName());
        }
        ingredients.add(ingredient);
        LOGGER.info("Додано інгредієнт: " + ingredient.getName());
    }

    public void updateIngredient(Ingredient existing, String newName, double newCalories, String newUnit) {
        String oldName = existing.getName();
        if (!oldName.equalsIgnoreCase(newName.trim()) && findIngredientByName(newName).isPresent()) {
            throw new IllegalArgumentException("Інгредієнт із назвою «" + newName + "» вже існує");
        }
        existing.setName(newName);
        existing.setCaloriesPer100(newCalories);
        existing.setUnit(newUnit);

        // Оновлюємо назву інгредієнта у складі всіх рецептів, де він використовується
        if (!oldName.equals(existing.getName())) {
            for (Recipe recipe : recipes) {
                for (RecipeItem item : recipe.getItems()) {
                    if (item.getIngredientName().equalsIgnoreCase(oldName)) {
                        item.setIngredientName(existing.getName());
                        item.setIngredient(existing);
                    }
                }
            }
        }
        LOGGER.info("Оновлено інгредієнт: " + existing.getName());
    }

    public void deleteIngredient(Ingredient ingredient) {
        for (Recipe recipe : recipes) {
            for (RecipeItem item : recipe.getItems()) {
                if (item.getIngredientName().equalsIgnoreCase(ingredient.getName())) {
                    throw new IllegalStateException(
                            "Неможливо видалити інгредієнт «" + ingredient.getName()
                                    + "», оскільки він входить до рецепта «" + recipe.getName() + "»");
                }
            }
        }
        ingredients.remove(ingredient);
        LOGGER.info("Видалено інгредієнт: " + ingredient.getName());
    }

    public Optional<Ingredient> findIngredientByName(String name) {
        if (name == null) return Optional.empty();
        return ingredients.stream()
                .filter(i -> i.getName().equalsIgnoreCase(name.trim()))
                .findFirst();
    }

    // --- CRUD для рецептів ---

    public void addRecipe(Recipe recipe) {
        recipe.validate();
        linkRecipeItems(recipe);
        if (findRecipeByName(recipe.getName()).isPresent()) {
            throw new IllegalArgumentException("Рецепт із такою назвою вже існує: " + recipe.getName());
        }
        recipes.add(recipe);
        LOGGER.info("Додано рецепт: " + recipe.getName());
    }

    public void updateRecipe(int index, Recipe updatedRecipe) {
        updatedRecipe.validate();
        linkRecipeItems(updatedRecipe);

        Recipe oldRecipe = recipes.get(index);
        if (!oldRecipe.getName().equalsIgnoreCase(updatedRecipe.getName())
                && findRecipeByName(updatedRecipe.getName()).isPresent()) {
            throw new IllegalArgumentException("Рецепт із назвою «" + updatedRecipe.getName() + "» вже існує");
        }
        recipes.set(index, updatedRecipe);

        // Оновлюємо посилання у тижневому меню, якщо змінилася назва рецепта
        if (!oldRecipe.getName().equals(updatedRecipe.getName())) {
            for (MenuEntry entry : menuPlan.getEntries()) {
                if (entry.getRecipeName().equalsIgnoreCase(oldRecipe.getName())) {
                    entry.setRecipeName(updatedRecipe.getName());
                }
            }
        }
        LOGGER.info("Оновлено рецепт: " + updatedRecipe.getName());
    }

    public void deleteRecipe(Recipe recipe) {
        recipes.remove(recipe);
        menuPlan.getEntries().removeIf(e -> e.getRecipeName().equalsIgnoreCase(recipe.getName()));
        LOGGER.info("Видалено рецепт: " + recipe.getName());
    }

    public Optional<Recipe> findRecipeByName(String name) {
        if (name == null) return Optional.empty();
        return recipes.stream()
                .filter(r -> r.getName().equalsIgnoreCase(name.trim()))
                .findFirst();
    }

    // --- CRUD для записів меню ---

    public void addMenuEntry(MenuEntry entry) {
        if (findRecipeByName(entry.getRecipeName()).isEmpty()) {
            throw new IllegalArgumentException("Рецепт не знайдено у довіднику: " + entry.getRecipeName());
        }
        menuPlan.addEntry(entry);
        LOGGER.info("Додано запис у меню: " + entry.getDayOfWeek() + " — " + entry.getRecipeName());
    }

    public void updateMenuEntry(int index, MenuEntry updated) {
        if (findRecipeByName(updated.getRecipeName()).isEmpty()) {
            throw new IllegalArgumentException("Рецепт не знайдено у довіднику: " + updated.getRecipeName());
        }
        menuPlan.getEntries().set(index, updated);
        LOGGER.info("Оновлено запис у меню: " + updated.getDayOfWeek() + " — " + updated.getRecipeName());
    }

    public void deleteMenuEntry(MenuEntry entry) {
        menuPlan.removeEntry(entry);
        LOGGER.info("Видалено запис із меню: " + entry.getRecipeName());
    }

    /**
     * Повертає унікальні категорії рецептів для фільтрації.
     */
    public List<String> getCategories() {
        return recipes.stream()
                .map(Recipe::getCategory)
                .distinct()
                .sorted()
                .toList();
    }

    private void relinkAllRecipeItems() {
        for (Recipe recipe : recipes) {
            linkRecipeItems(recipe);
        }
    }

    private void linkRecipeItems(Recipe recipe) {
        for (RecipeItem item : recipe.getItems()) {
            Ingredient ing = findIngredientByName(item.getIngredientName())
                    .orElseThrow(() -> new MissingIngredientException(item.getIngredientName()));
            item.setIngredient(ing);
        }
    }
}