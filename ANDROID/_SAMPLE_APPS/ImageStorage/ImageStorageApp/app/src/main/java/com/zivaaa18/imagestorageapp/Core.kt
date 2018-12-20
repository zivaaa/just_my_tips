package com.zivaaa18.imagestorageapp

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import com.zivaaa18.imagestorageapp.broadcastReceivers.ConnectivityReceiver
import com.zivaaa18.imagestorageapp.db.DB
import com.zivaaa18.imagestorageapp.httpClient.Api
import com.zivaaa18.imagestorageapp.httpClient.ApiClient
import com.zivaaa18.imagestorageapp.models.AccessToken
import com.zivaaa18.imagestorageapp.models.Credentials
import com.zivaaa18.imagestorageapp.models.User
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class Core private constructor(val ctx: Context) : Observable() {

    data class Event(val code: Int)

    companion object {
        val TAG = "IMG_APP"

        val ERROR_ACCESS_DENIED = 1000
        val CONNECTED = 2000
        val DISCONNECTED = 2001
        val LOGOUT = 2002

        val REASON_INVALID_CREDENTIALS = "Invalid credentials"


        private var instance: Core? = null

        fun getInstance(ctx: Context): Core {
            if (instance == null) {
                instance = Core(ctx)
            }

            return instance!!
        }
    }


    override fun notifyObservers(arg: Any?) {
        setChanged()
        super.notifyObservers(arg)
    }

    val api: Api = ApiClient.api()

    val db: DB = DB.getInstance(ctx)

    private var accessToken: AccessToken? = null

    private var receiver = ConnectivityReceiver(object : ConnectivityReceiver.ConnectionListener {
        override fun onNetConnected() {
            Log.d(TAG, "connection established!")
            notifyObservers(Event(CONNECTED))
        }

        override fun onNetDisconnected() {
            Log.d(TAG, "connection lost")
            notifyObservers(Event(DISCONNECTED))
        }
    }).also {
        ctx.registerReceiver(it, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
    }


    fun isOnline(): Boolean {
        return Tools.isOnline(ctx)
    }

    fun login(credentials: Credentials, cb: ((Throwable?) -> Unit)) {
        api.login(credentials).enqueue(object : Callback<AccessToken> {
            override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                cb(t)
            }

            override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {
                if (response.code() != 200) {
                    cb(Throwable(REASON_INVALID_CREDENTIALS))
                } else {
                    authenticate(response.body()!!, {
                        cb(it)
                    })
                }

            }
        })
    }

    fun checkAuth(cb: ((success: Boolean) -> Unit), validateByApi: Boolean = false) {
        Log.d(TAG, "checkAuth")
        if (accessToken == null) {
            Log.d(TAG, "retrieve token and check")
            db.dao().getAccessToken().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe { token, err ->
                    if (token == null) {
                        Log.d(TAG, "cant find token, false")
                        cb(false)
                    } else {
                        Log.d(TAG, "token exists : ${token.id}")
                        setAccessToken(token)
                        if (validateByApi) {
                            checkTokenValid {
                                cb(it)
                            }
                        } else {
                            cb(true)
                        }

                    }
                }
        } else {
            Log.d(TAG, "already has token, true")
            cb(true)
        }
    }

    private fun checkTokenValid(cb: ((isValid: Boolean) -> Unit)) {
        Log.d(TAG, "checkTokenValid")
        api.me().enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d(TAG, "token is not valid, or error")
                cb(false)
            }

            override fun onResponse(call: Call<User>, response: Response<User>) {
                Log.d(TAG, "its ok go next")
                cb(true)
            }
        })

    }

    private fun authenticate(token: AccessToken, cb: ((Throwable?) -> Unit)) {
        Single.fromCallable {
            db.dao().deleteTokens()
        }.flatMap {
            db.dao().insert(token)
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe { _, err ->
                if (err == null) {
                    setAccessToken(token)
                }
                cb(err)
            }
    }

    private fun setAccessToken(token: AccessToken?) {
        this.accessToken = token
        ApiClient.setAccessToken(token)
    }

    fun logout() {
        Single.fromCallable {
            if (isOnline()) {
                val response = api.logout().execute()
                if (response.raw().code() < 300) { } else {}
            }
            db.dao().deleteTokens()
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                _, t ->
                setAccessToken(null)
                notifyObservers(Event(LOGOUT))
            }
    }
}