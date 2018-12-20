package com.zivaaa18.imagestorageapp.presenters

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.zivaaa18.imagestorageapp.Core
import com.zivaaa18.imagestorageapp.httpClient.responses.SharedImageResponse
import com.zivaaa18.imagestorageapp.httpClient.responses.SimpleSuccessResponse
import com.zivaaa18.imagestorageapp.models.SharedImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ContentPresenter(view : ContentView) : BasePresenter<ContentPresenter.ContentView>(view){


    companion object {
        val CODE_IMAGE_LOADED = 50
        val CODE_IMAGE_SHOT = 51
    }

    private var images : MutableList<SharedImage> = mutableListOf()

    interface ContentView : BasePresenter.BaseView {
        fun onGotSharedImages(sharedImages : List<SharedImage>)

        fun onClearSharedImages()

        fun goToAddImage()

        fun toast(message : String)

        fun goToLogin()
    }

    override fun update(o: Observable?, arg: Any?) {
        val event = arg as Core.Event
        when(event.code) {
            Core.LOGOUT -> {
                goLogin()
            }
        }
    }

    fun logout() {
        core.logout()
    }

    private fun goLogin() {
        view.goToLogin()
    }

    override fun setup() {
        super.setup()
        refreshImages()
    }

    private fun refreshImages() {
        if (!core.isOnline()) {
            return
        }

        core.api.getImages().enqueue(object : Callback<SharedImageResponse> {
            override fun onFailure(call: Call<SharedImageResponse>, t: Throwable) {
                Log.d(Core.TAG, "error in getImages request")
            }

            override fun onResponse(call: Call<SharedImageResponse>, response: Response<SharedImageResponse>) {
                if (response.raw().code() == 200) {
                    images = response.body()!!.result!!.toMutableList()
                    updateImagesView()
                } else {
                    view.toast("ERROR")
                }
            }
        })
    }

    fun deleteImage(img : SharedImage) {
        core.api.deleteImage(img.id).enqueue(object : Callback<SimpleSuccessResponse> {
            override fun onFailure(call: Call<SimpleSuccessResponse>, t: Throwable) {
                Log.d(Core.TAG, "deleting Failed")
                view.toast(t.message ?: "Error")
            }

            override fun onResponse(call: Call<SimpleSuccessResponse>, response: Response<SimpleSuccessResponse>) {
                if (response.raw().code() == 200) {
                    Log.d(Core.TAG, "on delete!")
                    images.remove(img)
                    updateImagesView()
                } else {
                    view.toast("OOOps, an error happened!")
                }
            }
        })
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            CODE_IMAGE_LOADED -> {
                refreshImages()
            }
        }
    }

    private fun updateImagesView() {
        view.onGotSharedImages(images)
    }
}