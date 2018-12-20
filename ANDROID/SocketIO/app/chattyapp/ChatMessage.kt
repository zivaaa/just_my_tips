package com.zivaaa18.chattyapp

import org.json.JSONObject


class ChatMessage(var message : String, var userId : String, var userName : String) {
    fun isOwner(id : String) : Boolean = userId.equals(id)


    companion object {
        fun fromJSONObject(json : JSONObject) : ChatMessage {
            return ChatMessage(
                json.getString("message"),
                json.getString("userId"),
                json.getString("userName")
            )
        }
    }

}