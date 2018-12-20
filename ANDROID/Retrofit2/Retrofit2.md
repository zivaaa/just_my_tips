# Пример Retrofit2 + Android

Retrofit2 - библиотека для упрощения сетевых запросов. Подключим и заставим ее работать.

1. Для работы нужен сетевой ресурс. Следующий код это скрипт для nodejs, написаный на коленке, под управлением фреймворка express.

Установка зависимостей:

```
	npm init
	npm install express body-parser	
```

Запуск:

``` 
	nodejs index
```

Код:

```
var express = require('express');
var app = express();
var bodyParser = require('body-parser')

var os = require('os');
var ifaces = os.networkInterfaces();

Object.keys(ifaces).forEach(function (ifname) {
	var alias = 0;

	ifaces[ifname].forEach(function (iface) {
		if ('IPv4' !== iface.family || iface.internal !== false) {
			// skip over internal (i.e. 127.0.0.1) and non-ipv4 addresses
			return;
		}

		if (alias >= 1) {
			// this single interface has multiple ipv4 addresses
			console.log(ifname + ':' + alias, iface.address + "\n");
		} else {
			// this interface has only one ipv4 adress
			console.log(ifname, iface.address + "\n");
		}
		++alias;
	});
});

var subjects = [{
		method: "XXX",
		key: "YYY"
	},
	{
		method: "XXX2",
		key: "YYY2"
	},
	{
		method: "XXX3",
		key: "YYY3"
	}
]


app.use(bodyParser.json());


app.get('/', function (req, res) {
	res.send('Hello World!');
});

app.get('/subject', function (req, res) {
	console.log("get /subject");
	//res.json(subjects);
	setTimeout(function () {
		console.log("post /subject sent back");
		res.json(subjects);
	}, 2000)
});

app.get('/subject/:id', function (req, res) {
	console.log("get /subject/:id'");
	setTimeout(function () {
		console.log("get /subject/:id'");
		res.json(subjects[req.params.id]);
	}, 2000)

});


app.post('/subject', function (req, res) {
	try {
		console.log("post /subject");

		var json = req.body
		subjects.push(json);

		setTimeout(function () {
			console.log("post /subject' response");
			res.json(json);
		}, 2000)
	} catch (e) {
		console.error("WRONG BODY!")
		console.log(req.body)
		setTimeout(function () {
			console.log("post /subject' response");
			res.status(400).json(json);
		}, 2000)
	}
});


var listener = app.listen(3000, function () {
	console.log('listening on', listener.address());
});
```

Сначала выведет все ip адреса куда стучаться, потом настроит роуты, а потом начнет слушать порт 3000. В моем случае адрес: ``` 192.168.100.2:3000 ```

2.  Установка Retrofit2 и converter-gson для работы с JSON. Также понадобится RecyclerView для вывода данных.

``` 

android {
   ...
}

dependencies {
    ...
    implementation 'com.squareup.retrofit2:retrofit:2.5.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.5.0'
	
    implementation 'com.android.support:recyclerview-v7:28.0.0'
	...
}

```

3.  Кодим апи для работы в сети:

``` 
data class Subject(
    @SerializedName("method") @Expose var method : String = "",
    @SerializedName("key") @Expose var key : String = ""
)

```
Это простой дата класс с 2 полями, который будет учавствовать в работе по сети. Я снабдил их аннотациями

```@SerializedName("method") ```- показывает в какой ключ преобразуется поле снабженное аннотацией при конвертации в JSON (тут совпадают)

```@Expose ```- впринципе можно было опустить, помеченное такой аннотацией поле будет сериализоваться всегда. 

Но если не помечено, то будет исключено при использовании  GsonBuilder.excludeFieldsWithoutExposeAnnotation() 


```

interface Api {
    @GET("subject")
    fun getSubjects() : Call<List<Subject>>

    @GET("subject/{id}")
    fun getSubjectById(@Path("id") id : Int) : Call<Subject>

    @POST("subject")
    fun postSubject(@Body subject : Subject) : Call<Subject>


}
```

Api - интерфейс который будет реализован далее. В нем описаны методы по которым будем делать запросы. Думаю тут все понятно.

``` 

class ApiClient private constructor() {
    companion object {
        val BASE_API : String = "http://192.168.100.2:3000"

        private var instance : Api? = null

        fun getApi() : Api {
            if (instance == null) {
                instance = ApiClient().api
            }
            return instance!!
        }
    }

    lateinit var retrofit : Retrofit
    lateinit var api : Api

    init {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(Api::class.java)
    }

}
```

ApiClient - класс заставляющий Retrofit реализовать объект api умеющий делать запросы и знающий куда и как их отправлять. Обрабатывать будем позже.
Здесь важен ``` getApi() ``` метод который создает и возвращает синглтон нужного для запросов класса.
Тут также указан IP куда стучаться. 

### Как делать запросы в Retrofit2

