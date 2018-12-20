package com.zivaaa18.chattyapp

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import org.json.JSONObject

class ConnectionService() : Service(), ConnectionEventObserver.EventListener {
    companion object {
        val TAG = "CHATTY_SERVICE"

        private var running : Boolean = false

        /**
         * Check service is running
         */
        fun isRunning() : Boolean {
            return running
        }

        /**
         * Mark service as running or not
         */
        private fun setRunning(flag : Boolean) {
            running = flag
        }
    }

    /**
     * Socket wrapper class
     */
    lateinit var client : Connector

    /**
     * If something happened with socket, we will receive event.
     * Socket events all are in other threads. We need to make them in MainThread.
     * Use Handler for that
     *
     */
    override fun onEvent(eventName: String, json: JSONObject?) {
        Log.d(TAG, "thread ${Thread.currentThread().name}")
        handler.post {
            Log.d(TAG, "event ${eventName} fired")
            Log.d(TAG, "thread ${Thread.currentThread().name}")
            eventObserver.notifyAboutEvent(eventName, json)
        }
    }

    /**
     * Same as for event. But for errors. Handle them in MainThread
     */
    override fun onError(type: Int, message: String) {
        handler.post {
            Log.d(TAG, "error '${message}' has been catch!")
            eventObserver.notifyAboutError(type, message)
        }
    }

    /**
     * Used to handle socket events
     */
    lateinit var handlerThread : HandlerThread

    /**
     * Handler to make calls in MainThread
     */
    lateinit var handler : Handler

    /**
     * I dont want to use socket callbacks directly.
     * I need some abstraction level.
     * Well it is. Just an observer.
     */
    val eventObserver : ConnectionEventObserver = ConnectionEventObserver()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "service creating ${Thread.currentThread().name}")

        // setup thread handlers
        handlerThread = HandlerThread("ConnectionService")
        handlerThread.start()

        //set main thread looper to be sure that callbacks can be used safely
        handler = Handler(this.mainLooper)


        //Create Connector and make it connected.
        client = Connector().also {
            it.addEventListener(this)
            it.connect()
        }

        //just to know that service is running
        setRunning(true)
    }

    /**
     * Reconnection method
     */
    fun reconnect() {
        client.disconnect()
        client.connect()
    }

    /**
     * We need to return Binder to link service and activity.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return this.binder
    }

    /**
     * Method to be used by activity.
     * Will call events on server and maybe bring back some result.
     */
    fun emit(event : String, data : Any, cb : Connector.OnAckEvent? = null) {
        this.client.emit(event, data, if (cb != null) object : Connector.OnAckEvent {
            override fun call(data: Array<Any>) {
                handler.post {
                    cb.call(data)
                }
            }
        } else { null })
    }

    fun isConnected() : Boolean {
        return client.isConnected()
    }


    /**
     * Will be fired when no binders left.
     * Destroy conenction and handlers
     */
    override fun onDestroy() {
        handlerThread.quit()
        client.disconnect()
        Log.d(TAG, "service destroyed ${Thread.currentThread().name}")
        setRunning(false)
        client.removeEventListener(this)
    }

    fun getSocketId() : String {
        return client.client.id()
    }


    /* Binder class and field*/
    var binder : IBinder = ConBinder()

    inner class ConBinder : Binder() {
        fun getService() : ConnectionService {
            return this@ConnectionService
        }
    }
}