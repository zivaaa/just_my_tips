package com.zivaaa18.chattyapp

import android.content.ComponentName
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.json.JSONObject
import java.util.*

class ChatActivity : AppCompatActivity() {

    /**
     * Spinner dialog. Shows when user cant chat.
     */
    var dialogWaiting: SpinnerDialog? = null

    lateinit var chatty: Chatty

    /**
     * My service holder
     */
    val serviceHolder = object : ServiceHolder<ChatActivity>(this) {
        override fun onEvent(eventName: String, json: JSONObject?) {
            when (eventName) {
                Connector.EVENT_CHAT_MESSAGE -> {
                    if (json == null) {
                        return
                    }

                    /**
                     * Add message and notify recycler that there is something to show
                     */
                    chatty.addMessage(ChatMessage.fromJSONObject(json))
                    adapter.setData(chatty.getMessages())
                }
            }
        }

        override fun onError(type: Int, message: String) {
            when(type) {

                Connector.ERROR_CONNECTION -> {
                    /**
                     * Connection is broken
                     *
                     * Set waiting state.
                     *
                     * get back to MainActivity after 1 second
                     */
                    setWaitingState()
                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            goBack()
                        }
                    }, 1000)
                    Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            super.onServiceConnected(name, service)
            ctx.unsetWaitingState()
            chatty.setUserId(this.service!!.getSocketId())
        }
    }

    private lateinit var messageView: EditText
    private lateinit var sendBtnView: ImageButton
    private lateinit var recycler: RecyclerView
    private lateinit var adapter : MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatty = Chatty.from(this.applicationContext)
        messageView = findViewById(R.id.chatMessage)
        sendBtnView = findViewById(R.id.sendMessageBtn)
        sendBtnView.setOnClickListener {
            sendMessage()
        }

        this.adapter = MyAdapter(chatty)

        recycler = findViewById(R.id.recycler)
        recycler.adapter = this.adapter
        recycler.layoutManager = LinearLayoutManager(this)

        setWaitingState()

        serviceHolder.startConnectionService()
    }

    /**
     * Go back to MainActivity without history
     */
    fun goBack() {
        var intent = Intent(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    /**
     * Unbind from service.
     */
    override fun onDestroy() {
        super.onDestroy()
        serviceHolder.unBindConnectionService()
    }

    /**
     * get message and send to socket server
     */
    fun sendMessage() {
        if (!serviceHolder.mBound) return

        val message = messageView.text.toString()

        if (message.equals("")) {
            Toast.makeText(this, "message is empty!", Toast.LENGTH_SHORT).show()
            return
        }

        serviceHolder.service?.emit(Connector.EVENT_CHAT_MESSAGE, message)
    }

    fun setWaitingState() {
        if (dialogWaiting != null) {
            return
        }

        dialogWaiting = SpinnerDialog.getInstance().apply {
            show(supportFragmentManager, "wait")
        }
    }

    fun unsetWaitingState() {
        if (dialogWaiting == null) {
            return
        }

        dialogWaiting?.dismiss()
        dialogWaiting = null
    }

    class MyAdapter(val chatty: Chatty) : RecyclerView.Adapter<MyAdapter.MyHolder>() {

        var dataSet: List<ChatMessage> = mutableListOf()

        fun setData(messages : List<ChatMessage>) {
            this.dataSet = messages
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.chat_message_item, p0, false)
            return MyHolder(view).also {
                it.setMessage(dataSet[p1])
            }
        }

        override fun getItemCount(): Int {
            return dataSet.size
        }

        override fun onBindViewHolder(p0: MyHolder, p1: Int) {
            p0.setMessage(dataSet[p1])
        }

        inner class MyHolder(view: View) : RecyclerView.ViewHolder(view) {
            var textView: TextView

            init {
                textView = view.findViewById(R.id.chatMessageText)
            }

            /**
             * Just a view handler.
             * If it is owr message, show on the right side
             * If someone other writes it - on the left side
             */
            fun setMessage(msg: ChatMessage) {
                textView.setText("${msg.userName} : ${msg.message}")

                var isOwner = msg.isOwner(chatty.getUserId())

                (itemView as LinearLayout).apply {
                    if (isOwner) {
                        this.gravity = Gravity.RIGHT
                    } else {
                        this.gravity = Gravity.LEFT
                    }
                }

                textView.apply {
                    if (isOwner) {
                        this.setBackgroundResource(R.drawable.bubble_user)
                    } else {
                        this.setBackgroundResource(R.drawable.bubble_other)
                    }
                }
            }
        }
    }

}