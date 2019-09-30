<?php

$host = "127.0.0.1";
$port = 8081;

$server = new swoole_websocket_server($host, $port);

function xprint($str = "") {
    echo $str . PHP_EOL;
}

class Sockets {
    /**
     * @var swoole_websocket_server $server
     */
    public $server;

    /**
     * @var \Swoole\Table|null
     */
    public $table = null;

    private function __construct($server)
    {
        $this->server = $server;
        $this->table = new \Swoole\Table(1024);
        $this->table->column("id", \Swoole\Table::TYPE_INT, 10);
        $this->table->column("userId", \Swoole\Table::TYPE_STRING, 15);
        $this->table->create();
    }


    function add($fd) {
        $this->table[$fd] = ['id' => $fd, 'userId' => ''];
    }

    function remove($fd) {
        unset($this->table[$fd]);
    }

    function emitAll($action, $data) {
        foreach ($this->table as $fd => $socket) {
            $this->server->push($fd, json_encode([
                'action' => $action,
                'data' => $data
            ]));
        }
    }

    private static $me = null;

    /**
     * @param $server
     * @return Sockets
     */
    public static function &instance($server = null) {
        if (!static::$me) {
            static::$me = new static($server);
        }

        return static::$me;
    }
}


Sockets::instance($server);

$server->on('open', function($server, $req) {
    echo "connection open: {$req->fd}\n";
    Sockets::instance()->add($req->fd);
});

$server->on('message', function($server, swoole_websocket_frame $frame) {
    echo "received message: {$frame->data}\n";
    $allData = json_decode($frame->data, true);
    $action = $allData['action'];
    $data = $allData['data'];
    Sockets::instance()->emitAll($action, $data);
});

$server->on('close', function($server, $fd) {
    Sockets::instance()->remove($fd);
    echo "connection close: {$fd}\n";
});

echo "server started on $host:$port" . PHP_EOL;
$server->start();
