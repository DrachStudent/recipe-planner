package com.recipebook.controller;

import com.recipebook.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Допоміжний клас для діалогових вікон CRUD із валідацією введених даних.
 */
public final class DialogHelper {

    private static final List<String> DAYS_OF_WEEK = List.of(
            "Понеділок", "Вівторок", "Середа", "Четвер", "П'ятниця", "Субота", "Неділя"
    );
    private static final List<String> MEAL_TYPES = List.of(
            "Сніданок", "Обід", "Вечеря", "Перекус", "Десерт"
    );

    private DialogHelper() {
    }

    /**
     * Показує діалог створення або редагування інгредієнта.
     */
    public static Optional<Ingredient> showIngredientDialog(Ingredient existing) {
        Dialog<Ingredient> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Новий інгредієнт" : "Редагування інгредієнта");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(existing == null ? "" : existing.getName());
        TextField calField = new TextField(existing == null ? "" : String.valueOf(existing.getCaloriesPer100()));
        ComboBox<String> unitBox = new ComboBox<>(FXCollections.observableArrayList("г", "мл", "шт"));
        unitBox.setEditable(true);
        unitBox.setValue(existing == null ? "г" : existing.getUnit());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.addRow(0, new Label("Назва:"), nameField);
        grid.addRow(1, new Label("Ккал на 100 г/мл:"), calField);
        grid.addRow(2, new Label("Одиниця виміру:"), unitBox);
        dialog.getDialogPane().setContent(grid);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Назва інгредієнта є обов'язковою.");
                }
                double cal = Double.parseDouble(calField.getText().trim().replace(',', '.'));
                if (cal < 0) {
                    throw new IllegalArgumentException("Калорійність не може бути від'ємною.");
                }
            } catch (NumberFormatException ex) {
                showError("Помилка введення", "Калорійність має бути числом.");
                event.consume();
            } catch (IllegalArgumentException ex) {
                showError("Помилка валідації", ex.getMessage());
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                double cal = Double.parseDouble(calField.getText().trim().replace(',', '.'));
                return new Ingredient(nameField.getText().trim(), cal, unitBox.getValue());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // ШІ: Gemini – чернетка діалогового вікна створення та редагування рецепта.
    // Змінено: додано динамічне перемикання полів підкласів (MainDish, Dessert, Drink) та валідацію.
    /**
     * Показує діалог створення або редагування рецепта (MainDish, Dessert або Drink).
     */
    public static Optional<Recipe> showRecipeDialog(
            Recipe existing,
            List<Ingredient> availableIngredients,
            List<String> existingCategories
    ) {
        if (availableIngredients.isEmpty()) {
            showError("Немає інгредієнтів", "Спочатку додайте хоча б один інгредієнт у довідник.");
            return Optional.empty();
        }

        Dialog<Recipe> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Новий рецепт" : "Редагування рецепта");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> typeBox = new ComboBox<>(
                FXCollections.observableArrayList("Основна страва", "Десерт", "Напій"));
        typeBox.setValue(existing == null ? "Основна страва" : existing.getTypeLabel());

        TextField nameField = new TextField(existing == null ? "" : existing.getName());

        ComboBox<String> categoryBox = new ComboBox<>(FXCollections.observableArrayList(existingCategories));
        categoryBox.setEditable(true);
        categoryBox.setValue(existing == null ? "Другі страви" : existing.getCategory());

        TextField timeField = new TextField(existing == null ? "30" : String.valueOf(existing.getPrepTimeMinutes()));
        TextField servingsField = new TextField(existing == null ? "2" : String.valueOf(existing.getServings()));

        // Специфічні поля для підкласів
        TextField cookingMethodField = new TextField(
                existing instanceof MainDish md ? md.getCookingMethod() : "Варіння");
        CheckBox sugarCheck = new CheckBox("Містить цукор");
        sugarCheck.setSelected(!(existing instanceof Dessert ds) || ds.isContainsSugar());
        CheckBox coldCheck = new CheckBox("Подається холодним");
        coldCheck.setSelected(!(existing instanceof Drink dr) || dr.isServedCold());

        Label extraLabel = new Label("Спосіб приготування:");
        HBox extraContainer = new HBox(10);

        Runnable updateExtraControl = () -> {
            extraContainer.getChildren().clear();
            String selectedType = typeBox.getValue();
            if ("Основна страва".equals(selectedType)) {
                extraLabel.setText("Спосіб приготування:");
                extraContainer.getChildren().add(cookingMethodField);
            } else if ("Десерт".equals(selectedType)) {
                extraLabel.setText("Особливість:");
                extraContainer.getChildren().add(sugarCheck);
            } else {
                extraLabel.setText("Подача:");
                extraContainer.getChildren().add(coldCheck);
            }
        };
        typeBox.setOnAction(e -> updateExtraControl.run());
        updateExtraControl.run();

        // Склад рецепта (список інгредієнтів)
        ObservableList<RecipeItem> itemList = FXCollections.observableArrayList();
        if (existing != null) {
            for (RecipeItem item : existing.getItems()) {
                itemList.add(new RecipeItem(item.getIngredient(), item.getAmount()));
            }
        }
        ListView<RecipeItem> itemsListView = new ListView<>(itemList);
        itemsListView.setPrefHeight(130);

        ComboBox<Ingredient> ingSelector = new ComboBox<>(FXCollections.observableArrayList(availableIngredients));
        ingSelector.getSelectionModel().selectFirst();
        TextField amountField = new TextField("100");
        amountField.setPrefWidth(80);
        Button addItemBtn = new Button("Додати");
        Button removeItemBtn = new Button("Видалити");

        addItemBtn.setOnAction(e -> {
            Ingredient selectedIng = ingSelector.getValue();
            if (selectedIng == null) return;
            try {
                double amount = Double.parseDouble(amountField.getText().trim().replace(',', '.'));
                if (amount <= 0) {
                    showError("Помилка валідації", "Кількість інгредієнта має бути додатною.");
                    return;
                }
                itemList.add(new RecipeItem(selectedIng, amount));
            } catch (NumberFormatException ex) {
                showError("Помилка введення", "Кількість має бути додатним числом.");
            }
        });

        removeItemBtn.setOnAction(e -> {
            RecipeItem selected = itemsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                itemList.remove(selected);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Тип страви:"), typeBox);
        grid.addRow(1, new Label("Назва рецепта:"), nameField);
        grid.addRow(2, new Label("Категорія:"), categoryBox);
        grid.addRow(3, new Label("Час приготування (хв):"), timeField);
        grid.addRow(4, new Label("Кількість порцій:"), servingsField);
        grid.addRow(5, extraLabel, extraContainer);

        VBox content = new VBox(10,
                grid,
                new Label("Склад рецепта (інгредієнт та кількість у г/мл):"),
                new HBox(8, ingSelector, amountField, addItemBtn, removeItemBtn),
                itemsListView
        );
        content.setPadding(new Insets(12));
        content.setPrefWidth(540);
        dialog.getDialogPane().setContent(content);

        // Валідація перед закриттям діалогу
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                if (nameField.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Назва рецепта є обов'язковою.");
                }
                String cat = categoryBox.getEditor().getText().trim();
                if (cat.isEmpty()) {
                    throw new IllegalArgumentException("Категорія рецепта є обов'язковою.");
                }
                int time = Integer.parseInt(timeField.getText().trim());
                int servings = Integer.parseInt(servingsField.getText().trim());
                if (time <= 0 || servings <= 0) {
                    throw new IllegalArgumentException("Час приготування та кількість порцій мають бути додатними.");
                }
                if (itemList.isEmpty()) {
                    throw new IllegalArgumentException("Додайте хоча б один інгредієнт до складу рецепта.");
                }
            } catch (NumberFormatException ex) {
                showError("Помилка введення", "Час та кількість порцій мають бути цілими додатними числами.");
                event.consume();
            } catch (IllegalArgumentException ex) {
                showError("Помилка валідації", ex.getMessage());
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String name = nameField.getText().trim();
                String category = categoryBox.getEditor().getText().trim();
                int time = Integer.parseInt(timeField.getText().trim());
                int servings = Integer.parseInt(servingsField.getText().trim());
                List<RecipeItem> items = new ArrayList<>(itemList);

                return switch (typeBox.getValue()) {
                    case "Десерт" -> new Dessert(name, category, time, servings, items, sugarCheck.isSelected());
                    case "Напій" -> new Drink(name, category, time, servings, items, coldCheck.isSelected());
                    default -> new MainDish(name, category, time, servings, items, cookingMethodField.getText().trim());
                };
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Показує діалог додавання або редагування запису тижневого меню.
     */
    public static Optional<MenuEntry> showMenuEntryDialog(MenuEntry existing, List<Recipe> recipes) {
        if (recipes.isEmpty()) {
            showError("Немає рецептів", "Спочатку додайте хоча б один рецепт у довідник.");
            return Optional.empty();
        }

        Dialog<MenuEntry> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Новий запис у меню" : "Редагування запису меню");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> dayBox = new ComboBox<>(FXCollections.observableArrayList(DAYS_OF_WEEK));
        dayBox.setValue(existing == null ? DAYS_OF_WEEK.get(0) : existing.getDayOfWeek());

        ComboBox<String> mealBox = new ComboBox<>(FXCollections.observableArrayList(MEAL_TYPES));
        mealBox.setEditable(true);
        mealBox.setValue(existing == null ? MEAL_TYPES.get(0) : existing.getMealType());

        List<String> recipeNames = recipes.stream().map(Recipe::getName).sorted().toList();
        ComboBox<String> recipeBox = new ComboBox<>(FXCollections.observableArrayList(recipeNames));
        recipeBox.setValue(existing == null ? recipeNames.get(0) : existing.getRecipeName());
        recipeBox.setPrefWidth(280);

        TextField servingsField = new TextField(existing == null ? "2" : String.valueOf(existing.getServings()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.addRow(0, new Label("День тижня:"), dayBox);
        grid.addRow(1, new Label("Прийом їжі:"), mealBox);
        grid.addRow(2, new Label("Рецепт:"), recipeBox);
        grid.addRow(3, new Label("Кількість порцій:"), servingsField);
        dialog.getDialogPane().setContent(grid);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                if (recipeBox.getValue() == null || recipeBox.getValue().trim().isEmpty()) {
                    throw new IllegalArgumentException("Оберіть рецепт.");
                }
                int servings = Integer.parseInt(servingsField.getText().trim());
                if (servings <= 0) {
                    throw new IllegalArgumentException("Кількість порцій має бути додатною.");
                }
            } catch (NumberFormatException ex) {
                showError("Помилка введення", "Кількість порцій має бути цілим додатним числом.");
                event.consume();
            } catch (IllegalArgumentException ex) {
                showError("Помилка валідації", ex.getMessage());
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                int servings = Integer.parseInt(servingsField.getText().trim());
                return new MenuEntry(dayBox.getValue(), mealBox.getEditor().getText().trim(), recipeBox.getValue(), servings);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}