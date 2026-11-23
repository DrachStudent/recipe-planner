package com.recipebook.controller;

import com.recipebook.model.*;
import com.recipebook.repository.*;
import com.recipebook.service.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Головний контролер MVC, що зв'язує інтерфейс JavaFX із сервісами та репозиторієм.
 */
public class MainController {
    private static final Logger LOGGER = AppLogger.getLogger();
    private static final String ALL_CATEGORIES = "Усі категорії";

    // --- Елементи вкладки «Рецепти» ---
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> sortCombo;
    @FXML private TableView<Recipe> recipeTable;
    @FXML private TableColumn<Recipe, String> colRecipeName;
    @FXML private TableColumn<Recipe, String> colRecipeType;
    @FXML private TableColumn<Recipe, String> colRecipeCategory;
    @FXML private TableColumn<Recipe, Integer> colRecipeTime;
    @FXML private TableColumn<Recipe, Integer> colRecipeServings;
    @FXML private TableColumn<Recipe, Double> colRecipeCalories;
    @FXML private TableColumn<Recipe, String> colRecipeUnit;
    @FXML private TextArea recipeDetailsArea;
    @FXML private Label recipeCountLabel;

    // --- Елементи вкладки «Інгредієнти» ---
    @FXML private TableView<Ingredient> ingredientTable;
    @FXML private TableColumn<Ingredient, String> colIngName;
    @FXML private TableColumn<Ingredient, Double> colIngCalories;
    @FXML private TableColumn<Ingredient, String> colIngUnit;

    // --- Елементи вкладки «Меню та список покупок» ---
    @FXML private TableView<MenuEntry> menuTable;
    @FXML private TableColumn<MenuEntry, String> colMenuDay;
    @FXML private TableColumn<MenuEntry, String> colMenuMeal;
    @FXML private TableColumn<MenuEntry, String> colMenuRecipe;
    @FXML private TableColumn<MenuEntry, Integer> colMenuServings;

    @FXML private TableView<ShoppingListItem> shoppingTable;
    @FXML private TableColumn<ShoppingListItem, String> colShopName;
    @FXML private TableColumn<ShoppingListItem, Double> colShopAmount;
    @FXML private TableColumn<ShoppingListItem, String> colShopUnit;

    // --- Нижня панель статусу ---
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;

    // --- Сервіси та спостережувані списки ---
    private final DataRepository dataRepository = new FileDataRepository();
    private final RecipeBookService bookService = new RecipeBookService();
    private final RecipeProcessingService processingService = new RecipeProcessingService(dataRepository);

    private final ObservableList<Recipe> recipeObservable = FXCollections.observableArrayList();
    private final ObservableList<Ingredient> ingredientObservable = FXCollections.observableArrayList();
    private final ObservableList<MenuEntry> menuObservable = FXCollections.observableArrayList();
    private final ObservableList<ShoppingListItem> shoppingObservable = FXCollections.observableArrayList();

    private FilteredList<Recipe> filteredRecipes;
    private SortedList<Recipe> sortedRecipes;

    @FXML
    public void initialize() {
        setupRecipeTable();
        setupIngredientTable();
        setupMenuAndShoppingTables();
        setupFiltersAndSorting();
        loadInitialDataset();
    }

    /**
     * Завершує фоновий пул потоків при закритті вікна.
     */
    public void shutdown() {
        processingService.shutdown();
    }

