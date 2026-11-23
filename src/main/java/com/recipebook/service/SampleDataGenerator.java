package com.recipebook.service;

import com.recipebook.model.*;
import com.recipebook.repository.RecipeBookData;

import java.util.*;

/**
 * Генератор початкових даних: 50 реалістичних рецептів із тижневим меню
 * та великого набору (10 000+ рецептів) для тестування багатопоточності.
 */
public final class SampleDataGenerator {

    private SampleDataGenerator() {
    }

    /**
     * Створює стартовий довідник: 35 інгредієнтів, 50 рецептів та план меню на тиждень.
     */
    public static RecipeBookData createStandardDataset() {
        List<Ingredient> ingredients = createBaseIngredients();
        Map<String, Ingredient> byName = toMap(ingredients);
        List<Recipe> recipes = new ArrayList<>();

        // --- 25 основних страв (MainDish) ---
        recipes.add(main("Борщ український", "Перші страви", 90, 6, "Варіння", byName,
                i("Буряк", 300), i("Картопля", 400), i("Капуста", 300), i("Яловичина", 450), i("Морква", 120), i("Цибуля", 100), i("Томатна паста", 50)));
        recipes.add(main("Гречка з курячим філе", "Другі страви", 35, 3, "Тушкування", byName,
                i("Гречка", 200), i("Куряче філе", 400), i("Цибуля", 80), i("Морква", 80), i("Олія соняшникова", 20)));
        recipes.add(main("Омлет із сиром та помідорами", "Сніданки", 15, 2, "Смаження", byName,
                i("Яйця", 180), i("Молоко", 80), i("Сир твердий", 60), i("Помідори", 120), i("Масло вершкове", 15)));
        recipes.add(main("Паста Карбонара", "Другі страви", 25, 2, "Варіння та смаження", byName,
                i("Макарони", 200), i("Бекон", 120), i("Яйця", 100), i("Сир твердий", 70), i("Вершки 20%", 100)));
        recipes.add(main("Салат Цезар з куркою", "Салати", 20, 2, "Без термічної обробки", byName,
                i("Куряче філе", 200), i("Салат айсберг", 150), i("Помідори", 100), i("Сир твердий", 40), i("Хліб білий", 60), i("Олія оливкова", 20)));
        recipes.add(main("Плов зі свининою", "Другі страви", 60, 5, "Тушкування", byName,
                i("Рис", 350), i("Свинина", 500), i("Морква", 250), i("Цибуля", 150), i("Олія соняшникова", 50)));
        recipes.add(main("Картопля запечена з розмарином", "Гарніри", 45, 4, "Запікання", byName,
                i("Картопля", 700), i("Олія оливкова", 35), i("Часник", 15)));
        recipes.add(main("Крем-суп грибний", "Перші страви", 35, 4, "Варіння", byName,
                i("Печериці", 400), i("Картопля", 300), i("Цибуля", 100), i("Вершки 20%", 200), i("Масло вершкове", 25)));
        recipes.add(main("Вівсяна каша з бананом і медом", "Сніданки", 12, 1, "Варіння", byName,
                i("Вівсянка", 70), i("Молоко", 200), i("Банан", 100), i("Мед", 15)));
        recipes.add(main("Котлети курячі парові", "Другі страви", 30, 4, "На парі", byName,
                i("Куряче філе", 500), i("Цибуля", 100), i("Яйця", 60), i("Хліб білий", 50)));
        recipes.add(main("Грецький салат", "Салати", 15, 2, "Без термічної обробки", byName,
                i("Помідори", 150), i("Огірки", 150), i("Сир твердий", 80), i("Олія оливкова", 25), i("Цибуля", 40)));
        recipes.add(main("Запечений лосось із лимоном", "Другі страви", 30, 2, "Запікання", byName,
                i("Лосось", 350), i("Лимон", 50), i("Олія оливкова", 15)));
        recipes.add(main("Вареники з картоплею", "Другі страви", 50, 4, "Варіння", byName,
                i("Борошно пшеничне", 350), i("Картопля", 500), i("Цибуля", 100), i("Масло вершкове", 40), i("Яйця", 60)));
        recipes.add(main("Сирники класичні", "Сніданки", 25, 3, "Смаження", byName,
                i("Сир кисломолочний", 400), i("Яйця", 60), i("Борошно пшеничне", 60), i("Цукор", 40), i("Олія соняшникова", 20)));
        recipes.add(main("Овочеве рагу", "Другі страви", 40, 4, "Тушкування", byName,
                i("Картопля", 300), i("Морква", 150), i("Капуста", 250), i("Помідори", 200), i("Цибуля", 100), i("Олія соняшникова", 30)));
        recipes.add(main("Курячий бульйон з локшиною", "Перші страви", 55, 5, "Варіння", byName,
                i("Куряче філе", 400), i("Макарони", 120), i("Морква", 100), i("Цибуля", 80)));
        recipes.add(main("Яєчня з беконом", "Сніданки", 10, 1, "Смаження", byName,
                i("Яйця", 120), i("Бекон", 70), i("Хліб білий", 50)));
        recipes.add(main("Рис з овочами та яйцем", "Гарніри", 25, 3, "Смаження", byName,
                i("Рис", 200), i("Морква", 100), i("Цибуля", 70), i("Яйця", 120), i("Олія соняшникова", 25)));
        recipes.add(main("Стейк з яловичини", "Другі страви", 20, 2, "Смаження", byName,
                i("Яловичина", 400), i("Масло вершкове", 30), i("Часник", 10), i("Олія оливкова", 15)));
        recipes.add(main("Тушкована капуста зі свининою", "Другі страви", 50, 4, "Тушкування", byName,
                i("Капуста", 600), i("Свинина", 350), i("Морква", 100), i("Томатна паста", 40), i("Олія соняшникова", 25)));
        recipes.add(main("Салат із буряка та сиру", "Салати", 30, 3, "Варіння", byName,
                i("Буряк", 350), i("Сир твердий", 80), i("Часник", 10), i("Сметана", 60)));
        recipes.add(main("Макарони по-флотськи", "Другі страви", 30, 4, "Варіння та смаження", byName,
                i("Макарони", 300), i("Яловичина", 350), i("Цибуля", 100), i("Олія соняшникова", 25)));
        recipes.add(main("Сендвіч із куркою та огірком", "Сніданки", 10, 2, "Без термічної обробки", byName,
                i("Хліб білий", 120), i("Куряче філе", 120), i("Огірки", 80), i("Салат айсберг", 40), i("Сир твердий", 40)));
        recipes.add(main("Лосось з рисом та огірком", "Другі страви", 30, 2, "Запікання", byName,
                i("Лосось", 250), i("Рис", 150), i("Огірки", 100), i("Олія оливкова", 10)));
        recipes.add(main("Гриби смажені у сметані", "Гарніри", 25, 3, "Смаження", byName,
                i("Печериці", 450), i("Цибуля", 100), i("Сметана", 150), i("Масло вершкове", 25)));

        // --- 15 десертів (Dessert) ---
        recipes.add(dessert("Шарлотка з яблуками", "Десерти", 50, 8, true, byName,
                i("Яблука", 450), i("Борошно пшеничне", 180), i("Цукор", 150), i("Яйця", 200)));
        recipes.add(dessert("Сирна запіканка", "Десерти", 45, 6, true, byName,
                i("Сир кисломолочний", 500), i("Яйця", 120), i("Сметана", 100), i("Цукор", 80), i("Борошно пшеничне", 50)));
        recipes.add(dessert("Шоколадний брауні", "Десерти", 40, 8, true, byName,
                i("Шоколад темний", 200), i("Масло вершкове", 150), i("Цукор", 150), i("Яйця", 180), i("Борошно пшеничне", 100)));
        recipes.add(dessert("Панкейки на молоці", "Десерти", 25, 4, true, byName,
                i("Борошно пшеничне", 200), i("Молоко", 250), i("Яйця", 60), i("Цукор", 40), i("Масло вершкове", 30)));
        recipes.add(dessert("Бананові оладки без цукру", "Десерти", 20, 2, false, byName,
                i("Банан", 200), i("Яйця", 120), i("Вівсянка", 80)));
        recipes.add(dessert("Медовик домашній", "Десерти", 90, 10, true, byName,
                i("Борошно пшеничне", 400), i("Мед", 100), i("Цукор", 180), i("Масло вершкове", 100), i("Сметана", 400), i("Яйця", 120)));
        recipes.add(dessert("Запечені яблука з медом", "Десерти", 30, 3, false, byName,
                i("Яблука", 450), i("Мед", 45), i("Волоський горіх", 40)));
        recipes.add(dessert("Ягідний крамбл", "Десерти", 35, 4, true, byName,
                i("Полуниця", 300), i("Вівсянка", 100), i("Борошно пшеничне", 80), i("Масло вершкове", 70), i("Цукор", 60)));
        recipes.add(dessert("Шоколадний мус", "Десерти", 20, 4, true, byName,
                i("Шоколад темний", 150), i("Вершки 20%", 250), i("Цукор", 30)));
        recipes.add(dessert("Вівсяне печиво з горіхами", "Десерти", 30, 6, true, byName,
                i("Вівсянка", 200), i("Борошно пшеничне", 100), i("Масло вершкове", 100), i("Цукор", 80), i("Волоський горіх", 60), i("Яйця", 60)));
        recipes.add(dessert("Млинці класичні", "Десерти", 35, 5, true, byName,
                i("Молоко", 500), i("Борошно пшеничне", 220), i("Яйця", 120), i("Цукор", 40), i("Олія соняшникова", 30)));
        recipes.add(dessert("Фруктовий салат з йогуртом", "Десерти", 15, 2, false, byName,
                i("Яблука", 150), i("Банан", 150), i("Полуниця", 150), i("Йогурт натуральний", 120)));
        recipes.add(dessert("Сирний десерт з полуницею", "Десерти", 15, 2, false, byName,
                i("Сир кисломолочний", 250), i("Полуниця", 150), i("Мед", 25), i("Йогурт натуральний", 80)));
        recipes.add(dessert("Трайфл шоколадно-банановий", "Десерти", 25, 4, true, byName,
                i("Банан", 200), i("Шоколад темний", 100), i("Вершки 20%", 200), i("Борошно пшеничне", 80), i("Цукор", 50)));
        recipes.add(dessert("Енергетичні кульки з вівсянки", "Десерти", 15, 4, false, byName,
                i("Вівсянка", 150), i("Мед", 60), i("Волоський горіх", 80), i("Какао-порошок", 20)));

        // --- 10 напоїв (Drink) ---
        recipes.add(drink("Лате класичний", "Напої", 7, 1, false, byName,
                i("Вода", 60), i("Кава мелена", 15), i("Молоко", 220), i("Цукор", 10)));
        recipes.add(drink("Капучино", "Напої", 7, 1, false, byName,
                i("Вода", 50), i("Кава мелена", 15), i("Молоко", 150)));
        recipes.add(drink("Айс-лате", "Напої", 5, 1, true, byName,
                i("Вода", 100), i("Кава мелена", 15), i("Молоко", 180), i("Цукор", 10)));
        recipes.add(drink("Гарячий шоколад", "Напої", 10, 2, false, byName,
                i("Молоко", 400), i("Шоколад темний", 80), i("Какао-порошок", 15), i("Цукор", 20)));
        recipes.add(drink("Какао домашнє", "Напої", 10, 2, false, byName,
                i("Молоко", 450), i("Какао-порошок", 25), i("Цукор", 30)));
        recipes.add(drink("Смузі бананово-полуничний", "Напої", 8, 2, true, byName,
                i("Банан", 150), i("Полуниця", 200), i("Йогурт натуральний", 250), i("Мед", 15)));
        recipes.add(drink("Лимонад домашній з медом", "Напої", 10, 4, true, byName,
                i("Вода", 900), i("Лимон", 150), i("Мед", 50)));
        recipes.add(drink("Чай чорний з лимоном", "Напої", 5, 1, false, byName,
                i("Вода", 250), i("Чай чорний", 5), i("Лимон", 25), i("Цукор", 10)));
        recipes.add(drink("Яблучний узвар", "Напої", 35, 6, true, byName,
                i("Вода", 1500), i("Яблука", 350), i("Мед", 40)));
        recipes.add(drink("Молочний коктейль з бананом", "Напої", 5, 2, true, byName,
                i("Молоко", 400), i("Банан", 180), i("Цукор", 20)));

        // --- Тижневий план меню ---
        MenuPlan menuPlan = new MenuPlan();
        menuPlan.addEntry(new MenuEntry("Понеділок", "Сніданок", "Вівсяна каша з бананом і медом", 2));
        menuPlan.addEntry(new MenuEntry("Понеділок", "Обід", "Борщ український", 2));
        menuPlan.addEntry(new MenuEntry("Понеділок", "Вечеря", "Гречка з курячим філе", 2));
        menuPlan.addEntry(new MenuEntry("Вівторок", "Сніданок", "Омлет із сиром та помідорами", 2));
        menuPlan.addEntry(new MenuEntry("Вівторок", "Обід", "Крем-суп грибний", 2));
        menuPlan.addEntry(new MenuEntry("Середа", "Сніданок", "Сирники класичні", 2));
        menuPlan.addEntry(new MenuEntry("Середа", "Вечеря", "Запечений лосось із лимоном", 2));
        menuPlan.addEntry(new MenuEntry("Четвер", "Обід", "Плов зі свининою", 2));
        menuPlan.addEntry(new MenuEntry("П'ятниця", "Вечеря", "Паста Карбонара", 2));
        menuPlan.addEntry(new MenuEntry("Субота", "Сніданок", "Панкейки на молоці", 3));
        menuPlan.addEntry(new MenuEntry("Неділя", "Обід", "Салат Цезар з куркою", 2));
        menuPlan.addEntry(new MenuEntry("Неділя", "Десерт", "Шарлотка з яблуками", 4));

        return new RecipeBookData(ingredients, recipes, menuPlan);
    }

