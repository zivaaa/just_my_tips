package com.zivaaa18.chattyapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder


/**
 * Service handling class.
 */
abstract class ServiceHolder<T : Context>(val ctx : T) : ConnectionEventObserver.EventListener, ServiceConnection {

    var mBound : Boolean = false
    var service : ConnectionService? = null

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
        mBound = false
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (service == null) {
            return
        }

        this.service = (service as ConnectionService.ConBinder).getService()

        mBound = true

        this.service!!.eventObserver.addEventListener(this)
    }

    fun startConnectionService() {
        ctx.bindService(Intent(ctx, ConnectionService::class.java), this, Context.BIND_AUTO_CREATE)
    }


    fun stopConnectionService() {

    }

    fun unBindConnectionService() {
        if (mBound) {
            this.service!!.eventObserver.removeEventListener(this)
            ctx.unbindService(this)
            mBound = false
        }
    }
}