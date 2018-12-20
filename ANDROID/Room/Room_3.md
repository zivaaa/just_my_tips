# Room, TypeConverters, Many-To-Many, RxJava

В прошлый раз был Room + LiveData. Теперь Room + RxJava. В этой заметке

 - RXjava2

 - Как можно сделать связку многие-ко-многим (many-to-many)

 - TypeConverters (конверторы типов)


### Настройка проекта

Делаем в AndroidX, для этого надо переконверовать проект под AndroidX.

```

Refactor -> Migrate To AndroidX...


```

Добавляем зависимости:


```

...
apply plugin: 'kotlin-kapt'
...


dependencies {
    ...

    def room_version = "2.1.0-alpha03"

    implementation "androidx.room:room-runtime:$room_version"
    kapt "androidx.room:room-compiler:$room_version"

    // optional - RxJava support for Room
    implementation "androidx.room:room-rxjava2:$room_version"

    def rx_version = "2.2.4"
    implementation "io.reactivex.rxjava2:rxjava:$rx_version"

    implementation 'io.reactivex.rxjava2:rxandroid:2.1.0'
}


```

### БД

Настраиваем БД, для этого создадим 3 модели, Dao и DB.
Пусть будет что-то типа магазинчика.

```

@Entity
class Product(
    @PrimaryKey var id: Long?,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "price") var price: Long
)

@Entity
class Person(
    @PrimaryKey var id: Long?,
    @ColumnInfo(name = "fullName") var fullName: String
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = CASCADE
        )
    ]
)
class Order(
    @PrimaryKey var id: Long?,
    @ColumnInfo(name = "productId") var productId: Long,
    @ColumnInfo(name = "personId") var personId: Long,
    @Nullable var createdAt: Date,
    @Nullable var completedAt: Date
)

```

Модели:
 - Person, покупатель
 - Product, товар
 - Order, заказ. Связка меду Person и Product. Поэтому здесь внешние ключи.

 Также в Order указаны поля типа Date. В такой тип БД не умеет, поэтому надо его подсказать что с ним делать. Для этого есть
 TypeConverter. Это классы с методами хелперами которые room знает когда применить. Вообще цель простая - конвертировать типы.

 У нас есть Date, а в БД такой тип неприемлем, надо обрабатывать. Для этого надо конвертор объявить и указать.

 ```

class TimeStampConverter {
    @TypeConverter
    fun toDate(dateLong: Long): Date? {
        return if (dateLong == null) null else Date(dateLong)
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
}

 ```

 Этот конвертор имеет два метода, аннотированных ``` @TypeConverter ```.
 Их room и будет использовать для конвертации типов.

 Теперь надо указать где-использовать конверторы, это можно делать как у конвертируемых полей, так и на уровне сущности или БД.

```

// Так
class Order(
    ...
    @TypeConverters(TimeStampConverter::class) @Nullable var createdAt: Date,
    ...
)


// Или Так

@TypeConverters(TimeStampConverter::class)
class Order(
    ...
)

// Или даже так
@TypeConverters(TimeStampConverter::class)
@Database(entities = [Product::class, Person::class, Order::class], version = 1)
abstract class DB : RoomDatabase() {
    ...
}


```

Я на уровне БД оставил, чтобы не описывать это каждый раз.

### Dao, DB, Many-To-Many

Описываем Dao и BD. Каждый метод Dao должен возвращать объект RxJava, который можно будет использовать для выплнения запросов в новых потоках и не только.

```

@TypeConverters(TimeStampConverter::class)
@Database(entities = [Product::class, Person::class, Order::class], version = 1)
abstract class DB : RoomDatabase() {
    abstract fun storeDao(): StoreDao

    companion object {
        val TAG = "ROOM_APP"

        @Volatile
        private var instance: DB? = null

        @Synchronized
        fun getInstance(ctx: Context): DB {
            if (instance == null) {
                instance = Room.databaseBuilder(ctx, DB::class.java, "awesome_store")
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            Log.d(TAG, "onCreate db")
                            prepopulate(ctx)
                        }
                    }).build()
            }
            return instance!!
        }

        private fun prepopulate(ctx: Context) {
            Log.d(TAG, "db populating")
            //...
        }
    }
}

@Dao
interface StoreDao {
    @Query("select * from product")
    fun getProducts(): Flowable<List<Product>>

    @Query("select * from person")
    fun getPersons(): Flowable<List<Person>>

    @Insert
    fun insert(person: Person): Single<Long>

    @Insert
    fun insert(product: Product): Single<Long>

    @Insert
    fun insert(vararg person: Person): Single<List<Long>>

    @Insert
    fun insert(vararg products: Product): Single<List<Long>>

    @Query("select * from `order`")
    fun getOrders(): Single<List<Order>>

    @Insert
    fun insert(order: Order): Single<Long>

    @Insert
    fun insert(vararg orders: Order): Single<List<Long>>

    @Query("SELECT * from person")
    fun getPersonOrders() : Single<PersonOrders>

    @Query("select :personId as currentPersonId, product.name, product.price, `order`.id as orderId, `order`.createdAt as orderCreatedAt from product " +
            "inner join `order` on `order`.personId = product.id and `order`.productId " +
            "where product.id = :personId")
    fun getPersonProducts(personId : Long) : Single<List<PersonProducts>>
}

```

БД как обычно - Singleton.

В Dao можно указывать основные RX сущности (Observable, Flowable, Single, Maybe ...) как возвращаемое значения вызовов методов. Я указал Single и Flowable.
В основном Single, потомучто возвращать буду единственный объект, будь то модель или список моделей.

Использувать это можно следующим образом:

