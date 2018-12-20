package com.zivaaa18.imagestorageapp.presenters

import android.content.Context
import com.zivaaa18.imagestorageapp.Core
import java.util.*

abstract class BasePresenter<T : BasePresenter.BaseView>(var view : T) : Observer {

    interface BaseView {
        fun getAppContext(): Context
    }

    val core: Core = Core.getInstance(view.getAppContext().applicationContext)

    open fun setup() {
        core.addObserver(this)
    }

    open fun destroy() {
        core.deleteObserver(this)
    }
}