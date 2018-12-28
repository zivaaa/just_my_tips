# Пагинация в recycle view (PagedList) 

Для того чтобы подгружать данные в RecyclerView постепенно, а не сразу все можно использовать библиотеку пагинации от гугла.

### Paging library

В библиотеке есть несколько сущностей, которые надо связать, чтобы использовать в RecyclerView.

- PagedListAdapter, адаптер для recyclerView, он выводит данные из PagedList
- PagedList, хранилище данных, к которому обращается recyclerView, данные запрашиваются из DataSource.
- DataSource, источник данных, который загружает их и отдает в PagedList.

Самый простой вариант использовать использовать LiveData, но я этого делать, конечно не буду.

Тут как примерно (не все тут может верно, но вроде работает) настроить чтоб самому управлять, данные пойдут из Room.

### Настройка и взаимодействие

build.grande

( для AndroidX !)

```
    def room_version = "2.1.0-alpha03"

    implementation "androidx.room:room-runtime:$room_version"
    kapt "androidx.room:room-compiler:$room_version"

    def paging_version = "2.1.0-rc01"

    implementation "androidx.paging:paging-runtime:$paging_version"

    // alternatively - without Android dependencies for testing
    testImplementation "androidx.paging:paging-common:$paging_version" 
```

Пишем Adapter. DIFF_CALLBACK нужет, чтобы отличать объекты.

```
   
    class ProductAdapter : PagedListAdapter<Product, ProductAdapter.ProductViewHolder>(DIFF_CALLBACK) {
        class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var product: Product? = null

            fun bindTo(product: Product?) {
                if (product == null) {
                    clear()
                } else {
                    this.product = product
                    itemView.findViewById<TextView>(R.id.productNameView).setText("id(${product.id}) | " + product.name)
                }
            }

            fun clear() {
                itemView.findViewById<TextView>(R.id.productNameView).setText("- EMPTY -")
                product = null
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            Log.d(TAG, "onCreateViewHolder : ${viewType}")
            val holder =
                ProductViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_item, parent, false))
            holder.bindTo(getItem(viewType))
            return holder
        }

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            val product = getItem(position)
            if (product != null) {
                holder.bindTo(product)
            } else {
                holder.clear()
            }
        }

        companion object {
            private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<Product>() {
                override fun areItemsTheSame(
                    oldproduct: Product,
                    newproduct: Product
                ): Boolean =
                    oldproduct.id == newproduct.id

                override fun areContentsTheSame(
                    oldproduct: Product,
                    newproduct: Product
                ): Boolean =
                    oldproduct == newproduct
            }
        }
    }
```

[Doc](https://developer.android.com/reference/android/arch/paging/DataSource)

Реализуем DataSource. Он может наследоваться от 3 разный типов. Делать будем так

```

    class ProductDataSource(var ctx: Context, var initialData: List<Product>?) : PageKeyedDataSource<Int, Product>() {
        override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Product>) {
            Log.d(TAG, "source : loadInitial")
            if (initialData == null) {
                Thread {
                    val products = MyDatabase.getInstance(ctx).dao().getProductsPaginated(0, params.requestedLoadSize)
                    callback.onResult(products, null, (products.size - 1))
                    Log.d(TAG, "source : on result")
                }.start()
            } else {
                val size = initialData!!.size
                callback.onResult(initialData!!, null, if (size > 0) initialData!!.size - 1 else 0)
            }

        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Product>) {
            Log.d(TAG, "source : loadAfter ${params.key} | ${params.requestedLoadSize}")

            val products = MyDatabase.getInstance(ctx).dao().getProductsPaginated(params.key, params.requestedLoadSize)

            val nextKey = if (products.size == 0) {
                invalidate()
                null
            } else {
                params.key + products.size
            }
            callback.onResult(products, nextKey)
            Log.d(TAG, "source : on loadAfter result")
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Product>) {
            Log.d(TAG, "source : loadBefore")
        }
    }
    
```

 - loadInitial, нужен для первоначальной загрузки данных. Тут я проверяю, либо уже какие-то данные уже есть, 
 либо надо грузить с бд.
 
 - loadAfter, вызывается при прокрутке списка. Тут надо загрузить данные и задать ключ последнего объекта, 
 с которого надо начать грузить данные в след. раз (если нечего грузить ставим null). В моем случае запрос следующий:
 
```

@Dao
interface ProductDao {
    @Query("SELECT * FROM product LIMIT :limit OFFSET :offset")
    fun getProductsPaginated(offset : Int, limit : Int) : List<Product>
}

```

Теперь надо создать PagedList, задать конфигурацию, задать DataSource и закинуть в адаптер.

```
    fun reConfigAdapter(products: List<Product>?) {
        Log.d(TAG, "reconfig adapter")

        //Создаем источник данных
        var source = ProductDataSource(applicationContext, products).also {
            it.addInvalidatedCallback {
                Log.d(TAG, "invalidated!")
            }
        }

        // Задаем конфигурацию
        var config = PagedList.Config.Builder().also {
            it.setPageSize(5)
            it.setInitialLoadSizeHint(5)
            it.setMaxSize(1000)
            it.setPrefetchDistance(5)
        }.build()


        //Создаем PagedList
        this.pagedList = PagedList.Builder<Int, Product>(source, config)
            .setFetchExecutor(backgroundExecutor)
            .setNotifyExecutor(mainThreadExecutor)
            .build()


        var pos = adapter.itemCount
        
        //Сабмитим в adapter.
        adapter.submitList(this.pagedList, {
            Log.d(TAG, "on list submit")
            if (pos > 0) {
                recycler.scrollToPosition(pos - 1)
            }

        })
    }

```

Тут происходит следующее:
 - создается источник данных
 - с калбэком на invalidate (просто тречу это событие, тут ничего не происходит, invalidate() надо вызывать из DataSource если грузить нечего, или что-то пошло не так)
 - создается конфигурация в которой указываются значения загрузки данных (по сколько грузить)
 - создается pagedList
 - задается .setFetchExecutor(backgroundExecutor), это фоновый поток, в котором выполняется loadAfter
 - задается .setNotifyExecutor(mainThreadExecutor), это ui поток, в котором приходят результаты и выводятся в recyclerView
 - !! при это loadInitial выполняется в главном потоке !!
 - далее определяется количество уже созданных объектов (чтоб автоматом скролить вниз) и сабмитится PagedList
 
 Метод reConfigAdapter(products: List<Product>?) я сделал именно таким, чтобы можно было регировать на изменение данных. 
 Например при добавлении или удалении. Иначе по окончании списка, подгружаться больше ничего не будет, а чтобы это сработало 
 нужно, насколько я понял, создать новый DataSource и PagedList. Но новый PagedList не будет именть данных, поэтому я сделал 
 ``` ProductDataSource(applicationContext, products) ```, который принимает старый данные из предыдущего PagedList. 
 
 Такая вот фиговина.
 
 
 
 Теперь можно настроить RecyclerView
 
```

    var pagedList: PagedList<Product>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        adapter = ProductAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter


        //...

        reConfigAdapter(pagedList)
    }
```

Этот аляповатый пример с кучей логов [где-то тут](https://github.com/zivaaa/just_my_tips/tree/master/ANDROID/_SAMPLE_APPS)
