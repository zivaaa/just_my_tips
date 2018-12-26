# Шаринг картинок

Тут я набросал приложение для закачки картинок на сервер. 

 - авторизация
 - выбор картинок для закачки
 - вывод картинок с сервера
 - удаление картинок

### App

 - Retrofit
 - RxJava
 - Room
 - Picasso
 
Состоит из нескольких элементов
 - лендинг проверяет наличие токена доступа
 - форма логина позволяет авторизоваться и получить токен доступа
 - активити со списком картинок юзера с возможностью открыть в браузере, а также удаления
 - активити выбора файла или снимка с камеры с последующей загрузкой на сервак
 
### Server

Нехитрый сервер на Loopback с MongoDB (да можго должна быть установлена, а также бд img_db должна присутсвовать)

Установка:

```

npm install

//create models

node server/migrations/create_lb_models.js

```

Запуск

```

node server/server.js

```

Открыть апи

[http://localhost:3000/explorer/](http://localhost:3000/explorer/)



### Screenshots

![screen_1](https://github.com/zivaaa/just_my_tips/blob/master/ANDROID/_SAMPLE_APPS/ImageStorage/screenshots/screen_1.png =300x) 

![screen_2](https://github.com/zivaaa/just_my_tips/blob/master/ANDROID/_SAMPLE_APPS/ImageStorage/screenshots/screen_2.png =300x) 

![screen_3](https://github.com/zivaaa/just_my_tips/blob/master/ANDROID/_SAMPLE_APPS/ImageStorage/screenshots/screen_3.png =300x) 

![screen_4](https://github.com/zivaaa/just_my_tips/blob/master/ANDROID/_SAMPLE_APPS/ImageStorage/screenshots/screen_4.png =300x) 