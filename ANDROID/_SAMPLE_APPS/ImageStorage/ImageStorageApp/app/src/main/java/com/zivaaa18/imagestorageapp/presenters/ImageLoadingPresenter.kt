package com.zivaaa18.imagestorageapp.presenters

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zivaaa18.imagestorageapp.R
import com.zivaaa18.imagestorageapp.httpClient.ApiClient
import com.zivaaa18.imagestorageapp.managers.OneImagePickManager
import com.zivaaa18.imagestorageapp.managers.TakeShotManager
import com.zivaaa18.imagestorageapp.managers.UploadManager
import com.zivaaa18.imagestorageapp.models.SharedImage
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.*

class ImageLoadingPresenter(view: ImageLoadingView) : BasePresenter<ImageLoadingPresenter.ImageLoadingView>(view) {

    private val oneImagePickManager = OneImagePickManager()
    private val uploadManager = UploadManager(core)
    private val takeShotManager = TakeShotManager()
    private val handler : Handler = Handler()

    companion object {
        val WRITE_PERMISSION_CODE = 9000
    }

    private var nextImage : Uri? = null

    interface ImageLoadingView : BaseView {
        fun showSnackBarMessage(message: String, actionMessage: String?, cb: (() -> Unit)?)
        fun showToast(message: String)
        fun onWritePermissionGranted()
        fun showImage(uri : Uri?)
        fun setUploadingState(state: Boolean)
        fun onPublishUploadProgress(progress: Long)
        fun onImageUploaded()
    }

    override fun setup() {
        super.setup()

        if (doesPermissionGiven()) {
            view.onWritePermissionGranted()
        } else {
            tryRequestExternalFileAccessPermission()
        }
    }

    override fun update(o: Observable?, arg: Any?) {

    }

    private fun requestWritePermissions() {
        if (android.os.Build.VERSION.SDK_INT > 22) {
            (view as Activity).requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_PERMISSION_CODE
            )
        }
    }

    fun upload() {
        if (nextImage == null) {
            return view.showToast("Image hasn't been choosen!")
        }

        view.setUploadingState(true)

        uploadManager.uploadImage(nextImage!!, ApiClient.ONE_IMAGE_FIELD, object : UploadManager.ProgressRequestBody.ProgressListener<SharedImage?> {
            override fun onProgress(written: Long, size: Long) {
                handler.post {
                    var d : Float = written.toFloat() / size.toFloat()
                    view.onPublishUploadProgress((d * 100).toLong())
                }
            }

            override fun onFailure(call: Call<SharedImage?>, t: Throwable) {
                view.setUploadingState(false)
                clearChoosenImage()
                view.showToast(t.message ?: "ERROR")
            }

            override fun onResponse(call: Call<SharedImage?>, response: Response<SharedImage?>) {
                if (response.raw().code() == 200) {
                    onUploadSucceeded()
                } else {
                    view.showToast("Error ${response.raw().code()}")
                }
            }
        })
    }

    private fun onUploadSucceeded() {
        view.setUploadingState(false)
        clearChoosenImage()
        view.showToast("Succeeded")
        view.onImageUploaded()
    }

    private fun setUri(uri : Uri?) {
        nextImage = uri
        view.showImage(uri)
    }

    private fun clearChoosenImage() {
        nextImage = null
        view.showImage(nextImage)
    }


    private fun tryRequestExternalFileAccessPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this.view as Activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            val message = view.getAppContext().getString(R.string.allow_write_permission_message)
            val allow = view.getAppContext().getString(R.string.allow)

            view.showSnackBarMessage(message, allow, {
                requestWritePermissions()
            })

        } else {
            requestWritePermissions();
        }
    }


    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == WRITE_PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                view.onWritePermissionGranted()
            }
        }
    }

    private fun doesPermissionGiven(): Boolean {
        return ContextCompat.checkSelfPermission(
            view.getAppContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun chooseImage() {
        oneImagePickManager.pickFile(view as Activity)
    }

    fun takeShot() {
        takeShotManager.takeShot(view as Activity)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            OneImagePickManager.ONE_IMAGE_PICK_CODE -> {
                setUri(oneImagePickManager.onPickFileResult(data!!))
            }
            TakeShotManager.CODE_IMAGE_SHOT -> {
                try {
                    setUri(takeShotManager.onResult(view as Activity))
                } catch (e : IOException) {
                    view.showToast(e.message ?: "Image cannot be found!")
                }

            }
        }
    }
}