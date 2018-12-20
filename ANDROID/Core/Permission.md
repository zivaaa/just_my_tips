### Разрешения (Permissions)

Спрашиваем разрешение, чекаем его присутствие. Переспрашиваем.

Чтобы проверить разрешение можно юзать такую функцию

```

    private fun doesPermissionGiven(): Boolean {
        return ContextCompat.checkSelfPermission(
            view.getAppContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

```

Спрашивать разрешение нужно учитывая, что юзер может не дать его и заблочить запрос на него.
Поэтому надо показывать пояснение почему оно нужно. 
В следующей функции может всплыть SnackBar с сообщением и кнопкой, по клику на которую снова появляется запрос разрешения.


```

    private fun tryRequestExternalFileAccessPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this.view as Activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {

            Snackbar.make(findViewById(R.id.content), "Please give me permission!", Snackbar.LENGTH_LONG).also {
                    it.setAction("Allow"", {
                        requestWritePermissions()
                    })
                it.show()

        } else {
            requestWritePermissions()
        }
    }

```

Сам запрос разрешений

```
    private fun requestWritePermissions() {
        if (android.os.Build.VERSION.SDK_INT > 22) {
            (view as Activity).requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_PERMISSION_CODE
            )
        }
    }

```

Теперь у юзера выскакивает запрос на получение разрешений. Надо тречить результат.
Переопределяем у активити следующий метод и проверям, что было разрешено.

```
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == WRITE_PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //YES, GOT IT!
            }
        }
    }

```