    /**
     * Генерує великий набір рецептів (наприклад, 12 000) для тестування багатопоточності.
     */
    public static RecipeBookData createLargeDataset(int targetRecipeCount) {
        RecipeBookData base = createStandardDataset();
        List<Ingredient> ingredients = base.getIngredients();
        List<Recipe> templateRecipes = base.getRecipes();
        List<Recipe> largeList = new ArrayList<>(targetRecipeCount);

        // Спочатку додаємо базові 50 рецептів без змін, щоб тижневе меню залишалося коректним
        largeList.addAll(templateRecipes);

        Random random = new Random(42);
        String[] variants = {"Домашній", "Класичний", "Святковий", "Швидкий", "Фірмовий", "Легкий", "Авторський"};

        for (int idx = templateRecipes.size(); idx < targetRecipeCount; idx++) {
            Recipe tpl = templateRecipes.get(idx % templateRecipes.size());
            String newName = tpl.getName() + " (" + variants[idx % variants.length] + " #" + (idx + 1) + ")";

            List<RecipeItem> clonedItems = new ArrayList<>();
            for (RecipeItem orig : tpl.getItems()) {
                double factor = 0.8 + random.nextDouble() * 0.4;
                double amount = Math.round(orig.getAmount() * factor * 10.0) / 10.0;
                clonedItems.add(new RecipeItem(orig.getIngredient(), Math.max(5.0, amount)));
            }

            if (tpl instanceof MainDish md) {
                largeList.add(new MainDish(newName, md.getCategory(), md.getPrepTimeMinutes(), md.getServings(), clonedItems, md.getCookingMethod()));
            } else if (tpl instanceof Dessert ds) {
                largeList.add(new Dessert(newName, ds.getCategory(), ds.getPrepTimeMinutes(), ds.getServings(), clonedItems, ds.isContainsSugar()));
            } else if (tpl instanceof Drink dr) {
                largeList.add(new Drink(newName, dr.getCategory(), dr.getPrepTimeMinutes(), dr.getServings(), clonedItems, dr.isServedCold()));
            }
        }

        return new RecipeBookData(ingredients, largeList, base.getMenuPlan());
    }

