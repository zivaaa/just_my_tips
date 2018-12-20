# Picasso

Библиотека для суперудобного получения картинок из сети/файлов и может откудато еще.

Можно урл и загрузить, а затем сразу обработать и вывести на экран. 

### Мини-пример с небольшой долей Retrofit2.

- Приложение будет получать картинки с сайта https://picsum.photos/ (прикольный кстати сайт).

- Сохранять url на них и выводить на экран.

- Выглядить это будет как 6 ячеек с 6 картинками на весь экран, по клику на любую из них произойдет ее смена.

1. В манифест пишем

``` 

<uses-permission android:name="android.permission.INTERNET"/>

``` 


Лейаут ячейки image_item.xml. Во время загрузки показываем прогресс, а как все будет готово то картинку.

``` 

<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
                                             xmlns:app="http://schemas.android.com/apk/res-auto"
                                             android:layout_width="match_parent"
                                             android:background="@drawable/rect"
                                             android:layout_weight="1"
                                             android:layout_height="match_parent">

    <ImageView
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:scaleType="centerCrop"
            app:srcCompat="@android:color/holo_purple"
            android:id="@+id/imageView" app:layout_constraintEnd_toEndOf="parent"
            android:layout_marginEnd="0dp"
            android:layout_marginBottom="0dp"
            android:layout_marginStart="0dp"
            android:layout_marginTop="0dp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"/>
    <ProgressBar
            style="?android:attr/progressBarStyle"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:minHeight="80dp"
            android:layout_marginEnd="0dp"
            android:layout_marginBottom="0dp"
            android:layout_marginStart="0dp"
            android:layout_marginTop="0dp"
            android:minWidth="80dp"
            android:visibility="gone"
            android:id="@+id/imageItemProgress"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
    />
</android.support.constraint.ConstraintLayout>

```

Небольшая рамочка для контейнера item (для красоты)

```

<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <stroke android:color="@color/colorAccent" android:width="1dp"></stroke>
</shape>

```


Лейаут активити activity_main.xml.

``` 

<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">

    <TableLayout
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:id="@+id/tableOfImages"
            android:layout_marginEnd="8dp"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent" android:layout_marginStart="8dp"
            android:layout_marginTop="8dp"
            app:layout_constraintTop_toTopOf="parent" android:layout_marginBottom="8dp"
            app:layout_constraintBottom_toBottomOf="parent" android:layout_margin="0dp">

        <TableRow android:layout_weight="1"
                  android:layout_width="match_parent"
                  android:layout_height="match_parent"
        >

        </TableRow>
        <TableRow android:layout_weight="1"
                  android:layout_width="match_parent"
                  android:layout_height="match_parent"
        >

        </TableRow>

    </TableLayout>
</android.support.constraint.ConstraintLayout>

```

2. Теперь по загрузке картинок.

Будем стучаться по ссылке https://picsum.photos/200/300/?random. Она вернет сначала 309 статус. 
А там надо ссылку сохранить, поэтому сначала будем делать запрос через Retrofit2. 
Получать ответ, а затем уже эту ссылку сохранять и просить Picasso загрузить картинку во вьюшку.

Кодим:

``` 


interface Api {

    @GET("/{width}/{height}")
    fun getImageUrl(
        @Path("width") width: Int,
        @Path("height") height: Int,
        @Query("random") random: String = "?"
    ): Call<ResponseBody>

}


class ApiClient private constructor(followRedirects: Boolean = true) {
    companion object {
        /**
         * https://picsum.photos/
         */
        val BASE_API: String = "https://picsum.photos"

        fun getApi(followRedirects: Boolean = true): Api {
            return ApiClient(followRedirects).getApi()
        }
    }

    private var api: Api

    init {
        api = Retrofit.Builder()
            .baseUrl(BASE_API)
            .client(OkHttpClient.Builder().followRedirects(followRedirects).build())
            .build()
            .create(Api::class.java)
    }

    fun getApi(): Api {
        return api
    }

}

```

Тут все просто, единственное это надо 
``` .client(OkHttpClient.Builder().followRedirects(followRedirects).build()) ``` 
передать настройку запрещающую авторедиректы (хотя можно наверне как-то попроще придумать, ну да не важно).

3. Активити. По частям.

``` 


class MainActivity : AppCompatActivity() {

    companion object {
        val IMAGE_WIDTH: Int = 200
        val IMAGE_HEIGHT: Int = 300
        val START_IMAGE_ID : Int = 2000
        val PREFERENCE_KEY : String = "preference"
    }

    lateinit var apiNoRedirect: Api

    var table: TableLayout? = null

    inner class Item(
        //...
    ) {
       //...
    }

    enum class ItemState {
        IDLE, LOADING, ERROR
    }

    var allItems = mutableListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        apiNoRedirect = ApiClient.getApi(false)

        table = findViewById(R.id.tableOfImages)
        fillTable()
    }

    /**
     * Fill table with new items.
     */
    fun fillTable() {
        var currentId = START_IMAGE_ID
        for (i in 0..1) {
            val row = table?.getChildAt(i) as TableRow

            for (j in 0..2) {

                val view = layoutInflater.inflate(R.layout.image_item, row, false)
                view.id = currentId

                row.addView(view)

                Item(view).also { allItems.add(it) }

                currentId++

            }
        }

    }

}

```

