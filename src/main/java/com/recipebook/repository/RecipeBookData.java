package com.recipebook.repository;

import com.recipebook.model.Ingredient;
import com.recipebook.model.MenuPlan;
import com.recipebook.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Контейнер для збереження та завантаження всіх даних довідника у форматі JSON.
 */
public class RecipeBookData {
    private List<Ingredient> ingredients = new ArrayList<>();
    private List<Recipe> recipes = new ArrayList<>();
    private MenuPlan menuPlan = new MenuPlan();

    public RecipeBookData() {
    }

    public RecipeBookData(List<Ingredient> ingredients, List<Recipe> recipes, MenuPlan menuPlan) {
        this.ingredients = (ingredients == null) ? new ArrayList<>() : new ArrayList<>(ingredients);
        this.recipes = (recipes == null) ? new ArrayList<>() : new ArrayList<>(recipes);
        this.menuPlan = (menuPlan == null) ? new MenuPlan() : menuPlan;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = (ingredients == null) ? new ArrayList<>() : ingredients;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = (recipes == null) ? new ArrayList<>() : recipes;
    }

    public MenuPlan getMenuPlan() {
        return menuPlan;
    }

    public void setMenuPlan(MenuPlan menuPlan) {
        this.menuPlan = (menuPlan == null) ? new MenuPlan() : menuPlan;
    }
}