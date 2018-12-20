package com.zivaaa18.chattyapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    /**
     * This is local connection handler
     */
    class MyServiceHolder(activity : MainActivity) : ServiceHolder<MainActivity>(activity) {
        override fun onEvent(eventName: String, json: JSONObject?) {
            when (eventName) {
                Connector.EVENT_CONNECTED -> {
                    ctx.onConnected()
                }
            }
        }

        override fun onError(type: Int, message: String) {
            when (type) {
                Connector.ERROR_CONNECTION -> {
                    Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    lateinit var userNameText : EditText
    lateinit var connectBtn : Button
    //Just a data wrapper
    lateinit var chatty: Chatty
    lateinit var serviceHolder : MyServiceHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serviceHolder = MyServiceHolder(this)

        chatty = Chatty.from(this.applicationContext)

        userNameText = findViewById(R.id.loginNameText)

        if (!chatty.getName().equals("")) {
            userNameText.setText(chatty.getName())
        }

        connectBtn = findViewById(R.id.btnEnter)

        connectBtn.apply {
            setOnClickListener {
                chatty.setName(userNameText.text.toString())
                connect()
            }
        }

        serviceHolder.startConnectionService()
    }

    /**
     * Will connect to socket. Or fire chat activity if it is connected already
     */
    fun connect() {
        if (serviceHolder.mBound and serviceHolder.service!!.isConnected()) {
            goToChat()
        } else {
            Toast.makeText(this, "Connection error, trying to reconnect. U should try again later.", Toast.LENGTH_SHORT).show()
            serviceHolder.service?.reconnect()
        }
    }

    /**
     * Action witch will be fire on connection succeeded.
     * It will lead us to chat if nockname is stored
     */
    fun onConnected() {
        if (!chatty.getName().equals("")) {
            serviceHolder.service?.emit(Connector.EVENT_LOGIN, chatty.getName(), object : Connector.OnAckEvent {
                override fun call(data: Array<Any>) {
                    goToChat()
                }
            })
        } else {
            Toast.makeText(this@MainActivity, "connection established!", Toast.LENGTH_SHORT).show()
        }

    }


    /**
     * Fire Chat activity. We dont need it in back stack history, just make it not saved
     */
    fun goToChat() {
        var intent = Intent(this, ChatActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    /**
     * Unbind service if activity is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        serviceHolder.unBindConnectionService()
    }
}
