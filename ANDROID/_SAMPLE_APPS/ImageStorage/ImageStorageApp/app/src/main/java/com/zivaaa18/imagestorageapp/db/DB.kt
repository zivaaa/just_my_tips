package com.zivaaa18.imagestorageapp.db

import android.content.Context
import android.util.Log
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zivaaa18.imagestorageapp.models.AccessToken
import com.zivaaa18.imagestorageapp.models.SharedImage
import io.reactivex.Single

@Dao
interface ImgDao {

    /* access */

    @Query("select * from accesstoken")
    fun getAccessToken() : Single<AccessToken?>

    @Insert
    fun insert(token : AccessToken) : Single<Unit>

    @Query("delete from accesstoken")
    fun deleteTokens()

    /* Images */

//    @Query("select * from sharedimage")
//    fun getImages() : Single<List<SharedImage>>
//
//    @Insert
//    fun insert(vararg image : SharedImage) : Single<List<Long>>
//
//    @Delete
//    fun delete(vararg image : SharedImage) : Single<List<Long>>
}

@Database(entities = [AccessToken::class], version = 1)
abstract class DB : RoomDatabase() {

    abstract fun dao() : ImgDao

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
                        }
                    }).build()
            }
            return instance!!
        }
    }
}