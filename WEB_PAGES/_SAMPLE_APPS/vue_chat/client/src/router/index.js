import Vue from 'vue'
import Router from 'vue-router'
import PageWelcome from "../containers/PageWelcome"
import PageChat from "../containers/PageChat";
import ROUTES from "../const/routes"


import auth from "../middleware/auth"
import store from "../store"


Vue.use(Router);

const router = new Router({
  routes: [
    {
      path: '/',
      name: ROUTES.WELCOME,
      component: PageWelcome,
      meta: {
        middleware: []
      }
    },
    {
      path: "/chat",
      name: ROUTES.CHAT,
      component: PageChat,
      meta: {
        middleware: [auth]
      }
    }
  ]
});

/**
 * Middleware setup.
 * Each middleware can do following things:
 *
 * - if returns false => already handled
 * - if returns true => go next, its ok
 * - if throw an Error => error, stop propagation
 */
router.beforeEach(function (to, from, next) {
  if (!to.meta.middleware) {
    next();
  } else {
    try {
      if (!to.meta.middleware.some((mw) => !mw(to, from, next, store))) {
        console.warn("route access granted");
        next()
      } else {
        console.warn("route has being handled already")
      }
    } catch (e) {
      console.error(e.message);
      next(e)
    }
  }
});

export default router;
