package com.recipebook.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.recipebook.model.Ingredient;
import com.recipebook.model.MissingIngredientException;
import com.recipebook.model.Recipe;
import com.recipebook.model.RecipeItem;
import com.recipebook.service.AppLogger;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Реалізація репозиторію для роботи з JSON (Jackson) та XML (JAXB).
 */
public class FileDataRepository implements DataRepository {
    private static final Logger LOGGER = AppLogger.getLogger();
    private final ObjectMapper objectMapper;

    public FileDataRepository() {
        this.objectMapper = new ObjectMapper();
        // Форматування JSON з відступами для зручного читання
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public RecipeBookData loadFromJson(Path filePath) throws IOException {
        if (filePath == null || !Files.exists(filePath)) {
            IOException ex = new IOException("Файл не знайдено: " + filePath);
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw ex;
        }

        try {
            RecipeBookData data = objectMapper.readValue(filePath.toFile(), RecipeBookData.class);
            if (data == null) {
                throw new IOException("Файл JSON порожній або має некоректну структуру");
            }
            linkAndValidate(data);
            LOGGER.info("Успішно імпортовано JSON: " + filePath.getFileName()
                    + " (рецептів: " + data.getRecipes().size()
                    + ", інгредієнтів: " + data.getIngredients().size() + ")");
            return data;
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Пошкоджений формат JSON у файлі: " + filePath, e);
            throw new IOException("Пошкоджений файл JSON: " + e.getOriginalMessage(), e);
        } catch (MissingIngredientException | IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Помилка валідації даних при імпорті: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void saveToJson(Path filePath, RecipeBookData data) throws IOException {
        Path parent = filePath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        // Запис у тимчасовий файл із подальшою заміною для збереження цілісності даних
        Path tempFile = Files.createTempFile(parent, "recipes_", ".tmp");
        try {
            objectMapper.writeValue(tempFile.toFile(), data);
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Успішно експортовано JSON у файл: " + filePath);
        } catch (IOException e) {
            Files.deleteIfExists(tempFile);
            LOGGER.log(Level.SEVERE, "Помилка збереження JSON у файл: " + filePath, e);
            throw e;
        }
    }

    // ШІ: Gemini – чернетка методу маршалінгу списку покупок у формат XML через JAXB.
    // Змінено: додано автоматичне створення директорій, форматування відступів та запис у журнал.
    @Override
    public void exportShoppingListToXml(Path filePath, ShoppingListXml shoppingList) throws IOException {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            JAXBContext context = JAXBContext.newInstance(ShoppingListXml.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(shoppingList, filePath.toFile());
            LOGGER.info("Список покупок експортовано у XML: " + filePath
                    + " (позицій: " + shoppingList.getItems().size() + ")");
        } catch (JAXBException e) {
            LOGGER.log(Level.SEVERE, "Помилка експорту XML через JAXB", e);
            throw new IOException("Не вдалося сформувати XML-файл: " + e.getMessage(), e);
        }
    }

    /**
     * Зв'язує інгредієнти у складі рецептів із довідником та перевіряє коректність даних.
     */
    public void linkAndValidate(RecipeBookData data) {
        Map<String, Ingredient> ingredientMap = new HashMap<>();
        for (Ingredient ing : data.getIngredients()) {
            if (ing.getName() == null || ing.getName().isBlank()) {
                throw new IllegalArgumentException("Знайдено інгредієнт без назви");
            }
            ingredientMap.put(ing.getName().toLowerCase(), ing);
        }

        for (Recipe recipe : data.getRecipes()) {
            recipe.validate();
            for (RecipeItem item : recipe.getItems()) {
                Ingredient found = ingredientMap.get(item.getIngredientName().toLowerCase());
                if (found == null) {
                    throw new MissingIngredientException(item.getIngredientName());
                }
                item.setIngredient(found);
            }
        }
    }
}