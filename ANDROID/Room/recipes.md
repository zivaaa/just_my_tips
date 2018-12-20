# Всякие заметки по Room

### Как удалить последний элемент в таблице

```

    @Query("DELETE FROM page WHERE id IN (Select id from page order by id DESC limit 1)")
    fun deleteLast()

```


### Как сделать простую обертку над Room Database

```


class Repo(private val ctx : Context) {

    private var pages : LiveData<List<Page>> = MyDatabase.getInstance(ctx).pageDao().getAllLive()

    interface Callback<T> {
        fun call(data : T)
    }

    fun getPages() : LiveData<List<Page>> {
        return pages
    }

    fun savePage(page : Page) {
        Executors.newSingleThreadScheduledExecutor().execute {
            MyDatabase.getInstance(ctx).pageDao().update(page)
        }
    }

    fun getArticlesByPageLive(pageId : Int) : LiveData<List<Article>> {
        return MyDatabase.getInstance(ctx).articleDao().getAllByPageIdLive(pageId)
    }

    fun createArticle(article : Article) {
        Executors.newSingleThreadScheduledExecutor().execute {
            MyDatabase.getInstance(ctx).articleDao().insert(article)
        }
    }

    fun createOne() {
        Executors.newSingleThreadScheduledExecutor().execute {
            MyDatabase.getInstance(ctx).pageDao().insertAll(Page(null, "new One", "Just a description of new One", false))
        }
    }

    fun deleteLastPage() {
        Executors.newSingleThreadScheduledExecutor().execute {
            MyDatabase.getInstance(ctx).pageDao().deleteLast()
        }
    }

    fun getPageArticle(cb : Callback<List<PageArticles>>) {
        Executors.newSingleThreadScheduledExecutor().execute {
            cb.call(MyDatabase.getInstance(ctx).articleDao().getPageArticles())
        }
    }

}

//... Создаем  в MainActivity

val repo = Repo(this.applicationContext)

//... Используем

repo.getPageArticle(object : Repo.Callback<List<PageArticles>> {
    override fun call(data: List<PageArticles>) {
        Log.d(TAG, "yeee") // Fetched in other thread!
    }
})

//... Слушаем изменения

repo.getPages().observe(this, Observer {
    Log.d(TAG, "pages updated")
    setPages(it!!)
})

```

### Как слушать события по изменению массива дочерних элементов.

```

    //..DAO

    @Query("SELECT * FROM article WHERE pageId = :pageId")
    fun getAllByPageIdLive(pageId : Int): LiveData<List<Article>>

    //... Main Activity

    repo.getArticlesByPageLive(currentPage?.id!!).observe(this, Observer {
        onGetArticle(it!!)
    })

```

