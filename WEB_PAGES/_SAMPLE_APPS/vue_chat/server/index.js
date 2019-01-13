var express = require('express');
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);

var users = {}

app.use(express.static('dist'));

io.on('connection', function (socket) {
    console.log('a user connected');

    socket.on('login', function (data, callback) {
        console.log('login as ', data);
        users[socket.id] = {
            id: socket.id,
            ...data
        };

        if (callback) {
            console.log('cb as ', users[socket.id]);
            callback(null, users[socket.id])
        }
    });

    socket.on('logout', function () {
        if (users[socket.id]) {
            delete users[socket.id];
        }
    });

    socket.on('chat_message', function (msg, cb) {
        console.log('message: ' + msg);
        io.emit('chat_message', {
            user: users[socket.id],
            message: msg,
        });
    });

    socket.on('disconnect', function () {
        console.log('user disconnected');
    });


    socket.emit("connected", {
        id: socket.id
    })
});

http.listen(3000, function () {
    console.log('listening on *:3000');
});