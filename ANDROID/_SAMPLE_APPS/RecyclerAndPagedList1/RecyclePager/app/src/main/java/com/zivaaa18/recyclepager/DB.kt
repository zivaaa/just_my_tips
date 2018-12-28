package com.zivaaa18.recyclepager

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.*
import android.content.Context
import androidx.annotation.NonNull
import java.util.concurrent.Executors

@Entity
class Product constructor(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @NonNull @ColumnInfo(name = "title") var name: String
)

@Dao
interface ProductDao {
    @Query("SELECT * FROM product")
    fun getAll(): List<Product>

    @Query("SELECT * FROM product LIMIT :limit OFFSET :offset")
    fun getProductsPaginated(offset : Int, limit : Int) : List<Product>

    @Insert
    fun insertAll(vararg products: Product)

    @Insert
    fun insertAll(products: List<Product>)

    @Update
    fun update(product: Product)

    @Delete
    fun delete(product: Product)
}

@Database(entities = [Product::class], version = 1)
abstract class MyDatabase : RoomDatabase() {
    abstract fun dao(): ProductDao

    companion object {
        val DATABASE_NAME = "room_pager"

        @Volatile
        private var dbMyDatabase: MyDatabase? = null

        @Synchronized
        fun getInstance(appContext: Context): MyDatabase {

            if (dbMyDatabase == null) {
                dbMyDatabase = Room.databaseBuilder(appContext, MyDatabase::class.java, DATABASE_NAME)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            Executors.newSingleThreadScheduledExecutor()
                                .execute {
                                    var products = mutableListOf<Product>()
                                    for (i in 0..30) {
                                        products.add(Product(null, "Baltica ${i}"))
                                    }
                                    getInstance(appContext).dao().insertAll(products)
                                }
                        }
                    }).build()
            }

            return dbMyDatabase!!

        }
    }
}