    private void setupRecipeTable() {
        colRecipeName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colRecipeType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTypeLabel()));
        colRecipeCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colRecipeTime.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrepTimeMinutes()));
        colRecipeServings.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getServings()));
        colRecipeCalories.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().calculateCalories()));
        colRecipeUnit.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCalorieUnit()));

        filteredRecipes = new FilteredList<>(recipeObservable, r -> true);
        sortedRecipes = new SortedList<>(filteredRecipes);
        sortedRecipes.comparatorProperty().bind(recipeTable.comparatorProperty());
        recipeTable.setItems(sortedRecipes);

        recipeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) ->
                showRecipeDetails(selected));
    }

    private void setupIngredientTable() {
        colIngName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colIngCalories.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCaloriesPer100()));
        colIngUnit.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUnit()));
        ingredientTable.setItems(ingredientObservable);
    }

    private void setupMenuAndShoppingTables() {
        colMenuDay.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDayOfWeek()));
        colMenuMeal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMealType()));
        colMenuRecipe.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRecipeName()));
        colMenuServings.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getServings()));
        menuTable.setItems(menuObservable);

        colShopName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIngredientName()));
        colShopAmount.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getTotalAmount()));
        colShopUnit.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUnit()));
        shoppingTable.setItems(shoppingObservable);
    }

    private void setupFiltersAndSorting() {
        sortCombo.setItems(FXCollections.observableArrayList(
                "За замовчуванням",
                "Час (за зростанням)",
                "Час (за спаданням)",
                "Калорійність (за зростанням)",
                "Калорійність (за спаданням)"
        ));
        sortCombo.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        categoryFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        sortCombo.valueProperty().addListener((obs, o, n) -> applySortPreset(n));
    }

    private void loadInitialDataset() {
        Path defaultPath = Path.of("data/recipes.json");
        try {
            RecipeBookData data;
            if (Files.exists(defaultPath)) {
                data = dataRepository.loadFromJson(defaultPath);
            } else {
                data = SampleDataGenerator.createStandardDataset();
                dataRepository.saveToJson(defaultPath, data);
            }
            applyLoadedData(data, "Завантажено початковий довідник (" + data.getRecipes().size() + " рецептів)");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Помилка читання стартового файлу, створюється новий набір", e);
            RecipeBookData fallback = SampleDataGenerator.createStandardDataset();
            applyLoadedData(fallback, "Створено стандартний набір (50 рецептів)");
        }
    }

    private void applyLoadedData(RecipeBookData data, String statusText) {
        bookService.loadData(data);
        refreshAllViews();
        setStatus(statusText);
        onBuildShoppingList();
    }

    private void refreshAllViews() {
        ingredientObservable.setAll(bookService.getIngredients());
        recipeObservable.setAll(bookService.getRecipes());
        menuObservable.setAll(bookService.getMenuPlan().getEntries());
        refreshCategoryFilter();
        applyFilters();
        recipeTable.refresh();
    }

    private void refreshCategoryFilter() {
        String current = categoryFilter.getValue();
        ObservableList<String> categories = FXCollections.observableArrayList();
        categories.add(ALL_CATEGORIES);
        categories.addAll(bookService.getCategories());
        categoryFilter.setItems(categories);
        if (current != null && categories.contains(current)) {
            categoryFilter.setValue(current);
        } else {
            categoryFilter.setValue(ALL_CATEGORIES);
        }
    }

    private void applyFilters() {
        String query = (searchField.getText() == null) ? "" : searchField.getText().trim().toLowerCase();
        String selectedCategory = categoryFilter.getValue();

        filteredRecipes.setPredicate(recipe -> {
            // Фільтр за категорією
            if (selectedCategory != null && !ALL_CATEGORIES.equals(selectedCategory)) {
                if (!recipe.getCategory().equalsIgnoreCase(selectedCategory)) {
                    return false;
                }
            }
            // Пошук за назвою рецепта АБО за назвою будь-якого інгредієнта у складі
            if (!query.isEmpty()) {
                boolean nameMatch = recipe.getName().toLowerCase().contains(query);
                boolean ingredientMatch = recipe.getItems().stream()
                        .anyMatch(item -> item.getIngredientName().toLowerCase().contains(query));
                return nameMatch || ingredientMatch;
            }
            return true;
        });
        recipeCountLabel.setText("Показано рецептів: " + filteredRecipes.size() + " із " + recipeObservable.size());
    }

    private void applySortPreset(String preset) {
        if (preset == null) return;
        recipeTable.getSortOrder().clear();
        switch (preset) {
            case "Час (за зростанням)" -> {
                colRecipeTime.setSortType(TableColumn.SortType.ASCENDING);
                recipeTable.getSortOrder().add(colRecipeTime);
            }
            case "Час (за спаданням)" -> {
                colRecipeTime.setSortType(TableColumn.SortType.DESCENDING);
                recipeTable.getSortOrder().add(colRecipeTime);
            }
            case "Калорійність (за зростанням)" -> {
                colRecipeCalories.setSortType(TableColumn.SortType.ASCENDING);
                recipeTable.getSortOrder().add(colRecipeCalories);
            }
            case "Калорійність (за спаданням)" -> {
                colRecipeCalories.setSortType(TableColumn.SortType.DESCENDING);
                recipeTable.getSortOrder().add(colRecipeCalories);
            }
            default -> {
                // Без примусового сортування
            }
        }
        recipeTable.sort();
    }

    private void showRecipeDetails(Recipe recipe) {
        if (recipe == null) {
            recipeDetailsArea.clear();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Назва: ").append(recipe.getName()).append("\n");
        sb.append("Тип: ").append(recipe.getTypeLabel()).append("\n");
        sb.append("Категорія: ").append(recipe.getCategory()).append("\n");
        sb.append("Час приготування: ").append(recipe.getPrepTimeMinutes()).append(" хв\n");
        sb.append("Порцій: ").append(recipe.getServings()).append("\n");
        sb.append("Калорійність: ").append(recipe.getFormattedCalories()).append("\n");

        if (recipe instanceof MainDish md) {
            sb.append("Спосіб приготування: ").append(md.getCookingMethod()).append("\n");
        } else if (recipe instanceof Dessert ds) {
            sb.append("Містить цукор: ").append(ds.isContainsSugar() ? "Так" : "Ні").append("\n");
        } else if (recipe instanceof Drink dr) {
            sb.append("Подача: ").append(dr.isServedCold() ? "Холодний" : "Гарячий").append("\n");
        }

        sb.append("\nСклад рецепта:\n");
        for (RecipeItem item : recipe.getItems()) {
            sb.append(" • ").append(item).append("\n");
        }
        recipeDetailsArea.setText(sb.toString());
    }

    @FXML
    private void onResetFilters() {
        searchField.clear();
        categoryFilter.setValue(ALL_CATEGORIES);
        sortCombo.getSelectionModel().selectFirst();
    }

    // ==================== ІМПОРТ / ЕКСПОРТ / БАГАТОПОТОЧНІСТЬ ====================

    @FXML
    private void onImportJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Імпорт довідника з JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON-файли", "*.json"));
        setInitialDirectory(chooser);

        File file = chooser.showOpenDialog(getWindow());
        if (file == null) return;
        runAsyncImport(file.toPath());
    }

    @FXML
    private void onExportJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Експорт довідника у JSON");
        chooser.setInitialFileName("recipes_export.json");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON-файли", "*.json"));
        setInitialDirectory(chooser);

        File file = chooser.showSaveDialog(getWindow());
        if (file == null) return;

        try {
            dataRepository.saveToJson(file.toPath(), bookService.exportData());
            setStatus("Довідник успішно збережено у " + file.getName());
            DialogHelper.showInfo("Експорт JSON", null, "Файл збережено:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Помилка експорту JSON", e);
            DialogHelper.showError("Помилка експорту", e.getMessage());
        }
    }

    @FXML
    private void onLoadStandard() {
        RecipeBookData data = SampleDataGenerator.createStandardDataset();
        applyLoadedData(data, "Завантажено стандартну базу: 50 рецептів");
    }

    @FXML
    private void onLoadLargeAsync() {
        Path largePath = Path.of("data/recipes_large.json");
        if (!Files.exists(largePath)) {
            try {
                RecipeBookData large = SampleDataGenerator.createLargeDataset(15_000);
                dataRepository.saveToJson(largePath, large);
            } catch (Exception e) {
                DialogHelper.showError("Помилка генерації", e.getMessage());
                return;
            }
        }
        runAsyncImport(largePath);
    }

    private void runAsyncImport(Path filePath) {
        Task<RecipeBookData> task = processingService.createImportTask(filePath, this::setStatus);
        bindProgress(task);

        task.setOnSucceeded(e -> {
            RecipeBookData data = task.getValue();
            applyLoadedData(data, "Фоновий імпорт завершено: " + data.getRecipes().size() + " рецептів");
            unbindProgress(1.0);
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.SEVERE, "Помилка фонового імпорту JSON", ex);
            unbindProgress(0.0);
            setStatus("Помилка імпорту файлу");
            DialogHelper.showError("Помилка імпорту JSON",
                    ex != null ? ex.getMessage() : "Невідома помилка читання файлу");
        });

        processingService.submitTask(task);
    }

    @FXML
    private void onRunBenchmark() {
        List<Recipe> currentRecipes = bookService.getRecipes();
        if (currentRecipes.isEmpty()) {
            DialogHelper.showError("Порожній список", "Немає рецептів для розрахунку.");
            return;
        }

        Task<BenchmarkResult> task = processingService.createBatchCaloriesTask(currentRecipes, this::setStatus);
        bindProgress(task);

        task.setOnSucceeded(e -> {
            BenchmarkResult res = task.getValue();
            unbindProgress(1.0);
            setStatus(res.toSummary());
            DialogHelper.showInfo(
                    "Результати багатопотокового розрахунку",
                    "Порівняння 1-потокового та пакетного режимів",
                    String.format(
                            "Кількість рецептів: %d\nКількість потоків (n): %d\n\n"
                                    + "Однопотоковий час (T1): %.2f мс\n"
                                    + "Багатопотоковий час (Tn): %.2f мс\n\n"
                                    + "Прискорення S = T1 / Tn = %.2f\n"
                                    + "Ефективність E = S / n = %.2f",
                            res.recipeCount(), res.threadCount(),
                            res.singleThreadMs(), res.multiThreadMs(),
                            res.speedup(), res.efficiency()
                    )
            );
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.SEVERE, "Помилка багатопотокового розрахунку", ex);
            unbindProgress(0.0);
            DialogHelper.showError("Помилка розрахунку", ex != null ? ex.getMessage() : "Помилка виконання");
        });

        processingService.submitTask(task);
    }

    // ==================== CRUD: РЕЦЕПТИ ====================

    @FXML
    private void onAddRecipe() {
        Optional<Recipe> created = DialogHelper.showRecipeDialog(
                null, bookService.getIngredients(), bookService.getCategories());
        created.ifPresent(recipe -> {
            try {
                bookService.addRecipe(recipe);
                refreshAllViews();
                setStatus("Додано рецепт: " + recipe.getName());
            } catch (Exception e) {
                DialogHelper.showError("Не вдалося додати рецепт", e.getMessage());
            }
        });
    }

    @FXML
    private void onEditRecipe() {
        Recipe selected = recipeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Рецепт не обрано", "Оберіть рецепт у таблиці для редагування.");
            return;
        }
        int index = bookService.getRecipes().indexOf(selected);
        Optional<Recipe> edited = DialogHelper.showRecipeDialog(
                selected, bookService.getIngredients(), bookService.getCategories());
        edited.ifPresent(updated -> {
            try {
                bookService.updateRecipe(index, updated);
                refreshAllViews();
                setStatus("Оновлено рецепт: " + updated.getName());
            } catch (Exception e) {
                DialogHelper.showError("Не вдалося оновити рецепт", e.getMessage());
            }
        });
    }

    @FXML
    private void onDeleteRecipe() {
        Recipe selected = recipeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Рецепт не обрано", "Оберіть рецепт для видалення.");
            return;
        }
        bookService.deleteRecipe(selected);
        refreshAllViews();
        recipeDetailsArea.clear();
        setStatus("Видалено рецепт: " + selected.getName());
    }

    // ==================== CRUD: ІНГРЕДІЄНТИ ====================

    @FXML
    private void onAddIngredient() {
        DialogHelper.showIngredientDialog(null).ifPresent(ing -> {
            try {
                bookService.addIngredient(ing);
                refreshAllViews();
                setStatus("Додано інгредієнт: " + ing.getName());
            } catch (Exception e) {
                DialogHelper.showError("Не вдалося додати інгредієнт", e.getMessage());
            }
        });
    }

    @FXML
    private void onEditIngredient() {
        Ingredient selected = ingredientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Інгредієнт не обрано", "Оберіть інгредієнт у таблиці.");
            return;
        }
        DialogHelper.showIngredientDialog(selected).ifPresent(updated -> {
            try {
                bookService.updateIngredient(selected, updated.getName(), updated.getCaloriesPer100(), updated.getUnit());
                refreshAllViews();
                setStatus("Оновлено інгредієнт: " + selected.getName());
            } catch (Exception e) {
                DialogHelper.showError("Не вдалося оновити інгредієнт", e.getMessage());
            }
        });
    }

    @FXML
    private void onDeleteIngredient() {
        Ingredient selected = ingredientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Інгредієнт не обрано", "Оберіть інгредієнт для видалення.");
            return;
        }
        try {
            bookService.deleteIngredient(selected);
            refreshAllViews();
            setStatus("Видалено інгредієнт: " + selected.getName());
        } catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            DialogHelper.showError("Захист цілісності даних", e.getMessage());
        }
    }

    // ==================== CRUD: МЕНЮ ТА СПИСОК ПОКУПОК ====================

    @FXML
    private void onAddMenuEntry() {
        DialogHelper.showMenuEntryDialog(null, bookService.getRecipes()).ifPresent(entry -> {
            try {
                bookService.addMenuEntry(entry);
                refreshAllViews();
                setStatus("Додано запис у меню: " + entry.getRecipeName());
            } catch (Exception e) {
                DialogHelper.showError("Помилка додавання", e.getMessage());
            }
        });
    }

    @FXML
    private void onEditMenuEntry() {
        MenuEntry selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Запис не обрано", "Оберіть запис у таблиці меню.");
            return;
        }
        int index = bookService.getMenuPlan().getEntries().indexOf(selected);
        DialogHelper.showMenuEntryDialog(selected, bookService.getRecipes()).ifPresent(updated -> {
            try {
                bookService.updateMenuEntry(index, updated);
                refreshAllViews();
                setStatus("Оновлено запис меню: " + updated.getRecipeName());
            } catch (Exception e) {
                DialogHelper.showError("Помилка редагування", e.getMessage());
            }
        });
    }

    @FXML
    private void onDeleteMenuEntry() {
        MenuEntry selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Запис не обрано", "Оберіть запис для видалення.");
            return;
        }
        bookService.deleteMenuEntry(selected);
        refreshAllViews();
        setStatus("Видалено запис із меню: " + selected.getRecipeName());
    }

    @FXML
    private void onBuildShoppingList() {
        Task<List<ShoppingListItem>> task = processingService.createShoppingListTask(
                bookService.getMenuPlan(),
                bookService.getRecipes(),
                bookService.getIngredients(),
                this::setStatus
        );
        bindProgress(task);

        task.setOnSucceeded(e -> {
            shoppingObservable.setAll(task.getValue());
            unbindProgress(1.0);
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.SEVERE, "Помилка формування списку покупок", ex);
            unbindProgress(0.0);
            DialogHelper.showError("Помилка списку покупок", ex != null ? ex.getMessage() : "Невідома помилка");
        });

        processingService.submitTask(task);
    }

    @FXML
    private void onExportShoppingXml() {
        if (shoppingObservable.isEmpty()) {
            DialogHelper.showError("Список порожній", "Спочатку сформуйте список покупок.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Експорт списку покупок у XML (JAXB)");
        chooser.setInitialFileName("shopping_list.xml");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML-файли", "*.xml"));
        setInitialDirectory(chooser);

        File file = chooser.showSaveDialog(getWindow());
        if (file == null) return;

        try {
            ShoppingListXml xml = new ShoppingListXml(shoppingObservable);
            dataRepository.exportShoppingListToXml(file.toPath(), xml);
            setStatus("Список покупок експортовано у XML: " + file.getName());
            DialogHelper.showInfo("Експорт XML", null, "Список покупок збережено у файл:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Помилка експорту XML", e);
            DialogHelper.showError("Помилка експорту XML", e.getMessage());
        }
    }

    // ==================== ДОПОМІЖНІ МЕТОДИ UI ====================

    private void bindProgress(Task<?> task) {
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(task.progressProperty());
    }

    private void unbindProgress(double finalValue) {
        progressBar.progressProperty().unbind();
        progressBar.setProgress(finalValue);
    }

    private void setStatus(String text) {
        if (Platform.isFxApplicationThread()) {
            statusLabel.setText(text);
        } else {
            Platform.runLater(() -> statusLabel.setText(text));
        }
    }

    private Window getWindow() {
        return recipeTable.getScene() != null ? recipeTable.getScene().getWindow() : null;
    }

    private void setInitialDirectory(FileChooser chooser) {
        File dataDir = new File("data");
        if (dataDir.exists() && dataDir.isDirectory()) {
            chooser.setInitialDirectory(dataDir);
        }
    }
}