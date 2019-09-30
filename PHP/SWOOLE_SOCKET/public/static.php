<?php

/**
 * @link https://gist.github.com/ezimuel/a2e0ff7308952f2aa946f828a1302a63
 */

$host = $argv[1] ?? '127.0.0.1';
$port = $argv[2] ?? 8080;

$http = new swoole_http_server($host, $port);

$http->on("start", function ($server) {
    printf("HTTP server started at %s:%s\n", $server->host, $server->port);
    printf("Master  PID: %d\n", $server->master_pid);
    printf("Manager PID: %d\n", $server->manager_pid);
});
$static = [
    'css' => 'text/css',
    'js' => 'text/javascript',
    'png' => 'image/png',
    'gif' => 'image/gif',
    'jpg' => 'image/jpg',
    'jpeg' => 'image/jpg',
    'mp4' => 'video/mp4',
    'html' => 'text/html'
];
$http->on("request", function ($request, $response) use ($static) {
    if (getStaticFile($request, $response, $static)) {
        return;
    }
    $response->status(404);
    $response->end();
});
$http->start();

function getStaticFile(
    swoole_http_request $request,
    swoole_http_response $response,
    array $static
): bool
{
    $staticFile = __DIR__ . $request->server['request_uri'];
    xprint($staticFile);
    if (!file_exists($staticFile)) {
        return false;
    }
    $type = pathinfo($staticFile, PATHINFO_EXTENSION);
    if (!isset($static[$type])) {
        return false;
    }
    $response->header('Content-Type', $static[$type]);
    $response->sendfile($staticFile);
    return true;
}

function xprint($str = "")
{
    echo $str . PHP_EOL;
}