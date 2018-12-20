# Рецепты для UI

### Как сделать длину в процентах от длины родительского контейнера.

``` android:weightSum & android:layout_weight```

Допустим сделаем кнопку длиной 70% от родителя и прилегающей к правой его стороне

```

    <LinearLayout
            android:orientation="horizontal"
            android:layout_width="match_parent"

            android:weightSum="10"
            android:gravity="right"

            android:layout_height="match_parent">
        <Button
                android:text="Button"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:id="@+id/button2"

                android:layout_weight="7"/>
    </LinearLayout>


```

### Как сделать прозрачный модальный диалог

```

class SpinnerDialog : DialogFragment() {
    companion object {
        fun getInstance()  : SpinnerDialog{
            return SpinnerDialog().apply { setCancelable(false) }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NO_TITLE, theme)
        return AlertDialog.Builder(activity)

            .setView(
                activity!!.layoutInflater.inflate(R.layout.spinner, null)
            )
            .create().apply {
                //Делаем фон диалога прозрачным
                window.setBackgroundDrawableResource(android.R.color.transparent)
            }
    }
}

```

Его вызов

```

        SpinnerDialog.getInstance().show(supportFragmentManager, "just_a_tag")
		
```

### Активити без Action Bar.

```
//styles.xml

<resources>

    <!-- Base application theme. -->
    <style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
        <!-- Customize your theme here. -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>

    <style name = "AppTheme.NoActionBar" parent = "AppTheme">
        <item name="windowActionBar">false</item>
        <item name="windowNoTitle">true</item>
    </style>

</resources>

//Manifest

...
<activity android:name=".LandingActivity" android:theme="@style/AppTheme.NoActionBar"> 
...


```

### Открытие ссылки по клику на TextView

```

android:autoLink="web"

```


### Задать пропорции view


```

app:layout_constraintDimensionRatio="1,1"

```