``` 
		//...
        ApiClient.getApi().getSubjects().enqueue(object : Callback<List<Subject>> {
            override fun onFailure(call: Call<List<Subject>>, t: Throwable) {
                //Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
                ///unsetLoadingState()
            }

            override fun onResponse(call: Call<List<Subject>>, response: Response<List<Subject>>) {
                //myAdapter.setData(response.body() ?: arrayListOf())
                //unsetLoadingState()
            }
        })
```

В объекте response мы получаем уже десериализованный объект который можно использовать. Там же можно проверить статус запрос (200, 400, ...). 
Вобщем реализовать нужное поведение.

Данный вариант делает запрос асинхронно, вызов калбэка же ловим в главном потоке. 


``` 
		//...
        val response: Response<List<Subject>> = ApiClient.getApi().getSubjects().execute()
        if (response.isSuccessful) {
            //myAdapter.setData(response.body() ?: arrayListOf())
        } else {
            //Toast.makeText(this@MainActivity, response.errorBody().toString(), Toast.LENGTH_SHORT).show()
        }
		//...

```

Если нужно делать синхронный запрос то путь почти тот же, но главный потом будет блокирован.

На этом вприницпе все. Далее просто код активити с диалогами, RecyclerView и 3 кнопками. 
1 кнопка загружает все объекты и выводит в recyclerview-v7
2 кнопка просто получает 1 рандомный объект и выводит в диалоге
3 кнопка посит рандомный объект, а потом запрашивает обновление

#### Как сделать запрос без который не будет автоматически перенаправлен (отключить редирект)

``` 

        api = Retrofit.Builder()
            .baseUrl(BASE_API)
            .client(OkHttpClient.Builder().followRedirects(followRedirects).build()) // Та самая настройка
            .build()
            .create(Api::class.java)

```

#### Как сделать чистый запрос без конвертеров

Если он не нужен то не добавляем конвертеры в билдер.

Также нужно задать вывод как ResponseBody. И тогда уже можно обрабатывать то, что получили.

```

            api.someRequest().enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    //TODO
                }

			
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    // см. response.raw
                }
            })

```

#### Как получить значение хедера

``` 
            api.someRequest().enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    //TODO
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val headers = response.headers()
                    val location = headers.get("location")
					// Делаем что нибудь с этим хедером
                }
            })
```


Пример реализации ниже -->>>>>

Добавляем разрешение на доступ в сеть в манифест

``` 

<uses-permission android:name="android.permission.INTERNET" />

```

MainActivity - тут вся логика

```

class MainActivity : AppCompatActivity() {

    lateinit var myAdapter: MyAdapter
    lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //recycler setup
        recyclerView = findViewById(R.id.recycler)
        myAdapter = MyAdapter(arrayListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = myAdapter


        findViewById<Button>(R.id.btnGetAll).setOnClickListener {
            getSubjectsAndUpdateList()

        }

        findViewById<Button>(R.id.getRandom).setOnClickListener {
            getRandomSubjectAndShowIfCan()

        }

        findViewById<Button>(R.id.postRandom).setOnClickListener {
            postRandomSubjectAndUpdateView()
        }
    }

    var dialog: SpinnerDialog? = null

    fun setLoadingState() {
        dialog = SpinnerDialog.getInstance()
        dialog?.show(supportFragmentManager, "just_a_tag")
    }

    fun unsetLoadingState() {
        if (dialog != null) {
            dialog?.dismiss()
        }
    }

    fun getSubjectsAndUpdateListSync() {

        val response: Response<List<Subject>> = ApiClient.getApi().getSubjects().execute()
        if (response.isSuccessful) {
            myAdapter.setData(response.body() ?: arrayListOf())
        } else {
            Toast.makeText(this@MainActivity, response.errorBody().toString(), Toast.LENGTH_SHORT).show()
        }
    }

    fun getSubjectsAndUpdateList() {
        setLoadingState()

        ApiClient.getApi().getSubjects().enqueue(object : Callback<List<Subject>> {
            override fun onFailure(call: Call<List<Subject>>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
                unsetLoadingState()
            }

            override fun onResponse(call: Call<List<Subject>>, response: Response<List<Subject>>) {
                myAdapter.setData(response.body() ?: arrayListOf())
                unsetLoadingState()
            }
        })
    }

    fun postRandomSubjectAndUpdateView() {
        val strings: List<String> = arrayListOf(
            "bad",
            "good",
            "eval",
            "stop",
            "change",
            "do",
            "same"
        )


        var subject = Subject(
            strings[getRandomInt(0, strings.size - 1)],
            strings[getRandomInt(0, strings.size - 1)]
        )

        Toast.makeText(this, "Trying to add new subject ${subject.toString()}", Toast.LENGTH_SHORT).show()
        setLoadingState()
        ApiClient.getApi().postSubject(subject).enqueue(object : Callback<Subject> {
            override fun onFailure(call: Call<Subject>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
                unsetLoadingState()
            }

            override fun onResponse(call: Call<Subject>, response: Response<Subject>) {
                Toast.makeText(this@MainActivity, "Maybe success. Now lets update all data again!", Toast.LENGTH_SHORT)
                    .show()
                unsetLoadingState()
                getSubjectsAndUpdateList()
            }
        })

    }

    fun getRandomInt(min: Int, max: Int): Int {
        return Random().nextInt((max - min) + 1) - min
    }

    fun getRandomSubjectAndShowIfCan() {
        if (myAdapter.itemCount == 0) {
            Toast.makeText(this, "U should load all subjects before call this action", Toast.LENGTH_SHORT).show()
            return
        }

        val rndInt = getRandomInt(0, myAdapter.itemCount - 1)

        setLoadingState()

        ApiClient.getApi().getSubjectById(rndInt).enqueue(object : Callback<Subject> {
            override fun onFailure(call: Call<Subject>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
                unsetLoadingState()
            }

            override fun onResponse(call: Call<Subject>, response: Response<Subject>) {
                val subject = response.body()
                ShowSubjectDialog.getInstance(subject.toString()).show(supportFragmentManager, "some_new_tag")
                unsetLoadingState()
            }
        })
    }


    class MyAdapter(private var subjects: List<Subject>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        fun setData(subjects: List<Subject>) {
            this.subjects = subjects
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
            return MyViewHolder(
                TextView(p0.context)
            ).apply {
                applySubject(subjects[p1])
            }
        }

        override fun getItemCount(): Int {
            return subjects.size
        }

        override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
            p0.applySubject(subjects[p1])
        }

        class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            fun applySubject(sub: Subject) {
                (itemView as TextView).setText(sub.key + ", " + sub.method)
            }

        }
    }
}

```

