package com.recipebook.service;

import java.util.Map;

/**
 * Результати порівняння однопотокового та багатопотокового розрахунку калорійності.
 */
public record BenchmarkResult(
        int recipeCount,
        int threadCount,
        double singleThreadMs,
        double multiThreadMs,
        double speedup,
        double efficiency,
        Map<String, Double> caloriesByRecipe
) {
    /**
     * Формує текстовий підсумок для відображення в інтерфейсі та журналі.
     */
    public String toSummary() {
        return String.format(
                "Рецептів: %d | Потоків (n): %d | T1 (1 потік): %.2f мс | Tn (%d потоків): %.2f мс | Прискорення S = %.2f | Ефективність E = %.2f",
                recipeCount, threadCount, singleThreadMs, threadCount, multiThreadMs, speedup, efficiency
        );
    }
}