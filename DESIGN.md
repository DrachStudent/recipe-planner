# Архітектурна специфікація проєкту «Довідник рецептів із планувальником меню»

## 1. Стек технологій
- Мова програмування: Java 25 (OpenJDK 25.0.2).
- Графічний інтерфейс: JavaFX 25 (FXML + контролер).
- Збирання та залежності: Apache Maven (`pom.xml`, модульна система `module-info.java`).
- Серіалізація даних: Jackson (`jackson-databind` 2.18.2) для JSON, JAXB (`jakarta.xml.bind-api` 4.0.2 + `jaxb-runtime` 4.0.5) для XML.
- Логування: `java.util.logging` (`FileHandler` у файл `logs/recipe_planner.log`).

## 2. Архітектура (MVC) та структура пакетів
Обрано шаблон **MVC (Model – View – Controller)** для чіткого відокремлення доменної моделі та бізнес-логіки від інтерфейсу JavaFX:
- `com.recipebook` — точка входу застосунку (`MainApp`).
- `com.recipebook.model` — доменна модель предметної області: `Nutritional`, `Recipe`, `MainDish`, `Dessert`, `Drink`, `Ingredient`, `RecipeItem`, `MenuPlan`, `MenuEntry`, `MissingIngredientException`.
- `com.recipebook.repository` — доступ до файлової системи та формати даних: інтерфейс `DataRepository`, реалізація `FileDataRepository`, обгортки `RecipeBookData`, `ShoppingListXml`, `ShoppingListItem`.
- `com.recipebook.service` — бізнес-логіка та багатопотоковість: `RecipeBookService` (CRUD, цілісність даних), `RecipeProcessingService` (фонові задачі, пакетний розрахунок калорійності, формування списку покупок), `BenchmarkResult`, `SampleDataGenerator`, `AppLogger`.
- `com.recipebook.controller` — шар керування інтерфейсом: `MainController`, `DialogHelper`.
- `src/main/resources/com/recipebook/main-view.fxml` — декларативна розмітка представлення (View).

## 3. ООП-модель та прийняті рішення
- **Абстракція:** інтерфейс `Nutritional` задає контракт розрахунку харчової цінності (`calculateCalories()`, `getCalorieUnit()`); абстрактний клас `Recipe` об'єднує спільні атрибути й валідацію рецептів; інтерфейс `DataRepository` ізолює спосіб збереження даних від сервісів.
- **Наслідування:** класи `MainDish`, `Dessert` та `Drink` наслідують `Recipe`, додаючи власні властивості (`cookingMethod`, `containsSugar`, `servedCold`). Обрано ієрархію через абстрактний клас замість композиції, оскільки всі види страв мають спільний стан (назва, категорія, час, порції, склад) та єдину валідацію, але різний алгоритм розрахунку калорійності.
- **Поліморфізм:** метод `calculateCalories()` перевизначено у кожному нащадку (`MainDish` — на 1 порцію, `Dessert` — на 100 г, `Drink` — на 100 мл) і викликається поліморфно через колекцію `List<Recipe>` у таблиці та в сервісі пакетного розрахунку. Поліморфна серіалізація JSON реалізована через анотації Jackson `@JsonTypeInfo` та `@JsonSubTypes`.
- **Інкапсуляція:** усі поля моделей мають модифікатор `private`; зміна значень проходить через сетери з валідацією (порожні рядки та недодатні числа спричиняють `IllegalArgumentException`).

## 4. Формати даних і надійність
- **JSON (`data/recipes.json`, `data/recipes_large.json`):** містить масиви `ingredients`, `recipes` (з дискримінатором `"type": "MAIN_DISH" | "DESSERT" | "DRINK"`) та об'єкт `menuPlan`. Запис виконується через тимчасовий файл із заміною (`Files.move(..., REPLACE_EXISTING)`) для збереження цілісності.
- **XML (`data/shopping_list.xml`):** кореневий тег `<shoppingList>` із датою формування та переліком `<items><item>...</item></items>` (назва, сумарна кількість, одиниця виміру).
- **Обробка винятків:** перехоплюються пошкоджений синтаксис (`JsonProcessingException` → `IOException`), відсутній інгредієнт (`MissingIngredientException`), некоректне введення (`IllegalArgumentException`) та порушення посилальної цілісності при видаленні (`IllegalStateException`). Усі події фіксуються у `logs/recipe_planner.log`.

## 5. Багатопотокова обробка
- **Пул потоків:** `ExecutorService` (`Executors.newFixedThreadPool(threadCount)` із daemon-потоками, де `threadCount = Math.max(2, availableProcessors)`).
- **Фонові задачі (`javafx.concurrent.Task`):** імпорт великого файлу (`createImportTask`), пакетний розрахунок калорійності 15 000 рецептів частинами (`createBatchCaloriesTask`), формування списку покупок (`createShoppingListTask`).
- **Синхронізація:** спільні результати розрахунків і накопичення обсягів інгредієнтів захищені потокобезпечною структурою `ConcurrentHashMap` (метод `put` та атомарне злиття `merge`), а лічильник оброблених пакетів — `AtomicInteger`.
- **Оновлення UI:** статус оновлюється через `Platform.runLater()`, прогрес — через прив'язку `progressBar.progressProperty().bind(task.progressProperty())`.