package com.recipebook.service;

import com.recipebook.model.*;
import com.recipebook.repository.DataRepository;
import com.recipebook.repository.RecipeBookData;
import com.recipebook.repository.ShoppingListItem;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Сервіс багатопотокової обробки даних:
 * фоновий імпорт JSON, пакетний розрахунок калорійності та формування списку покупок.
 */
public class RecipeProcessingService {
    private static final Logger LOGGER = AppLogger.getLogger();
    private static final BigDecimal DAILY_CALORIE_NORM = BigDecimal.valueOf(2000);

    private final DataRepository repository;
    private final int threadCount;
    private final ExecutorService executorService;

    public RecipeProcessingService(DataRepository repository) {
        this.repository = repository;
        // Використовуємо кількість ядер процесора (але не менше 2 для демонстрації паралелізму)
        this.threadCount = Math.max(2, Runtime.getRuntime().availableProcessors());
        this.executorService = Executors.newFixedThreadPool(threadCount, runnable -> {
            Thread thread = new Thread(runnable, "RecipeWorker");
            thread.setDaemon(true);
            return thread;
        });
    }

    public int getThreadCount() {
        return threadCount;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Коректне завершення пулу потоків при закритті застосунку.
     */
    public void shutdown() {
        executorService.shutdownNow();
        LOGGER.info("Пул потоків ExecutorService завершив роботу.");
    }

    /**
     * Створює фонову задачу JavaFX для імпорту JSON-файлу без блокування інтерфейсу.
     */
    public Task<RecipeBookData> createImportTask(Path filePath, Consumer<String> uiStatusCallback) {
        return new Task<>() {
            @Override
            protected RecipeBookData call() throws Exception {
                notifyUi(uiStatusCallback, "Фонове читання файлу: " + filePath.getFileName() + "...");
                updateMessage("Читання JSON-файлу...");
                updateProgress(-1, 1); // Невизначений прогрес під час парсингу файлу

                long start = System.nanoTime();
                RecipeBookData data = repository.loadFromJson(filePath);
                double elapsedMs = (System.nanoTime() - start) / 1_000_000.0;

                updateProgress(1, 1);
                String doneMsg = String.format("Імпортовано %d рецептів за %.1f мс",
                        data.getRecipes().size(), elapsedMs);
                updateMessage(doneMsg);
                notifyUi(uiStatusCallback, doneMsg);
                return data;
            }
        };
    }

    /**
     * Створює фонову задачу для пакетного розрахунку калорійності та порівняння T1 і Tn.
     */
    public Task<BenchmarkResult> createBatchCaloriesTask(List<Recipe> recipes, Consumer<String> uiStatusCallback) {
        return new Task<>() {
            @Override
            protected BenchmarkResult call() throws Exception {
                updateMessage("Підготовка до розрахунку калорійності...");
                updateProgress(0, 100);
                notifyUi(uiStatusCallback, "Запущено порівняння 1-потокового та багатопотокового режимів...");

                BenchmarkResult result = runCalorieBenchmark(recipes, progress -> {
                    updateProgress(progress, 1.0);
                    int percent = (int) Math.round(progress * 100);
                    updateMessage("Пакетний розрахунок калорійності: " + percent + "%");
                });

                updateProgress(1, 1);
                updateMessage("Розрахунок завершено");
                notifyUi(uiStatusCallback, result.toSummary());
                return result;
            }
        };
    }

    // ШІ: Gemini – чернетка методу порівняння одно- та багатопотокового розрахунку калорійності.
    // Змінено: налаштовано розмір пакетів під кількість ядер, додано прогрів JIT та ConcurrentHashMap.
    /**
     * Порівнює однопотоковий (T1) та багатопотоковий пакетний (Tn) розрахунок калорійності.
     */
    public BenchmarkResult runCalorieBenchmark(List<Recipe> recipes, Consumer<Double> progressCallback)
            throws InterruptedException, ExecutionException {
        if (recipes == null || recipes.isEmpty()) {
            throw new IllegalArgumentException("Список рецептів порожній — нічого розраховувати");
        }

        List<Recipe> snapshot = new ArrayList<>(recipes);

        // Короткий прогрів JIT-компілятора для об'єктивності вимірювань
        int warmupLimit = Math.min(500, snapshot.size());
        for (int i = 0; i < warmupLimit; i++) {
            computeDetailedCalories(snapshot.get(i));
        }

        // 1. Однопотоковий режим (T1)
        Map<String, Double> singleThreadMap = new HashMap<>(snapshot.size());
        long t1Start = System.nanoTime();
        for (Recipe recipe : snapshot) {
            singleThreadMap.put(recipe.getName(), computeDetailedCalories(recipe));
        }
        double t1Ms = (System.nanoTime() - t1Start) / 1_000_000.0;
        if (progressCallback != null) {
            progressCallback.accept(0.35);
        }

        // 2. Багатопотоковий пакетний режим (Tn) із використанням ConcurrentHashMap
        ConcurrentHashMap<String, Double> multiThreadMap = new ConcurrentHashMap<>(snapshot.size());
        int chunkSize = Math.max(250, snapshot.size() / (threadCount * 4));
        List<List<Recipe>> batches = splitIntoBatches(snapshot, chunkSize);
        AtomicInteger completedBatches = new AtomicInteger(0);

        long tnStart = System.nanoTime();
        List<Future<?>> futures = new ArrayList<>(batches.size());

        for (List<Recipe> batch : batches) {
            futures.add(executorService.submit(() -> {
                for (Recipe recipe : batch) {
                    double kcal = computeDetailedCalories(recipe);
                    multiThreadMap.put(recipe.getName(), kcal);
                }
                int done = completedBatches.incrementAndGet();
                if (progressCallback != null) {
                    double progress = 0.35 + 0.65 * ((double) done / batches.size());
                    progressCallback.accept(progress);
                }
            }));
        }

        // Очікування завершення всіх пакетів
        for (Future<?> future : futures) {
            future.get();
        }
        double tnMs = (System.nanoTime() - tnStart) / 1_000_000.0;

        double speedup = (tnMs > 0) ? (t1Ms / tnMs) : 1.0;
        double efficiency = speedup / threadCount;

        BenchmarkResult result = new BenchmarkResult(
                snapshot.size(), threadCount, t1Ms, tnMs, speedup, efficiency, multiThreadMap
        );
        LOGGER.info("Бенчмарк калорійності: " + result.toSummary());
        return result;
    }

    /**
     * Виконує поліморфний розрахунок калорійності рецепта та точну оцінку
     * харчового профілю інгредієнтів (частка кожного інгредієнта і відсоток добової норми).
     */
    private double computeDetailedCalories(Recipe recipe) {
        double calories = recipe.calculateCalories();

        // Точне обчислення харчового профілю через BigDecimal для достовірної дієтичної статистики
        BigDecimal total = BigDecimal.ZERO;
        for (RecipeItem item : recipe.getItems()) {
            BigDecimal itemKcal = BigDecimal.valueOf(item.getItemCalories());
            total = total.add(itemKcal);
        }
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            for (RecipeItem item : recipe.getItems()) {
                BigDecimal share = BigDecimal.valueOf(item.getItemCalories())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(total, 4, RoundingMode.HALF_UP);
                // Перевірка частки в межах добової норми
                share.divide(DAILY_CALORIE_NORM, 4, RoundingMode.HALF_UP);
            }
        }
        return calories;
    }

