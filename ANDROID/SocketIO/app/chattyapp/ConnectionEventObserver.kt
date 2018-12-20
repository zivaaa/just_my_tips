package com.zivaaa18.chattyapp

import org.json.JSONObject



class ConnectionEventObserver {
    interface EventListener {
        fun onEvent(eventName: String, json: JSONObject?);
        fun onError(type: Int, message: String);
    }
    var listeners: MutableList<EventListener> = ArrayList<EventListener>()


    fun notifyAboutEvent(eventName: String, json: JSONObject?) {
        for (listener in listeners) {
            listener.onEvent(eventName, json);
        }
    }

    fun notifyAboutError(code: Int, message: String) {
        for (listener in listeners) {
            listener.onError(code, message);
        }
    }

    fun addEventListener(listener: EventListener) {
        this.listeners.add(listener)
    }

    fun removeEventListener(listener: EventListener) {
        listeners.remove(listener)
    }

}