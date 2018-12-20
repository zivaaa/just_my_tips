package com.zivaaa18.imagestorageapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zivaaa18.imagestorageapp.presenters.LandingPresenter
import java.lang.ref.WeakReference

class LandingActivity : AppCompatActivity(), LandingPresenter.LandingView {

    lateinit var presenter: LandingPresenter
    private var task: GoToTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        presenter = LandingPresenter(this)
        presenter.setup()
    }

    override fun onResume() {
        super.onResume()
        presenter.check()
    }

    override fun goToLogin() {
        goToNext(LoginActivity::class.java)
    }

    override fun goToContent() {
        goToNext(ContentActivity::class.java)
    }

    private fun goToNext(clazz: Class<*>) {
        task = GoToTask(clazz, WeakReference(this))
        task?.execute()
    }

    override fun onPause() {
        super.onPause()
        task?.apply {
            cancel(false)
        }
    }

    override fun getAppContext(): Context {
        return applicationContext
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    class GoToTask(
        private val clazz: Class<*>,
        private val activity: WeakReference<Activity>
    ) : AsyncTask<Unit, Unit, Unit>() {

        override fun doInBackground(vararg params: Unit?) {
            Thread.sleep(2000)
        }

        override fun onPostExecute(result: Unit?) {
            activity.get()?.apply {
                startActivity(Intent(this, clazz))
                finish()
            }
        }
    }
}
