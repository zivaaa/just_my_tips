import ROUTES from "../const/routes"

/**
 * @see router/index.js
 *
 * @param to
 * @param from
 * @param next
 * @param store
 * @returns {*}
 */
export default function auth (to, from, next, store) {
  if (!store.state.authenticated) {
    return next({
      name: ROUTES.WELCOME
    })
  }

  return true;
}
