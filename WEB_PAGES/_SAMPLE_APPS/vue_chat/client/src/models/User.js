export default class User {
  constructor(id, username) {
    if (typeof id == 'object') {
      this.id = id.id;
      this.username = id.username;
    } else {
      this.id = id;
      this.username = username;
    }
  }
}