Пока опустим Item, который будет хранить состояние, загружать/сохранять url, делать запросы.
Делаем следующее:

 - Создаем Api. Для запроса ссылка на конкретную картинку.
 
 - Получаем ссылку на таблицу в которой будем выводить это все.
 
 - Заполняем ее ``` fillTable() ```
 
 Заполнение происходит по двум строкам которые уже есть в лейауте. В каждую встраиваем по 3 представления компонента с картинкой. 
 Создаем Item и задаем id по которому можно будет искать его если нужно. Ну и чтобы понять куда грузить сохраненную картинку.
 
 Теперь Item
 
 ``` 
 
 
    inner class Item(
        var view: View,
        private var state: ItemState = MainActivity.ItemState.IDLE,
        private var url: String = ""
    ) {
        private var viewId : Int
        private var progress : ProgressBar
        private var image : ImageView

        init {
            progress = view.findViewById<ProgressBar>(R.id.imageItemProgress)
            image = view.findViewById<ImageView>(R.id.imageView)
            viewId = view.id


            view.setOnClickListener {
                this.reloadImage()
            }

            loadSavedUrl()
        }

        /**
         * Save url in storage
         */
        private fun saveUrl(url : String) {
            var pref = this@MainActivity.getPreferences(Context.MODE_PRIVATE).edit() ?: return
            pref.putString(PREFERENCE_KEY + "_${viewId}", url).commit()
        }

        /**
         * Load saved url and use it in image
         */
        private fun loadSavedUrl() {
            var pref = this@MainActivity.getPreferences(Context.MODE_PRIVATE) ?: return
            var url = pref.getString(PREFERENCE_KEY + "_${viewId}", "")
            if (url != "") {
                setUrl(url, false)
            }
        }

        fun setState(state : ItemState) {
            if (this.state == state) {
                return
            }

            this.state = state

            when (state) {
                ItemState.IDLE -> {
                    progress.visibility = View.GONE
                    image.visibility = View.VISIBLE
                }
                ItemState.ERROR -> {
                    progress.visibility = View.GONE
                    image.visibility = View.VISIBLE
                    image.setImageDrawable(null)

                }
                ItemState.LOADING -> {
                    progress.visibility = View.VISIBLE
                    image.visibility = View.GONE
                }
            }
        }

        /**
         * Set image url, load it in view, and save in storage if required.
         */
        fun setUrl(url : String, needToSaveIt : Boolean = true) {
            this.setState(ItemState.LOADING)
            this.url = url

            if (needToSaveIt) {
                saveUrl(url)
            }

            /**
             * If not using centerCrop in ImageView prop u can do
             *
             * .load(...).centerCrop()
             *
             * To resize ->
             *
             *  .load(...).resize(200, 300)
             *
             */
            Picasso.get().load(url).into(image, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    this@Item.setState(ItemState.IDLE)
                }

                override fun onError(e: Exception?) {
                    this@Item.setState(ItemState.ERROR)
                    Toast.makeText(this@MainActivity, e?.message, Toast.LENGTH_SHORT).show()
                }
            })
        }

        /**
         * Try to get new image from service
         */
        fun reloadImage() {
            setState(ItemState.LOADING)

            apiNoRedirect.getImageUrl(IMAGE_WIDTH, IMAGE_HEIGHT).enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    this@Item.setState(ItemState.ERROR)
                    Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val headers = response.headers()
                    val location = headers.get("location")

                    if (location == null) {
                        Toast.makeText(this@MainActivity, "location hasn't been found!", Toast.LENGTH_SHORT).show()
                        setState(ItemState.ERROR)
                    } else {
                        if (Uri.parse(location).scheme == null) {
                            setUrl(ApiClient.BASE_API + location)
                        } else {
                            setUrl(location)
                        }
                    }
                }
            })
        }
    }
 
 ```

Какойто он большой получился конечно, но впринципе простой.

 - в конструкторе задаем калбек на клик по вьюшке.
  
 - пытается выгрузить сохраненную в памяти url для нее, и если выходит то просим Picasso нам картинку показать.
 
 - по клику происходит ``` reloadImage() ``` который прячет картинку и показывает прогресс. Потом делаем запрос через Retrofit2, чтобы получить url картинки.
  Там мы проверяем хедер ``` location ```. Там обычно приходит чтото типа 
 
 
 ```
 
 location: /200/300/?image=108
 
 ```
 - и ели все хорошо, сохраняем url и говорим - "Picasso, выгрузи картинку вот отсюда и закинь вот сюда"
 
 ``` 
 
 Picasso.get().load(url).into(image, ...)
 
 
 ```
 
 - там же вторым аргументом стоит калбэк, по вызову которого убиаем спиннер и показываем картинку.
 
 На этом все.
 