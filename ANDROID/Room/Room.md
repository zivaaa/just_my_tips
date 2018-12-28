# Room

Room - архитектурный компонент, уменьшающий объем шаблонного кода при работе с SqlLite

### Пример работы с Kotlin

Описываем зависимости в build.grandle

```

...
apply plugin: 'kotlin-kapt'
...

dependencies {
    ...

    def room_version = "1.1.1"
    implementation "android.arch.persistence.room:runtime:$room_version"
    kapt "android.arch.persistence.room:compiler:$room_version"

}


```

Чтобы создать БД с которой можно взаимодействовать, нужно как минимум три компонента, которые описываются аннотациями:

 - ``` @Entity ``` модель данных

 - ``` @Dao ``` (data transfer object) инструмент взаимодейтвия с моделями

 - ``` @Database ``` БД, включающая реализации Dao

 ### Entity

 Модель, задает название таблицы и ее поля.

 id в данном случае указано как nullable, чтобы при создании бд сама сгенерировала нам id.

 ```

@Entity
class Page constructor(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @NonNull @ColumnInfo(name = "title") var title: String,
    @NonNull @ColumnInfo(name = "description") var description: String,
    @NonNull @ColumnInfo(name = "valid") var valid: Boolean
)

 ```


 Можно указать имя таблицы и ключи. Primary Key должен быть указан либо через Аннотацию ``` @PrimaryKey ``` (см. выше)
 либо через ``` @Entity( primaryKeys = [] ) ``` (см. ниже), но не одновременно

 ```

@Entity(
    primaryKeys = ["id"],
    tableName = "page"
)

 ```

Работать с моделью будем через Dao

### Dao

Предоставляет все необходимые методы для работы с моделями

```

@Dao
interface PageDao {
    @Query("SELECT * FROM page")
    fun getAll(): List<Page>

    @Query("SELECT * FROM page")
    fun getAllLive(): LiveData<List<Page>>

    @Query("SELECT * FROM page WHERE id IN (:pageIds)")
    fun loadAllByIds(pageIds: IntArray): List<Page>

    @Insert
    fun insertAll(vararg pages: Page)

    @Update
    fun update(page : Page)
	
    @Delete
    fun delete(page: Page)
}

```

Все методы нужно описывать через аннотации, аргументы можно считывать из функции и указывать в sql-запросе.

Методы могу возвращать, как обычные массивы данных ( ``` getAll() ``` ) так и LiveData ( ``` getAllLive() ``` ),
на которые можно подписаться и получать результат где необходимо. Можно подключить и RxJava, а потом получать Flowable или Observable,
только нужно добавить в зависимости соответствующий модуль (например ``` android.arch.persistence.room:rxjava2:1.0.0 ```).


Dao сам по себе интерфейс и его нужно реализовать, чтобы использовать, эти займется следующий компонент.

### Database

Абстрактный класс, который содержит статический метод создания собственной реализации (можно сделать и через отдельный класс если надо).
В аннотации перечисляем все модели и задаем версию БД (для будущих миграций)

При создании надо предоставить билдеру реализуемый класс БД, а также имя БД.

Класс должен иметь абстрактные методы получения Dao.

```

@Database(entities = [Page::class], version = 1)
abstract class MyDatabase : RoomDatabase() {
    abstract fun pageDao(): PageDao

    companion object {
        val DATABASE_NAME = "room_example"

        @Volatile private var dbMyDatabase: MyDatabase? = null

        @Synchronized
        fun getInstance(appContext: Context): MyDatabase {

            if (dbMyDatabase == null) {
                dbMyDatabase = Room.databaseBuilder(appContext, MyDatabase::class.java, DATABASE_NAME).build()
            }

            return dbMyDatabase!!

        }
    }
}

```

DB создается как singleton, чтобы не создавать ее лишний раз и тем самым улучшить производительность.

На заметку:

Так как вызовы бд могут быть небыстрыми, бд будет требовать отдельный поток для каждого поэтому
* все вызовы связанные с Dao должны происходить в отличных от MainThread потоках *.
(но можно и разрешить конечно добавив вызов ``` .allowMainThreadQueries() ``` в билдере)



### Database наполнение при создании

Если надо, чтобы бд была заполнена стартовыми значениями, можно повесить обработчик обратных вызовов, например вот так

```

//...

dbMyDatabase = Room.databaseBuilder(appContext, MyDatabase::class.java, DATABASE_NAME)
    .addCallback(object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            Log.d(TAG, "database created, now populate!")

            Executors.newSingleThreadScheduledExecutor()
                .execute {
                    getInstance(appContext).pageDao().insertAll(
                        Page(null, "test", "test", false),
                        Page(null, "test 2", "test 2", false),
                        Page(null, "test 3", "test 3", false)
                    )
                    Log.d(TAG, "database is populated!")
                }
        }
    })
    .build()

//...

```

Теперь после создания БД (а не каждый раз призапуске приложения!) выполнится, код который в отдельном потоке сделает 3 экземпляра Page.

Тут позникает проблема:

Если код выполняется при запуске приложения в первый раз, а данные нужны прямо при настройке активити,
то можно получить ситуацию, когда в активити происходит получение данных,
а код наполнения еще не выполнен, то есть получим пустой массив данных.

Решить проблему можно используя LiveData.

```

@Dao
interface PageDao {

    //...
    @Query("SELECT * FROM page")
    fun getAllLive(): LiveData<List<Page>>
    //...

}

```

В нужном месте (в данном случае активити) получаем LiveData и подписываемся на обновления

```

var pages = MyDatabase.getInstance(this).pageDao().getAllLive()
pages.observe(this, Observer {
    Log.d(TAG, "pages updated")
    //setPages(it!!)
})

```

Теперь не приходится гадать, когда данные будут заполнены, на обновления данных существует подписка.

### Миграции

Когда придет время менять структуру бд или добавлять таблицы, нужно реализовывать миграции. Обязательно!

Для этого надо будет внести изменения, а потом изменить номер версии бд

``` 

 @Database(entities = [Page::class, Article::class], version = 2)
 
```

В данном случае был переход от 1 ко 2 версии. Значит миграция должна быть создана именно для этого перехода.

Вот пример

``` 

@Database(entities = [Page::class, Article::class], version = 2)
abstract class MyDatabase : RoomDatabase() {

	//...

    companion object {
		//...

        @Synchronized
        fun getInstance(appContext: Context): MyDatabase {

            if (dbMyDatabase == null) {
                dbMyDatabase = Room.databaseBuilder(appContext, MyDatabase::class.java, DATABASE_NAME)
                    .addCallback(object : RoomDatabase.Callback() {
                        //...
                    }).addMigrations(MIGRATION_1_2)
                    .build()
            }

            return dbMyDatabase!!

        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `Article` " +
                        "(" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "`content` TEXT NOT NULL " +
                        ");"
                )
            }
        }
    }
}


```

В данном случае я добавляю новую таблицу Article.

Теперь при следующем запуске приложения, эта миграция будет выполнена и текущая версия БД изменится.