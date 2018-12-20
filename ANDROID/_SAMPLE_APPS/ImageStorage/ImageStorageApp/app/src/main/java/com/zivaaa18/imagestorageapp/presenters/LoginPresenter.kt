package com.zivaaa18.imagestorageapp.presenters

import com.zivaaa18.imagestorageapp.Core
import com.zivaaa18.imagestorageapp.R
import com.zivaaa18.imagestorageapp.models.Credentials
import java.util.*

class LoginPresenter(
    view: LoginView
) : BasePresenter<LoginPresenter.LoginView>(view) {

    interface LoginView : BasePresenter.BaseView {
        fun onUILock()
        fun onUIFree()
        fun onLoginFailed(reason : String)
        fun onLoginSucceeded()
        fun onError(message: String)
    }

    override fun setup() {
        super.setup()
    }


    override fun update(o: Observable?, arg: Any?) {
        val event = arg as Core.Event

        when (event.code) {
            Core.DISCONNECTED -> {

            }
            Core.CONNECTED -> {

            }
            Core.ERROR_ACCESS_DENIED -> {

            }
        }
    }

    fun doLogin(login: String, password: String) {
        view.onUILock()
        login(Credentials(login, password))
    }

    protected fun login(credentials: Credentials) {
        if (!core.isOnline()) {
            view.onError(view.getAppContext().getString(R.string.error_not_online))
            view.onUIFree()
        } else {
            core.login(credentials, { err ->
                if (err == null) {
                    loginSuccess()
                } else {
                    loginFailed(err.message ?: "")
                }
            })
        }
    }

    protected fun loginSuccess() {
        view.onUIFree()
        view.onLoginSucceeded()
    }

    protected fun loginFailed(reason : String) {
        view.onUIFree()
        view.onLoginFailed(reason)
    }
}