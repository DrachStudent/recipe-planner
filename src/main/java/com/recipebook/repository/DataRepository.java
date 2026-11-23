package com.recipebook.repository;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Інтерфейс репозиторію для імпорту та експорту даних у форматах JSON та XML.
 */
public interface DataRepository {
    RecipeBookData loadFromJson(Path filePath) throws IOException;

    void saveToJson(Path filePath, RecipeBookData data) throws IOException;

    void exportShoppingListToXml(Path filePath, ShoppingListXml shoppingList) throws IOException;
}