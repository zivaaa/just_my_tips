'use strict';

const RESPONSES = require("../tools/responses")

module.exports = function (Zuser) {
  Zuser.me = function (req, res, callback) {
    const userId = req.accessToken.userId;
    Zuser.findById(userId, (err, user) => {
      if (err) {
        return callback(RESPONSES.NOT_FOUND)
      }

      return callback(null, {
        ...RESPONSES,
        result: user
      }.SUCCEDED)
    })
  }
};
