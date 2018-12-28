# Room Relations (Связи)

Модели часто связаны между собой. Чтобы обрабатывать получать связанные данные есть несколько способов.

В данном случае я использую связь "один-ко-многим" для моделей Page и Article.
Одна Page может иметь множество Article.

```

@Entity(
    primaryKeys = ["id"],
    tableName = "page"
)
class Page constructor(
    val id: Int? = null,
    @NonNull @ColumnInfo(name = "title") var title: String,
    @NonNull @ColumnInfo(name = "description") var description: String,
    @NonNull @ColumnInfo(name = "valid") var valid: Boolean
) : Serializable {}

//...

@Entity
class Article(
    @PrimaryKey var id: Int? = null,
    @ColumnInfo(name = "pageId") var pageId: Int,
    @ColumnInfo(name = "content") var content : String
)


```


### Прямой запрос

Пример:

Есть ViewPager который показывает фрагменты демонстрирующие Page. Фрагмент также имеет список Article, который выводится юзеру.

Можно создать в Dao метод возвращающий LiveData со списком Articles, отфильтрованными по Page.id.

```


@Dao
interface ArticleDao {
    //...

    @Query("SELECT * FROM article WHERE pageId = :pageId")
    fun getAllByPageIdLive(pageId : Int): LiveData<List<Article>>

    //...
}

```

Теперь надо просто во фрагменте подписаться на эти данные и все. Каждый фрагмент будет сам контроллировать изменения,
в том числе если будет создан новый.

```
//...

repo.getArticlesByPageLive(currentPage?.id!!).observe(this, Observer {
    onGetArticle(it!!)
})

//...

```
<img src="https://github.com/zivaaa/just_my_tips/src/master/ANDROID/Room/screenshots/scr1.png" width="25%">

<img src="https://github.com/zivaaa/just_my_tips/src/master/ANDROID/Room/screenshots/scr2.png" width="25%">


### Через Relation

``` @Relation ``` это аннотация которая применяется на отдельном классе (НЕ ВНУТРИ Entity!) и задает выборку связанных данных.

Сделаем PageArticle класс, который должен включать в себя Page и список Article одновременно

```


class PageArticles() {
    var id : Int = -1

/*    @Relation(parentColumn = "id", entityColumn = "pageId") */
    @Relation(entity = Article::class, parentColumn = "id", entityColumn = "pageId")
    lateinit var articles : List<Article>
}


```

Переменные нельзя писать в конструкторе, потомучто сначала создается объект, а потом уже заполняется данными.

Этот класс предоставляет поля

 - id -> идентификатор Page
 - articles -> список связанных Article

 В скобках написан сокращенный вариант аннотации, в ней не указывается тип элементов списка, хотя тут это не важно. Room поймет с чем имеем дело.

 Теперь опишем метод Dao

 ```
    //...

    @Query("SELECT id from page")
    fun getPageArticles() : List<PageArticles>

    //...

 ```

 Он задает вывод поля id принадлежащего Page, а список получит, как указано в ``` @Relation ```


 Можно также получить сразу моделль Page и список Article. Для этого надо переписать PageArticle и Dao метод.

 ```

 class PageArticles() {
     @Embedded
     lateinit var page : Page

     @Relation(entity = Article::class, parentColumn = "id", entityColumn = "pageId")
     lateinit var articles : List<Article>
 }

 //...

 @Query("SELECT * from page")
 fun getPageArticles() : List<PageArticles>

 ```

 Надо только указать аннотацию ``` @Embedded ``` надо полем page, и в запросе вывести все поля таблицы.

 получение данных будет также как и везде

 ```

    Executors.newSingleThreadScheduledExecutor().execute {
        val data = MyDatabase.getInstance(ctx).articleDao().getPageArticles()
    }

 ```

Если не нужно возвращать целые объекты, можно указать возврат нужного поля через атрибут ``` projection```


```

class PageArticlesContent() {
    var id : Int = -1
    @Relation(entity = Article::class, parentColumn = "id", entityColumn = "pageId", projection = ["content"])
    lateinit var articles : List<String>
}

```

### Foreign Keys (Внешние ключи)

Внешние ключи обеспечивают ссылочную целостность, отсюда и ограничение - таким полям абы какое значение не установишь.
Описываются следующим образом:

```

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Page::class,
            parentColumns = ["id"],
            childColumns = ["pageId"],
            onDelete = CASCADE
        )
    ]
)
class Article(
    @PrimaryKey var id: Int? = null,
    @ColumnInfo(name = "pageId") var pageId: Int,
    @ColumnInfo(name = "content") var content : String
)

```

entity - таблица на которую нужна ссылка;

parentColumns - поле таблицы на которую ссылаемся (page.id);

childColumns - поле таблицы которая ссылается (article.pageId);

onDelete - что делать в случае удаления записи на которую ссылаемся, в данном случае - удаление записи

В случае необходимости миграции, придется написать код миграции, например так:

```

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `Article` " +
                        "(" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "`pageId` INTEGER NOT NULL, " +
                        "`content` TEXT NOT NULL, " +
                        " FOREIGN KEY (pageId) REFERENCES page (id) ON DELETE CASCADE" +
                        ");"
                )
            }
        }

```
