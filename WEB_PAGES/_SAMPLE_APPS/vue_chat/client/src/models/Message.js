export default class Message {
  constructor(message, user, date) {
    this.message = message;
    this.user = user;
    this.date = date || new Date();
  }
}
