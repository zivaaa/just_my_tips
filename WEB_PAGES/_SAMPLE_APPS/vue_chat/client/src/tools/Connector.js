import STORE_CONST from "../const/store"

export default class Connector {
  constructor() {
    this.store = null;
    this.socket = null;
    this.initiated = false;
    this.url = "";
  }

  init(connectionUrl, store) {
    if (this.initiated) {
      throw new Error("Connector is being initiated already!");
    }
    this.url = connectionUrl;
    this.initiated = true;
    this.store = store;

    this.store.dispatch(STORE_CONST.ACTION_SET_STATUS, STORE_CONST.STATUS_LOADING);
    this.socket = io(this.url);
    this._listen()
  }

  _listen() {
    this.socket.on("connected", () => {
      console.info("connected");
      this.store.dispatch(STORE_CONST.ACTION_SET_STATUS, STORE_CONST.STATUS_NORMAL);
    });

    this.socket.on("chat_message", (messageInfo) => {
      console.info("chat_message", messageInfo)
      this.store.dispatch(STORE_CONST.ACTION_RECEIVE_MESSAGE, messageInfo);
    });

    this.socket.on("disconnected", () => {
      console.info("disconnected")
    });
  }

  doLogin(data, cb) {
    this.socket.emit("login", data, function (err, userInfo) {
      if (err) {
        console.error("doLogin error", err);
      } else {
        console.info("doLogin", userInfo);
      }

      if (cb)
        cb(err, userInfo)
    })
  }

  doLogout() {
    this.socket.emit("logout")
  }

  doSendMessage(message) {
    this.socket.emit("chat_message", message);
  }
}
