package com.recipebook.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Налаштування журналювання подій і помилок застосунку у файл засобами java.util.logging.
 */
public final class AppLogger {
    private static final Logger LOGGER = Logger.getLogger("RecipePlannerLogger");
    private static boolean initialized = false;

    private AppLogger() {
    }

    /**
     * Повертає налаштований екземпляр логера із записом у logs/recipe_planner.log.
     */
    public static synchronized Logger getLogger() {
        if (!initialized) {
            try {
                Files.createDirectories(Path.of("logs"));
                FileHandler fileHandler = new FileHandler("logs/recipe_planner.log", true);
                fileHandler.setEncoding("UTF-8");
                fileHandler.setFormatter(new SimpleFormatter());
                LOGGER.addHandler(fileHandler);
                LOGGER.setLevel(Level.ALL);
                initialized = true;
            } catch (IOException e) {
                System.err.println("Не вдалося створити файл журналу: " + e.getMessage());
            }
        }
        return LOGGER;
    }
}