package com.zivaaa18.imagestorageapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.zivaaa18.imagestorageapp.presenters.ImageLoadingPresenter
import kotlinx.android.synthetic.main.inc_add_image.*

class ImageLoadingActivity : AppCompatActivity(), ImageLoadingPresenter.ImageLoadingView {

    lateinit var presenter: ImageLoadingPresenter
    var dialog : ProgressDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_loading)
        btnTakeShot.isEnabled = false

        presenter = ImageLoadingPresenter(this)
        presenter.setup()

        btnChooseImage.setOnClickListener {
            presenter.chooseImage()
        }

        btnTakeShot.setOnClickListener {
            presenter.takeShot()
        }

        btnUpload.setOnClickListener {
            presenter.upload()
        }
    }


    override fun showSnackBarMessage(message: String, actionMessage: String?, cb: (() -> Unit)?) {
        Snackbar.make(findViewById(R.id.content), message, Snackbar.LENGTH_LONG).also {
            if (actionMessage != null) {
                it.setAction(actionMessage, {
                    cb!!.invoke()
                })
            }
            it.show()
        }
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onWritePermissionGranted() {
        btnTakeShot.isEnabled = true
    }

    override fun getAppContext(): Context {
        return this.applicationContext
    }

    override fun setUploadingState(state: Boolean) {
        if (state) {
            if (dialog == null) {
                dialog = ProgressDialogFragment.getInstance().apply {
                    show(supportFragmentManager, ProgressDialogFragment.SHOW_TAG)
                }
            }
        } else {
            dialog?.dismiss()
            dialog = null
        }

    }

    override fun onPublishUploadProgress(progress: Long) {
        Log.d(Core.TAG, "progress now => ${progress} %")
        dialog?.setProgress(progress.toInt())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    override fun onImageUploaded() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun showImage(uri: Uri?) {
        previewImage.setImageURI(uri)
    }
}
