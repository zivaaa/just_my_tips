package com.zivaaa18.chattyapp

import android.content.Context
import android.preference.PreferenceManager

class Chatty private constructor(val context : Context) {
    companion object {
        fun from(applicationContext : Context) : Chatty {
            return Chatty(applicationContext)
        }

        val USER_NAME_KEY = "USR_NAME"
    }

    private var userId : String = ""
    private var userName : String = ""

    private var messages : MutableList<ChatMessage> = mutableListOf()


    init {
        loadName()
    }


    fun addMessage(msg : ChatMessage) {
        messages.add(msg)
    }

    fun getMessages() : MutableList<ChatMessage> {
        return messages
    }

    fun setUserId(id : String) {
        this.userId = id
    }

    fun getUserId() : String {
        return this.userId
    }

    fun setName(name : String) {
        this.userName = name
        saveName()
    }

    fun getName() : String {
        return this.userName
    }

    fun saveName() {
        PreferenceManager.getDefaultSharedPreferences(context).edit().also {
            it.putString(USER_NAME_KEY, this.userName)
            it.commit()
        }
    }

    fun loadName() {
        this.userName = PreferenceManager.getDefaultSharedPreferences(context).getString(USER_NAME_KEY, "")
    }
 }