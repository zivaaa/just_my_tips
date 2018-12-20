package com.zivaaa18.chattyapp

import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class Connector() {

    interface OnAckEvent {
        fun call(data: Array<Any>)
    }

    val eventObserver = ConnectionEventObserver()

    companion object {
        val CONNECTION_HOST: String = "http://192.168.100.2:3000"

        val ERROR_CONNECTION: Int = 1
        val ERROR_COMMON: Int = 2

        val EVENT_CONNECTED = "connected"
        val EVENT_DISCONNECTED = "disconnected"
        val EVENT_CHAT_MESSAGE = "chat_message"
        val EVENT_LOGIN = "login"
    }


    lateinit var client: Socket

    fun connect() {
        var opt: IO.Options = IO.Options()
        opt.reconnectionAttempts = 1

        client = IO.socket(CONNECTION_HOST, opt)

        setupClient()

        client.connect()
    }

    fun disconnect() {
        if (this.isConnected()) {
            client.disconnect()
        }
    }

    fun addEventListener(listener: ConnectionEventObserver.EventListener) {
        this.eventObserver.addEventListener(listener)
    }

    fun removeEventListener(listener: ConnectionEventObserver.EventListener) {
        eventObserver.removeEventListener(listener)
    }

    fun emit(event: String, data: Any, cb: OnAckEvent? = null) {
        client.emit(event, data, Ack {
            if (cb != null) {
                cb.call(it)
            }
        })
    }

    fun isConnected(): Boolean {
        return this.client.connected()
    }

    protected fun setupClient() {
        client.on(EVENT_CONNECTED, {
            eventObserver.notifyAboutEvent(EVENT_CONNECTED, null)
        }).on(EVENT_CHAT_MESSAGE, {
            eventObserver.notifyAboutEvent(EVENT_CHAT_MESSAGE, if (it.size > 0) it[0] as JSONObject else null)
        }).on(EVENT_DISCONNECTED, {
            eventObserver.notifyAboutEvent(EVENT_DISCONNECTED, null)
        }).on(Socket.EVENT_CONNECT_ERROR, {
            eventObserver.notifyAboutError(ERROR_CONNECTION, "connection error!")
        })
    }
}