activity_main.xml - главный лейаут

```

<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">
    <android.support.v7.widget.RecyclerView android:layout_width="match_parent"
                                            android:layout_height="0dp"
                                            android:layout_marginBottom="8dp"
                                            android:id="@+id/recycler"
                                            app:layout_constraintBottom_toTopOf="@+id/linearLayout"
                                            android:layout_marginTop="8dp" app:layout_constraintTop_toTopOf="parent">

    </android.support.v7.widget.RecyclerView>
    <LinearLayout
            android:orientation="horizontal"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="8dp"
            app:layout_constraintBottom_toBottomOf="parent"
            android:id="@+id/linearLayout">
        <Button
                android:text="GET ALL"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" android:id="@+id/btnGetAll" android:layout_weight="1"/>
        <Button
                android:text="GET_RANDOM_SUBJECT"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" android:id="@+id/getRandom" android:layout_weight="1"/>

        <Button
                android:text="POST_RANDOM_SUBJECT"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" android:id="@+id/postRandom" android:layout_weight="1"/>
    </LinearLayout>

</android.support.constraint.ConstraintLayout>

```

ShowSubjectDialog - диалог для вывода данных об объекте

```

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment

class ShowSubjectDialog : DialogFragment() {
    companion object {
        val DATA_KEY : String = "data_subject"
        fun getInstance(dataToShow : String = "")  : ShowSubjectDialog{
            return ShowSubjectDialog().apply {
                var bundle : Bundle = Bundle()
                bundle.putString(DATA_KEY, dataToShow)
                arguments = bundle
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dataOfSubject = arguments?.getString(DATA_KEY, "no value saved")
        return AlertDialog.Builder(activity)
            .setTitle(dataOfSubject)
            .create()
    }
}

```

SpinnerDialog - прозрачный диалог для вывода спиннера на время запросов.

```

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment

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

spinner.xml - лейаут для спиннера.


``` 

<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
                                             xmlns:app="http://schemas.android.com/apk/res-auto"
                                             xmlns:tools="http://schemas.android.com/tools"
                                             android:layout_width="match_parent"
                                             android:layout_height="match_parent"

>

    <ProgressBar
            style="?android:attr/progressBarStyle"
            android:layout_width="120dp"
            android:layout_height="120dp"
            android:id="@+id/progressBar" android:layout_marginTop="8dp"
            app:layout_constraintTop_toTopOf="parent" android:layout_marginBottom="8dp"
            app:layout_constraintBottom_toBottomOf="parent" app:layout_constraintStart_toStartOf="parent"
            android:layout_marginStart="8dp" app:layout_constraintEnd_toEndOf="parent" android:layout_marginEnd="8dp"/>
    <TextView
            android:text="Just loading..."
            android:textColor="@color/colorAccent"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/textView" app:layout_constraintStart_toStartOf="parent" android:layout_marginStart="8dp"
            app:layout_constraintEnd_toEndOf="parent" android:layout_marginEnd="8dp"
            app:layout_constraintTop_toBottomOf="@+id/progressBar" android:layout_marginTop="8dp"
            app:layout_constraintHorizontal_bias="0.501"/>
</android.support.constraint.ConstraintLayout>


```