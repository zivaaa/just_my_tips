package com.zivaaa18.imagestorageapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView

import android.Manifest.permission.READ_CONTACTS
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.zivaaa18.imagestorageapp.presenters.LoginPresenter

import kotlinx.android.synthetic.main.activity_login.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), LoginPresenter.LoginView {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    lateinit private var presenter : LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        email_sign_in_button.setOnClickListener { attemptLogin() }

        presenter = LoginPresenter(this)
        presenter.setup()
    }

    override fun onUILock() {
        showProgress(true)
    }

    override fun onUIFree() {
        showProgress(false)
    }

    override fun getAppContext(): Context {
        return applicationContext
    }

    override fun onLoginFailed(reason : String) {
        Log.d(Core.TAG, "onLoginFailed : ${reason}")
        Toast.makeText(this, "Failed. ${reason}", Toast.LENGTH_SHORT).show()
        if (reason == Core.REASON_INVALID_CREDENTIALS) {
            onCredentialsInCorrect()
        }
    }

    override fun onLoginSucceeded() {
        Log.d(Core.TAG, "onLoginSucceeded")
        startActivity(Intent(this, ContentActivity::class.java))
        finish()
    }

    override fun onError(message: String) {
        Log.d(Core.TAG, message)
        Toast.makeText(this, "Error. ${message}", Toast.LENGTH_SHORT).show()
    }


    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        // Reset errors.
        email.error = null
        password.error = null

        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()

        presenter.doLogin(emailStr, passwordStr)
    }

    private fun onCredentialsInCorrect() {
        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password.text.toString()) && !isPasswordValid(password.text.toString())) {
            password.requestFocus()
        } else {
            password.error = "Invalid credentials."
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email.text.toString())) {
            email.error = getString(R.string.error_field_required)
            email.requestFocus()
        } else if (!isEmailValid(email.text.toString())) {
            email.error = getString(R.string.error_invalid_email)
            email.requestFocus()
        } else {
            email.error = "Invalid credentials."
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

}
