# Как просматривать данные BD с девайса без рута

1. Заходим в ``` View -> Tool Windows -> Device File Explorer ```

2. Ищем файл DB, например ``` /data/data/{package_name}/databases/room_example ```

3. Сохраняем у себя на ПК.

4. Открываем в любой программе понимающей sql_lite.

В моем случае для ubuntu 18. Я ставил ``` DB Browser For Sql Lite. ```.

### Способ через adb (не проверял).

``` adb devices ```

``` adb -s 8402a8bf shell ```

``` run-as com.zivaaa18.roomexample ```

``` cd databases ```

Возможно надо юзать ``` su ``` для рута.

``` sqllite3 room_example.db ```

