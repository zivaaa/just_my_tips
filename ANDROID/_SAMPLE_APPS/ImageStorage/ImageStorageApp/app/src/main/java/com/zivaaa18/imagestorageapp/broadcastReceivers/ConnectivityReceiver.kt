package com.zivaaa18.imagestorageapp.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zivaaa18.imagestorageapp.Tools

class ConnectivityReceiver(val connector : ConnectionListener) : BroadcastReceiver() {
    interface ConnectionListener {
        fun onNetConnected()
        fun onNetDisconnected()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (Tools.isOnline(context)) {
            connector.onNetConnected()
        } else {
            connector.onNetDisconnected()
        }
    }
}