```

override fun onActivityCreated(savedInstanceState: Bundle?) {
    //...
    DB.getInstance(this.context!!).storeDao().getPersons()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe { lst ->
            adapter.setData(lst)
        }
    //...
}

```

Я подписался на обновления ``` getPersons() ```. а результат обновления использую в адаптере.
Если где-то в другом месте создать новую запись Person, адаптер также обновится.

```

//...
findViewById<Button>(R.id.buttonAdd).setOnClickListener {
    DB.getInstance(this.context!!).storeDao().insert(Person(null, "Test Name"))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe()
}
//...

```

### Relations (отношения). Many-to-many, one-to-many

Товары (Product) связаны с покупателями (Person) через таблицу заказов (Order).
Это связь многие-ко-многим. Чтобы получить данные о товарах, заказанных покупателем
можно сделать следующее

```

//...
    @Query("select :personId as currentPersonId, product.name, product.price, `order`.id as orderId, `order`.createdAt as orderCreatedAt from product " +
            "inner join `order` on `order`.personId = product.id and `order`.productId " +
            "where product.id = :personId")
    fun getPersonProducts(personId : Long) : Single<List<PersonProducts>>
//...

//...
class PersonProducts() {
    var currentPersonId : Long? = null
    var price : Long? = null
    var orderId : Long? = null
    var name : String? = ""
    var orderCreatedAt : Date? = null
}
//...


```

Сделать запрос с inner join по таблицам.
Создать класс, поля которого соответствуют данным указанным в ``` select ```. (тип Date обработает TypeConverter)

Таким образом получим объект, который включает в себя : id покупателя, id заказа, цену товара, название товара и когда заказ был создан.

А теперь где-нибудь в коде активити:

```

        DB.getInstance(this).storeDao().getPersonProducts(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe { l,t->
                // l is our List<PersonProduct>
            }

```

И все.

Еще можно получить просто список заказов. Это отношение один-ко-многим (one-to-many).

Для этого надо создать сущность и указать ``` @Relation ```, можно указать полностью вывод Person через ``` @Embedded ```.
И сам запрос

```

//...
    @Query("SELECT * from person")
    fun getPersonOrders() : Single<PersonOrders>
//...


class PersonOrders() {
    @Embedded
    lateinit var person: Person

    @Relation(parentColumn = "id", entityColumn = "personId")
    lateinit var orders: List<Order>
}

```

### RXJava population (наполнение БД)

Наполнить БД данными может быть проблемно, первое что пришло в голову это вот такое вот спагетти:

```

abstract class DB : RoomDatabase() {
    abstract fun storeDao(): StoreDao

    companion object {
        //...

        private fun prepopulate(ctx: Context) {
            Log.d(TAG, "db populating")

            getInstance(ctx).storeDao().insert(
                Person(null, "Sasha Kuricin"),
                Person(null, "Arnold Totsamiy")
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { personsIds, th ->
                    Log.d(TAG, "persons ready")
                    getInstance(ctx).storeDao().insert(
                        Product(null, "pelmeni", 5),
                        Product(null, "apple juice", 3),
                        Product(null, "vodka", 7),
                        Product(null, "fruit salad", 8)
                    ).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { produtsIds, th ->
                            Log.d(TAG, "products ready")
                            getInstance(ctx).storeDao().insert(
                                Order(null, produtsIds[0], personsIds[0], System.currentTimeMillis(), 0),
                                Order(null, produtsIds[2], personsIds[0], System.currentTimeMillis(), 0),
                                Order(null, produtsIds[2], personsIds[0], System.currentTimeMillis(), 0),
                                Order(null, produtsIds[2], personsIds[0], System.currentTimeMillis(), 0),

                                Order(null, produtsIds[1], personsIds[1], System.currentTimeMillis(), 0),
                                Order(null, produtsIds[1], personsIds[3], System.currentTimeMillis(), 0)
                            ).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { list, th ->
                                    Log.d(TAG, "orders ready")
                                }
                        }
                }
        }
    }
}

```

Но RxJava имеет метод flatMap, который можно цепочить и конвертировать типы. Поэтому можно переписать в нечто более приличное:

```

//...
private fun prepopulate(ctx: Context) {
            Log.d(TAG, "db populating")

            lateinit var personsIds: List<Long>
            lateinit var produtsIds: List<Long>
            lateinit var orderIds: List<Long>

            var dao = getInstance(ctx).storeDao()

            dao.insert(
                Person(null, "Sasha Kuricin"),
                Person(null, "Arnold Totsamiy")
            ).flatMap {
                personsIds = it
                dao.insert(
                        Product(null, "pelmeni", 5),
                        Product(null, "apple juice", 3),
                        Product(null, "vodka", 7),
                        Product(null, "fruit salad", 8)
                )
            }.flatMap {
                produtsIds = it
                dao.insert(
                    Order(null, produtsIds[0], personsIds[0], Date(System.currentTimeMillis()), Date(0)),
                    Order(null, produtsIds[2], personsIds[0],  Date(System.currentTimeMillis()), Date(0)),
                    Order(null, produtsIds[2], personsIds[0],  Date(System.currentTimeMillis()), Date(0)),
                    Order(null, produtsIds[2], personsIds[0],  Date(System.currentTimeMillis()), Date(0)),

                    Order(null, produtsIds[1], personsIds[1],  Date(System.currentTimeMillis()), Date(0)),
                    Order(null, produtsIds[1], personsIds[1],  Date(System.currentTimeMillis()), Date(0))
                )
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                        lst, th ->
                        orderIds = lst
                }
}
//...

```

На этом финиш.