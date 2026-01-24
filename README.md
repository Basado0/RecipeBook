# Recipe Book  - Приложение для поиска рецептов 🍽️
Проект разработан студентом **Агарковым Егором Дмитриевичем**  
Группа: **Б9123-09.03.02прс**  
Тема проекта: **Взаимодействие с API, Coroutines + Retrofit**

## Технические требования
* Compose + Material3
* Navigation Compose
* ViewModel + viewModelScope
* Retrofit + конвертер (Gson или kotlinx.serialization)

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

## Чек-лист что из обязательных требований было сделано
+ Минимум 2 экрана:
  * List/Search - список результатов (и поиск/фильтр)
  * Detail/{id} - детали элемента по аргументу в route
+ Архитектура
  * UiState
  * ViewModel
  * UI-экраны максимально stateless: получают state + callbacks ( onEvent )
  * Repository между ViewModel и Retrofit
+ Все сетевые запросы - через suspend функции Retrofit, Запуск из viewModelScope
+ UI состояния
  * Loading (показываем индикатор)
  * Error (сообщение + кнопка Retry)
  * Empty (если ничего не найдено)
  * Success (список)
+ Избранное
  * Можно добавлять/убирать элемент в favourites
  * хранится в ViewModel state

## Бонусы
1. Debounce поиска без Flow: Job + delay(300–500ms) и отмена
2. Экран Favourites как отдельный route
3. Логирование запросов (OkHttp logging)
