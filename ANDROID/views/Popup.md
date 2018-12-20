# PopupMenu (всплывающее меню)

Чтобы показать менюшку в ответ на клик по элементу нужно сделать следующее.

Создать xml layout.

```
// res/menu/image_menu.xml

<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:app="http://schemas.android.com/apk/res-auto" xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:title="@string/copy_to_clipboard"
          android:id="@+id/itemCopy"
          app:showAsAction="never"/>
    <item android:title="@string/delete"
          android:id="@+id/itemDelete"
          app:showAsAction="never"/>
</menu>

```

Создать меню, встроить луйаут в него, указать обработчики клика элементов меню и показать.
Например так:


```

moreBtn.setOnClickListener {
        val menu = PopupMenu(context, moreBtn)
        menu.inflate(R.menu.image_menu)
        menu.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.itemCopy -> {
                    //TODO
                }
                R.id.itemDelete -> {
                    //TODO
                }
            }
            true
        }
        menu.show();
}

```

Есть еще способ создавать его в коде, но мне лень его описывать.

[ScreenShot](https://bitbucket.org/zivaaa/just_my_tips/src/master/ANDROID/_images/popup_menu.png)
