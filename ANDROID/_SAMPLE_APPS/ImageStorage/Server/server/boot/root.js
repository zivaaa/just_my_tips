'use strict';
const path = require('path');

module.exports = function(server) {
  // Install a `/` route that returns server status
  var router = server.loopback.Router();
  router.get('/img', function(req, res) {
    res.sendFile(path.resolve(__dirname, "../", "public/pages/index.html"))
  });
  router.get('/', server.loopback.status());
  server.use(router);
};
