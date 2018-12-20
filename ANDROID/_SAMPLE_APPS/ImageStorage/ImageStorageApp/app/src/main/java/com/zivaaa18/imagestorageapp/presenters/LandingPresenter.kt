package com.zivaaa18.imagestorageapp.presenters

import java.util.*

class LandingPresenter(view: LandingView) : BasePresenter<LandingPresenter.LandingView>(view) {
    interface LandingView : BasePresenter.BaseView {
        fun goToLogin()

        fun goToContent()
    }

    override fun update(o: Observable?, arg: Any?) {

    }

    override fun setup() {
        super.setup()
    }

    fun check() {
        core.checkAuth({
            if (it) {
                onAuthenticated()
            } else {
                onNotAuthenticated()
            }
        })
    }

    private fun onAuthenticated() {
        view.goToContent()
    }

    private fun onNotAuthenticated() {
        view.goToLogin()
    }
}