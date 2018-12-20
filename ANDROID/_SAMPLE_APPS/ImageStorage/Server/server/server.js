'use strict';

var loopback = require('loopback');
var boot = require('loopback-boot');
// var bodyParser = require('body-parser');
// var multer = require('multer')
// var CONST = require("../config/const")

var app = module.exports = loopback();
require('loopback-disable-method-mixin')(app);

// app.middleware('parse', multer({dest : CONST.UPLOADS_TEMP_PATH}).single("userfile"));

app.start = function() {
  // start the web server
  return app.listen(function() {
    app.emit('started');
    var baseUrl = app.get('url').replace(/\/$/, '');
    console.log('Web server listening at: %s', baseUrl);
    if (app.get('loopback-component-explorer')) {
      var explorerPath = app.get('loopback-component-explorer').mountPath;
      console.log('Browse your REST API at %s%s', baseUrl, explorerPath);
    }
  });
};

app.middleware('auth', loopback.token({
  cookies: ['access_token'],
  headers: ['access_token', 'X-Access-Token'],
  params:  ['access_token'],
  currentUserLiteral: 'me'
}));


// Bootstrap the application, configure models, datasources and middleware.
// Sub-apps like REST API are mounted via boot scripts.
boot(app, __dirname, function(err) {
  if (err) throw err;

  // start the server if `$ node server.js`
  if (require.main === module)
    app.start();
});
