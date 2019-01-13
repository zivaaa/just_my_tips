import Vue from 'vue'
import Vuex from 'vuex'
import * as STORE_CONST from "../const/store"
import User from "../models/User"
import Message from "../models/Message"
import Connector from "../tools/Connector"
import router from "../router"
import ROUTES from "../const/routes"

Vue.use(Vuex);

// root state object.
// each Vuex instance is just a single state tree.
const state = {
  authenticated: false,
  user: null,
  status: STORE_CONST.STATUS_NORMAL,
  connector: new Connector(),
  messages: [
    // new Message("test1", new User("1", "user_1")),
  ]
};

// mutations are operations that actually mutates the state.
// each mutation handler gets the entire state tree as the
// first argument, followed by additional payload arguments.
// mutations must be synchronous and can be recorded by plugins
// for debugging purposes.


const mutations = {
  [STORE_CONST.MUTATION_LOGIN]: function (state, user) {
    state.authenticated = true;
    state.user = user;
    router.push({
      name: ROUTES.CHAT
    })
  },
  [STORE_CONST.MUTATION_LOGOUT]: function (state) {
    state.authenticated = false;
    state.user = null;
    router.push({
      name: ROUTES.WELCOME
    })
  },
  [STORE_CONST.MUTATION_SET_STATUS]: function (state, status) {
    state.status = status;
  },
  [STORE_CONST.MUTATION_RECEIVE_MESSAGE]: function (state, message) {
    state.messages.push(message);
  },
};

// actions are functions that cause side effects and can involve
// asynchronous operations.
const actions = {
  [STORE_CONST.ACTION_LOGIN]: (context, data) => {
    context.commit(STORE_CONST.MUTATION_SET_STATUS, STORE_CONST.STATUS_LOADING);
    context.state.connector.doLogin(data, (err, userInfo) => {
      if (err) {
        context.commit(STORE_CONST.MUTATION_SET_STATUS, STORE_CONST.STATUS_ERROR);
      } else {
        context.commit(STORE_CONST.MUTATION_SET_STATUS, STORE_CONST.STATUS_NORMAL);
        context.commit(STORE_CONST.MUTATION_LOGIN, new User(userInfo.id, userInfo.username))
      }
    });
  },
  [STORE_CONST.ACTION_LOGOUT]: (context) => {
    context.state.connector.doLogout();
    context.commit(STORE_CONST.MUTATION_LOGOUT)
  },
  [STORE_CONST.ACTION_SET_STATUS]: (context, status) => {
    context.commit(STORE_CONST.MUTATION_SET_STATUS, status);
  },
  [STORE_CONST.ACTION_SEND_MESSAGE]: (context, message) => {
    context.state.connector.doSendMessage(message);
  },
  [STORE_CONST.ACTION_RECEIVE_MESSAGE]: (context, message) => {
    context.commit(STORE_CONST.MUTATION_RECEIVE_MESSAGE, new Message(message.message, new User(message.user)));
  }
}

// getters are functions
const getters = {
  // evenOrOdd: state => state.count % 2 === 0 ? 'even' : 'odd'
}

// A Vuex instance is created by combining the state, mutations, actions,
// and getters.
export default new Vuex.Store({
  state,
  getters,
  actions,
  mutations
})