    private static List<Ingredient> createBaseIngredients() {
        return List.of(
                new Ingredient("Картопля", 77, "г"),
                new Ingredient("Буряк", 43, "г"),
                new Ingredient("Капуста", 25, "г"),
                new Ingredient("Морква", 41, "г"),
                new Ingredient("Цибуля", 40, "г"),
                new Ingredient("Часник", 149, "г"),
                new Ingredient("Помідори", 18, "г"),
                new Ingredient("Огірки", 15, "г"),
                new Ingredient("Салат айсберг", 14, "г"),
                new Ingredient("Печериці", 22, "г"),
                new Ingredient("Томатна паста", 82, "г"),
                new Ingredient("Яловичина", 250, "г"),
                new Ingredient("Свинина", 242, "г"),
                new Ingredient("Куряче філе", 113, "г"),
                new Ingredient("Бекон", 541, "г"),
                new Ingredient("Лосось", 208, "г"),
                new Ingredient("Гречка", 343, "г"),
                new Ingredient("Рис", 365, "г"),
                new Ingredient("Вівсянка", 389, "г"),
                new Ingredient("Макарони", 371, "г"),
                new Ingredient("Борошно пшеничне", 364, "г"),
                new Ingredient("Хліб білий", 265, "г"),
                new Ingredient("Яйця", 155, "г"),
                new Ingredient("Молоко", 52, "мл"),
                new Ingredient("Вершки 20%", 205, "мл"),
                new Ingredient("Сметана", 206, "г"),
                new Ingredient("Йогурт натуральний", 61, "г"),
                new Ingredient("Сир твердий", 356, "г"),
                new Ingredient("Сир кисломолочний", 121, "г"),
                new Ingredient("Масло вершкове", 717, "г"),
                new Ingredient("Олія соняшникова", 884, "мл"),
                new Ingredient("Олія оливкова", 884, "мл"),
                new Ingredient("Цукор", 387, "г"),
                new Ingredient("Мед", 304, "г"),
                new Ingredient("Шоколад темний", 546, "г"),
                new Ingredient("Какао-порошок", 228, "г"),
                new Ingredient("Волоський горіх", 654, "г"),
                new Ingredient("Яблука", 52, "г"),
                new Ingredient("Банан", 89, "г"),
                new Ingredient("Полуниця", 32, "г"),
                new Ingredient("Лимон", 29, "г"),
                new Ingredient("Вода", 0, "мл"),
                new Ingredient("Кава мелена", 2, "г"),
                new Ingredient("Чай чорний", 1, "г")
        );
    }

