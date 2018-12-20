# Чат через Socket IO

Как сделать чат на сокетах.

![IMAGE #1] (https://bitbucket.org/zivaaa/just_my_tips/src/master/ANDROID/SocketIO/screenshots/chatty.jpg)

### 1. Серверная часть

Сервер работает на NodeJs { socket io, express }


```
  //package.json dependencies:

  ...
  "dependencies": {
    "express": "^4.15.2",
    "socket.io": "^2.2.0"
  }
  ...

```

Код серверной части index.js (почти полный копипаст с сайта socket io)

```

var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);

var users = {

}

app.get('/', function(req, res){
  res.sendFile(__dirname + '/index.html');
});

io.on('connection', function(socket){
  console.log('a user connected');

   socket.on('login', function(name, callback){
    console.log('login as ' + name);
    console.log(socket.id);
	users[socket.id] = name
	if (callback) {
		callback()
	}
  });

  socket.on('chat_message', function(msg){
    console.log('message: ' + msg);
	io.emit('chat_message', {
		userName : users[socket.id] || "unknown",
		message: msg,
		userId: socket.id
	});
  });

  socket.on('disconnect', function(){
    console.log('user disconnected');
  });

  socket.broadcast.emit('Someone connected...');
  socket.emit("connected", {
		id : socket.id
	  })
});

http.listen(3000, function(){
   console.log('listening on *:3000');
});



```

Логика такая:

 - подключаемся к серверу и получаем событие  ``` connected ``` (там еще id отправляется но он не особо нужен)

 - по событию ``` connected ``` мы знаем что подключение успешно и можно указать свой ник через вызов события ``` login ```

 - в событии ``` login ``` передаем серверу имя юзера и сохраняем связку ``` socket.id => userName ```, после чего вызываем acknowledge каллбэк,
 если он есть

 - Далее можно уже отсылать сообщения методом ``` chat_message ```, этим же событием будем принимать

 ### 2. Веб клиент.

 Это почти копипаст с сайта socket io, этого хватит. Пишем index.html

 ```

<!doctype html>
<html>
  <head>
    <title>Socket.IO chat</title>
    <style>
      * { margin: 0; padding: 0; box-sizing: border-box; }
      body { font: 13px Helvetica, Arial; }
      form { background: #000; padding: 3px; position: fixed; bottom: 0; width: 100%; }
      form input { border: 0; padding: 10px; width: 90%; margin-right: .5%; }
      form button { width: 9%; background: rgb(130, 224, 255); border: none; padding: 10px; }
      #messages { list-style-type: none; margin: 0; padding: 0; }
      #messages li { padding: 5px 10px; }
      #messages li:nth-child(odd) { background: #eee; }
    </style>
  </head>
  <body>
    <ul id="messages"></ul>
    <form action="">
      <input id="m" autocomplete="off" /><button>Send</button>
    </form>
	<div style="position: absolute; top: 0; right: 0; padding: 25px;">
		<label for="my_nick">My nick is:</label>
		<input type="text" id="my_nick" value="AnonWeb"/>
		<button id="login">login</button>
	</div>
	<script src="/socket.io/socket.io.js"></script>
	<script src="https://code.jquery.com/jquery-1.11.1.js"></script>
	<script>
	  $(function () {
		var socket = io();
		$('form').submit(function(){
		  socket.emit('chat_message', $('#m').val());
		  $('#m').val('');
		  return false;
		});

		socket.on('connected', function(msg){
		  socket.emit("login", $("#my_nick").val())
		});

		socket.on('chat_message', function(msg){
		  $('#messages').append($('<li>').text(msg.userName + ": " + msg.message));
		});

		$("#login").on("click", function() {
			socket.emit("login", $("#my_nick").val())
		})
	  });
	</script>


  </body>


</html>

 ```

тут можно писать сообщение и задавть ник юзера. Собственно с клиента и можно тестить работу сервера. А теперь к андроиду.

### 3. Android приложение

Тут буду писать кратко основные моменты, основное в см. в коде рядом с этим файлом.

Первое что надо сделать это подключить библиотеку. Я использовал эту (https://github.com/socketio/socket.io-client-java):

```
    //build.grandle

    implementation ('io.socket:socket.io-client:1.0.0') {
        exclude group: 'org.json', module: 'json'
    }

```

Она предоставит все, что нужно

 - соединение
 - реконнект
 - слушает события
 - вызывает события
 - обрабатывает ошибки соедиения
 - выводит данные в JSONObject (жаль что не Gson ну да ладно, конвертить не буду)

 #### Как пользоваться

Создаем подключение (Если не указано иное, IO вернет тот же объект подключения)

```

var client: Socket= IO.socket("http://192.168.100.2:3000")
client.connect()

```


Настройки подключения

```

        var opt: IO.Options = IO.Options()
        opt.reconnectionAttempts = 1
        //Тут еще можно время подключения, надо ли переподключатсья и все такое

        var client = IO.socket(CONNECTION_HOST, opt)

```

Слушаем события (данные с сервера и не только).

Помни: все события придут не в главном потоке, поэтому надо сделать так, чтобы данные обрабатывались корректно.


```

client.on(EVENT_CONNECTED, {
            //получаем данные в виде массива Object... args
            // можно вывести объект как JSONObject
             var json = it[0] as JSONObject

             //Do something else
        })

```

Отправляем данные на сервер

```

client.emit(event, data)

//или если надо получить обратный вызов, не забудь вызвать это с сервера!

client.emit(event, data, Ack {
    //Do something
})



```

Дисконнект

```

    client.disconnect()

```

Ловим ошибки подключения

```

client.on(Socket.EVENT_CONNECT_ERROR, {
                  //eventObserver.notifyAboutError(ERROR_CONNECTION, "connection error!")
              })

```



##### Продолжаем делать делать приложение

Оно будет состоять из 3 частей

1. Сервис который будет отвечать за работу с сокетами.
2. Главная активити, в которой будет происходить подключение / переподключение, а так же login юзера (читай задать ник).
3. Активити Чата, в которую будем переходить после логина юзера и из которой можно будет вернуться при сбросе соединения.

Сервис

Так как мне не нужно, чтобы сервис работал в фоне, например рассылал уведомления.
Буду делать его привязанным. То есть он будет работать пока хоть одна из активити привязана к нему.

Создаем сервис (Не забываем объявить в манифесте):

```


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

```

Сервис предоставляет методы калбеков, предоставляет интерфейс для взаимодействия сокетов и активити, а также хранит само подключение.


Connector - обертка надо сокетами.

```

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

```

Чтобы не писать в двух активти один и тот же код по привязке с сервисом, создадим абстракный класс со всеми методами.

```


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

```

Остается только создать экземпляр в активити и вызвать пару методов.

MainActivity - лендинг.

```

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



```

Все что она делает это слушает вызов события подключения и дает возможность выбрать ник.

Если ник уже сохранен (Chatty см. код рядом) и подлючение свершилось, то сразу перейдем в чат.
Если ник еще не сохранен, то придется его подтвердить.
Если подключение не работает придется логиниться (сработает проверка на соединение и возможно произойдет реконнект)

(Лейаут см в коде рядом)

ChatActivity - много кода, но в основном изза RecyclerView и обработки виджетов.
По сути просто биндится к сервису, отправляет сообщения и принимает.
А если подключение сорвалось - возвращает в MainActivity.

```


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

```

##### Манифест

```

<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="com.zivaaa18.chattyapp">

    <uses-permission android:name="android.permission.INTERNET"/>

    <application
            android:allowBackup="true"
            android:icon="@mipmap/ic_launcher"
            android:label="@string/app_name"
            android:roundIcon="@mipmap/ic_launcher_round"
            android:supportsRtl="true"
            android:theme="@style/AppTheme">
        <activity android:name=".MainActivity" android:screenOrientation="sensorPortrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>

                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>

        <service android:name=".ConnectionService"/>

        <activity android:name=".ChatActivity" android:screenOrientation="sensorPortrait">
        </activity>
    </application>

</manifest>

```

Остальной код в папке рядом