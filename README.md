# Recipe Book  - Приложение для поиска рецептов 🍽️
Проект разработан студентом **Агарковым Егором Дмитриевичем**  
Группа: **Б9123-09.03.02прс**  
Тема проекта: **Flow поверх проекта из прошлого дз**

## Реализованные требования

### 1. Flow используется по назначению

Реализовано **осмысленное реактивное поведение** — состояние экрана собирается из нескольких потоков:

- **Поиск по мере ввода:** `query → debounce(500ms) → distinctUntilChanged() → flatMapLatest(search)`
- **Объединение поиска + кэша из Room:** при пустом запросе автоматически показывается кэш, при вводе — сетевые результаты
- **Автоматическое обновление UI:** история и избранное из Room реактивно обновляют все экраны без ручной перезагрузки
- **Ручной retry:** как для поиска, так и для загрузки рецепта

### 2. Flow не простой State

- **Нет ручных** `uiState = uiState.copy(...)` — вся логика построена на композиции потоков
- `MutableStateFlow` используется **только для UI-источников** (query, selectedMealId)
- `SharedFlow` используется для **потоков действий** (retry), которые реально влияют на перезапуск Flow-цепочек
- Все suspend-вызовы встроены в реактивные цепочки с `flatMapLatest`

### 3. Минимум 3 источника данных/событий

Реализовано **6 независимых источников**:

| Источник | Тип | Откуда |
|----------|-----|--------|
| `_query` | `MutableStateFlow<String>` | UI (строка поиска) |
| `_retryTrigger` | `MutableSharedFlow<Unit>` | UI (кнопка Retry поиска) |
| `_selectedMealId` | `MutableStateFlow<Int?>` | UI (выбор рецепта) |
| `_recipeRetryTrigger` | `MutableSharedFlow<Unit>` | UI (кнопка Retry рецепта) |
| `cachedMealsFlow` | `Flow<List<Meal>>` | Data layer (Room) |
| `favouritesFlow` | `Flow<List<Meal>>` | Data layer (Room) |
| `historyFlow` | `Flow<List<Meal>>` | Data layer (Room) |

**Из data layer приходят 3 источника** (кэш, избранное, история — все из Room через Flow).

### 4. Использованы операторы Flow

| Оператор | Где применён | Назначение |
|----------|--------------|------------|
| `debounce` | `debouncedQuery` | Задержка поиска по мере ввода |
| `distinctUntilChanged` | `debouncedQuery` | Пропуск одинаковых запросов |
| `map` | `debouncedQuery` | Очистка строки (trim) |
| `flatMapLatest` | `searchResultsFlow`, `recipeFlow` | Отмена предыдущего запроса при новом вводе |
| `combine` | `dataFlow`, `uiState` | Объединение 7 потоков в итоговое состояние |
| `catch` | `searchResultsFlow` | Обработка ошибок сети |
| `filterNotNull` | `recipeFlow` | Фильтрация null значений |
| `onStart` | `searchResultsFlow`, `recipeFlow` | Начальная эмиссия |
| `stateIn` | `uiState` | Преобразование в StateFlow |

**Объединение:** `combine` используется для композиции 7 потоков  
**Управление потоком:** `flatMapLatest` отменяет предыдущие сетевые запросы

### 5. StateFlow и SharedFlow применены уместно

- **StateFlow** — для длительного состояния:
  - `_query` — текущий поисковый запрос
  - `_selectedMealId` — выбранный рецепт
  - `uiState` — итоговое состояние всего UI

- **SharedFlow** — для потока действий (может вызываться многократно):
  - `_retryTrigger` — перезапуск поиска
  - `_recipeRetryTrigger` — перезапуск загрузки рецепта

### 6. Экран остаётся рабочим

- **2+ экрана:** Search, RecipeDetail, Favourites, History
- **Navigation Compose** с передачей аргументов
- **Hilt** для внедрения зависимостей
- **Room** для кэша, избранного и истории
- **Retrofit + coroutines + viewModelScope**
- **UI-состояния:** Loading, Error + Retry, Empty, Success

## Выбранный API - spoonacular API: https://spoonacular.com/food-api/docs
**Были использованы следующие API эндпоинты:**

* GET https://api.spoonacular.com/recipes/complexSearch?query=  - для поиска блюд по названию
* GET https://api.spoonacular.com/recipes/{id}/information - получение ингридиентов для конкретного блюда по id
* GET https://api.spoonacular.com/recipes/{id}/analyzedInstructions - получение шагов для приготовления блюда по id

Доступ к API предоставляется по ключу, который можно получить после регистрации на сайте https://spoonacular.com/food-api/console

Количество запросов ограничено 50 в день для бесплатного пользования😢😭

## Как запустить? ##
1. Скопировать репозиторий
2. В папке api в ApiKey.kt прописать полученный ключ api

## Что хранится в Room:
1. таблица favourites_meals с полями:
   * id - идентификатор блюда
   * title - название блюда
   * image - ссылка на изображение блюда
* сценарий: избранные блюда остаются после перезапуска приложения
* как проверить: перезапустить приложение, на странице избранного останутся избранные блюда
2. таблица search_meals, такие же поля как и у favourites_meals, в таблице храняться результаты последнего поиска
* сценарий: при перезапуске приложения в списке показываются последние найденные блюда
* как проверить: перезапустить приложение, в списке вместо приглашения ввести название появятся карточки блюд
3. таблица history, те же поля + поле viewedAt - содержит время просмотра деталей блюда
* сценарий: на вкладке истории показываются карточки блюд, на детали которого переходили, отсортированы по новизне
* как проверить: перезапустить приложение, на экране истории сохраняться карточки блюд, если нажать на детали блюда, а потом перейти в историю, то это блюдо будет в верху списка

## Скриншоты приложения

### Поиск

<table> <tr> <td width="50%"> <img width="421" height="938" alt="image" src="https://github.com/user-attachments/assets/6e5b6de7-ce95-4880-aafb-00d74ca379bb" /></td> 
  <td width="50%"> <img width="417" height="932" alt="image" src="https://github.com/user-attachments/assets/45355da9-958c-4f30-86ca-1dc866e85245"/></td>
</tr>
</table>
<img width="405" height="900" alt="image" src="https://github.com/user-attachments/assets/6be61dcd-77df-4b66-a30b-939f6db57459" />

### Просмотр рецепта блюда
<table> <tr> <td width="50%"> <img width="421" height="938" alt="image" src="https://github.com/user-attachments/assets/f3bacb7a-eaf8-453b-b891-709ab20d3cb8"/></td> 
  <td width="50%"> <img width="417" height="932" alt="image" src="https://github.com/user-attachments/assets/a0f57f4e-41f0-4dfd-ad58-33b551da7c7d"/></td>
</tr>
</table>

### Список избранных рецептов
<table> <tr> <td width="50%"> <img width="421" height="938" alt="image" src="https://github.com/user-attachments/assets/b57def97-1a53-4a52-8ab8-3d1b53c67181"/></td> 
  <td width="50%"> <img width="417" height="932" alt="image" src="https://github.com/user-attachments/assets/798343c3-2531-4cf3-8196-b10a9f1f1acd"/></td>
</tr>
</table>

### Кэшированный результат последнего поиска
<img width="395" height="896" alt="image" src="https://github.com/user-attachments/assets/d6993cbf-12a8-4565-9e86-feb9048df1f0" />
строка поиска пуста, но список последних показан

### История просмотров блюд
<img width="402" height="897" alt="image" src="https://github.com/user-attachments/assets/8ab2fd65-fbd5-4773-b4f4-53e78230a54a" />