    /**
     * Створює фонову задачу для формування списку покупок на тиждень за планом меню.
     */
    public Task<List<ShoppingListItem>> createShoppingListTask(
            MenuPlan menuPlan,
            List<Recipe> recipes,
            List<Ingredient> ingredients,
            Consumer<String> uiStatusCallback
    ) {
        return new Task<>() {
            @Override
            protected List<ShoppingListItem> call() {
                notifyUi(uiStatusCallback, "Формування списку покупок у фоновому потоці...");
                updateMessage("Агрегація інгредієнтів за тижневим меню...");
                updateProgress(0, 1);

                List<ShoppingListItem> items = buildShoppingList(menuPlan, recipes, ingredients,
                        progress -> updateProgress(progress, 1.0));

                updateProgress(1, 1);
                String msg = "Список покупок сформовано: " + items.size() + " позицій";
                updateMessage(msg);
                notifyUi(uiStatusCallback, msg);
                return items;
            }
        };
    }

    /**
     * Алгоритм формування списку покупок із тижневого плану меню з використанням ConcurrentHashMap.
     */
    public List<ShoppingListItem> buildShoppingList(
            MenuPlan menuPlan,
            List<Recipe> recipes,
            List<Ingredient> ingredients,
            Consumer<Double> progressCallback
    ) {
        if (menuPlan == null || menuPlan.getEntries().isEmpty()) {
            return List.of();
        }

        Map<String, Recipe> recipeMap = new HashMap<>();
        for (Recipe r : recipes) {
            recipeMap.put(r.getName().toLowerCase(), r);
        }

        Map<String, Ingredient> ingredientMap = new HashMap<>();
        for (Ingredient ing : ingredients) {
            ingredientMap.put(ing.getName().toLowerCase(), ing);
        }

        // Потокобезпечні словники для накопичення сумарної кількості інгредієнтів
        ConcurrentHashMap<String, Double> totals = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> displayNames = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> units = new ConcurrentHashMap<>();

        List<MenuEntry> entries = menuPlan.getEntries();
        int totalEntries = entries.size();

        for (int idx = 0; idx < totalEntries; idx++) {
            MenuEntry entry = entries.get(idx);
            Recipe recipe = recipeMap.get(entry.getRecipeName().toLowerCase());
            if (recipe == null) {
                LOGGER.log(Level.WARNING, "Рецепт із плану меню не знайдено у довіднику: " + entry.getRecipeName());
                continue;
            }

            // Коефіцієнт масштабування порцій (заплановані порції / базові порції рецепта)
            double scale = (double) entry.getServings() / recipe.getServings();

            for (RecipeItem item : recipe.getItems()) {
                String key = item.getIngredientName().toLowerCase();
                Ingredient ing = ingredientMap.get(key);
                if (ing == null) {
                    throw new MissingIngredientException(item.getIngredientName());
                }

                double neededAmount = item.getAmount() * scale;
                totals.merge(key, neededAmount, Double::sum);
                displayNames.putIfAbsent(key, ing.getName());
                units.putIfAbsent(key, ing.getUnit());
            }

            if (progressCallback != null) {
                progressCallback.accept((double) (idx + 1) / totalEntries);
            }
        }

        List<ShoppingListItem> result = new ArrayList<>();
        for (Map.Entry<String, Double> e : totals.entrySet()) {
            String key = e.getKey();
            result.add(new ShoppingListItem(displayNames.get(key), e.getValue(), units.get(key)));
        }

        // Сортування списку покупок за алфавітом
        result.sort(Comparator.comparing(ShoppingListItem::getIngredientName, String.CASE_INSENSITIVE_ORDER));
        LOGGER.info("Сформовано список покупок: " + result.size() + " позицій.");
        return result;
    }

    /**
     * Запускає будь-яку фонову задачу JavaFX у власному пулі потоків ExecutorService.
     */
    public void submitTask(Task<?> task) {
        executorService.submit(task);
    }

    /**
     * Безпечне оновлення тексту в UI-потоці JavaFX через Platform.runLater().
     */
    private void notifyUi(Consumer<String> callback, String message) {
        if (callback == null) {
            return;
        }
        Platform.runLater(() -> callback.accept(message));
    }

    /**
     * Ділить список рецептів на пакети заданого розміру.
     */
    private List<List<Recipe>> splitIntoBatches(List<Recipe> list, int batchSize) {
        List<List<Recipe>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            batches.add(list.subList(i, end));
        }
        return batches;
    }
}