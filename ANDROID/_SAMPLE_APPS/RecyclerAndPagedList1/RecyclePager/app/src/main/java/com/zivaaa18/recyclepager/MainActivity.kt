package com.zivaaa18.recyclepager

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors
import android.os.Looper
import android.widget.ImageButton
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity() {
    lateinit var adapter: ProductAdapter
    var backgroundExecutor = Executors.newFixedThreadPool(5);

    companion object {
        val TAG = "APP_TAG"
    }

    var mainThreadExecutor = object : Executor {
        private val mHandler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mHandler.post(command)
        }
    }

    var pagedList: PagedList<Product>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        adapter = ProductAdapter(object : OnDelete {
            override fun onDelete(product: Product) {
                Log.d(TAG, "deleting ${product.id}")
                if (product.id != null) {
                    backgroundExecutor.execute {
                        MyDatabase.getInstance(this@MainActivity).dao().delete(product)
                        mainThreadExecutor.execute {
                            onDeleted(product.id)
                        }
                    }
                }

            }
        })
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter


        btnAdd10.setOnClickListener {
            backgroundExecutor.execute {
                Log.d(TAG, "add 10");
                var products = mutableListOf<Product>()

                for (i in 0..5) {
                    products.add(Product(null, "Baltica #E${i}"))
                }

                MyDatabase.getInstance(this@MainActivity.applicationContext).dao().insertAll(
                    products
                )

                mainThreadExecutor.execute {
                    onChanged()
                }
            }
        }

        reConfigAdapter(pagedList)
    }

    fun onChanged() {
        reConfigAdapter(adapter.currentList)
    }

    fun onDeleted(vararg ids: Long) {
        Log.d(TAG, "onDeleted ${ids.toString()}")
        val products = pagedList!!.snapshot().toMutableList()
        for (id in ids) {
            for ((index, value) in products.withIndex()) {
                if (value?.id == id) {
                    products.removeAt(index)
                    break
                }
            }
        }

        reConfigAdapter(products)
    }

    fun reConfigAdapter(products: List<Product>?) {
        Log.d(TAG, "reconfig adapter")

        var source = ProductDataSource(applicationContext, products).also {
            it.addInvalidatedCallback {
                Log.d(TAG, "invalidated!")
            }
        }

        var config = PagedList.Config.Builder().also {
            it.setPageSize(5)
            it.setInitialLoadSizeHint(5)
            it.setMaxSize(1000)
            it.setPrefetchDistance(5)
        }.build()



        this.pagedList = PagedList.Builder<Int, Product>(source, config)
            .setFetchExecutor(backgroundExecutor)
            .setNotifyExecutor(mainThreadExecutor)
            .build()


        var pos = adapter.itemCount
        adapter.submitList(this.pagedList, {
            Log.d(TAG, "on list submit")
            if (pos > 0) {
                recycler.scrollToPosition(pos - 1)
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    interface OnDelete {
        fun onDelete(product: Product)
    }


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


    class ProductAdapter(val onDelete: OnDelete) :
        PagedListAdapter<Product, ProductAdapter.ProductViewHolder>(DIFF_CALLBACK) {
        class ProductViewHolder(
            view: View,
            val onDelete: OnDelete
        ) : RecyclerView.ViewHolder(view) {
            var product: Product? = null

            init {
                view.findViewById<ImageButton>(R.id.deleteBtn).setOnClickListener {
                    if (product != null) {
                        onDelete.onDelete(product!!)
                    }
                }
            }

            fun bindTo(product: Product?) {
                if (product == null) {
                    clear()
                } else {
                    this.product = product
                    Log.d(TAG, "bind To ${product.id}")
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
                ProductViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.holder_item, parent, false),
                    object : OnDelete {
                        override fun onDelete(product: Product) {
                            onDelete.onDelete(product)
                        }
                    })
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
                // product details may have changed if reloaded from the database,
                // but ID is fixed.
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


}
