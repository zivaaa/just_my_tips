# Небольшой пример Android RXJava2

Небольшой пример использования RXJava2 в андроид с примесью MVP

1. Добавляем зависимость в grandle app

``` 

android {
   ...
}

dependencies {
    ...
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.0'
}

```

2. Приложение просто выводит список юзеров.

Реализовано по паттерну MVP следующим образом:

 - в роли View выступает активити которая релизует интерфейс IMyView, ее задача выводить на экран данные, а также регировать на действия юзера дергая методы Presenter, к которому View имеет доступ.
 - Presenter это класс который связан с моделью и view, умеет получать данные от модели и заставлять view их выводить. О модели он знает все, а о View только несколько методов, имеющихся в интерфейсе IMyView.
 - Model умеет получать данные и возвращает настроенные Observables, которые нужны чтобы асинхронно их получать, а вот что с ними делать - это забота Presenter.

Приложение включает RecyclerView в который выводится список юзеров.
Юзеры подгружаются с задержкой блокирующей основной поток, поэтому используем Observable чтобы сделать это асинхронно.

Код Activity:

```
class MainActivity : AppCompatActivity(), IMyView {

    companion object {
        val TAG: String = "TAG"
    }

    lateinit var recycler : RecyclerView
    lateinit var adapter : MyAdapter
    lateinit var presenter : Presenter

    /**
     * update view by users list. This called by presenter
     */
    override fun showAllUsers(users: List<User>) {
        Log.d(TAG, "MainActivity.showAllUsers fired")
        adapter.setData(users)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        Log.d(TAG, "onCreate")


        //Setup recycler
        presenter = Presenter(this)

        adapter = MyAdapter(arrayListOf())

        recycler = findViewById<RecyclerView>(R.id.recycler).apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }


    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
        // Make presenter update users on start
        this.presenter.updateUsers()
    }


    class MyAdapter(private var dataset : List<User>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
        fun setData(users : List<User>) {
            this.dataset = users
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.some_item, p0, false)).apply {
                applyUser(dataset[p1])
            }
        }

        override fun getItemCount(): Int {
            return dataset.size
        }

        override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
            p0.applyUser(dataset[p1])
        }

        class MyViewHolder(view : View) : RecyclerView.ViewHolder(view) {
            var index : Int = 0

            fun applyUser(user : User) {
                itemView.findViewById<TextView>(R.id.nameView).setText(user.name)
                itemView.findViewById<TextView>(R.id.ageView).setText(user.age.toString())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}


```

Код Presenter, Model ...

``` 

/**
 * Model. Here we have all data logic.
 */
class Model(val manager : UserManager = UserManager()) {

    fun getUsers() : Observable<List<User>> {
        /**
         * Create Observable with some long run operation
         */
        return Observable.create(ObservableOnSubscribe<List<User>> {
            Log.d(MainActivity.TAG, "getUsers fired | Thread ${Thread.currentThread().id}")

            // Notify all subscribers
            it.onNext(manager.getAll())

            //Complete execution
            it.onComplete()

        })
            //set all subscribed methods to execute in MainThread (For UI)
            .observeOn(AndroidSchedulers.mainThread())
            //set method in create({...}) executed in IO thread
            .subscribeOn(Schedulers.io())
    }


	//Not using in app
    fun getUserByIndexSingle(index : Int) : Single<User> {
        return Single.create(SingleOnSubscribe<User> {
            val user = manager.getByIndex(index)
            Log.d(MainActivity.TAG, "getUserByIndexSingle fired | Thread ${Thread.currentThread().id}")
            if (user == null) {
                it.onError(Throwable("user not found"))
            } else {
                it.onSuccess(user)
            }
        }).observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
    }
}

/**
 * Presenter. Used to link view and data
 */
class Presenter(
    val myView : IMyView,
    val myModel : Model = Model()
) {
    /**
     * Make request for all users. Then ask view to update self by user list
     */
    fun updateUsers() {
        myModel.getUsers().subscribe {
            Log.d(MainActivity.TAG, "Presenter.updateUsers subscriber | Thread ${Thread.currentThread().id}")
            myView.showAllUsers(it)
        }
    }
}

/**
 * My Activity Contract, its all that presenter need to know about view
 */
public interface IMyView {
    fun showAllUsers(users : List<User>)
}

/**
 * Just a simple data class
 */
data class User(var name : String = "", var age : Int = 0)

/**
 * Some simple data storage.
 */
class UserManager() {
    var users : ArrayList<User> = arrayListOf(
        User("Antoha", 18),
        User("Zhora", 44),
        User("Pafnutyi", 84),
        User("Kolyan", 23),
        User("Miha", 31)
    )

    fun add(user : User) {
        Thread.sleep(500)
        users.add(user)
    }

    fun getAll() : ArrayList<User> {
        Thread.sleep(1000) //need some sleep
        return users
    }

    fun getByIndex(index : Int) : User? {
        Thread.sleep(500) //need some sleep
        return if (users.size < index) users[index] else null
    }
}



```

3. RXjava2 используется конкретно так

```
Observable.create({...}) //создаем Observable
.observeOn(AndroidSchedulers.mainThread()) // Задаем исполнение кода подписчиков в главном потоке 
.subscribeOn(Schedulers.io()) // Задаем исполнение кода create({...}) в IO потоке
.subscribe {...} // Задаем калбэк исполняемый в Главном Потоке по вызову OnNext в Create({...})
```