    private static Map<String, Ingredient> toMap(List<Ingredient> list) {
        Map<String, Ingredient> map = new HashMap<>();
        for (Ingredient ing : list) {
            map.put(ing.getName().toLowerCase(), ing);
        }
        return map;
    }

    private record ItemSpec(String name, double amount) {
    }

    private static ItemSpec i(String name, double amount) {
        return new ItemSpec(name, amount);
    }

    private static List<RecipeItem> buildItems(Map<String, Ingredient> map, ItemSpec... specs) {
        List<RecipeItem> list = new ArrayList<>();
        for (ItemSpec s : specs) {
            Ingredient ing = map.get(s.name.toLowerCase());
            if (ing == null) {
                throw new MissingIngredientException(s.name);
            }
            list.add(new RecipeItem(ing, s.amount));
        }
        return list;
    }

    private static MainDish main(String name, String cat, int time, int servings, String method,
                                 Map<String, Ingredient> map, ItemSpec... specs) {
        return new MainDish(name, cat, time, servings, buildItems(map, specs), method);
    }

    private static Dessert dessert(String name, String cat, int time, int servings, boolean sugar,
                                   Map<String, Ingredient> map, ItemSpec... specs) {
        return new Dessert(name, cat, time, servings, buildItems(map, specs), sugar);
    }

    private static Drink drink(String name, String cat, int time, int servings, boolean cold,
                               Map<String, Ingredient> map, ItemSpec... specs) {
        return new Drink(name, cat, time, servings, buildItems(map, specs), cold);
    }
}