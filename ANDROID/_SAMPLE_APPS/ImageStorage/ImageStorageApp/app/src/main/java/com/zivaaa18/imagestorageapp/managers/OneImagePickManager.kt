package com.zivaaa18.imagestorageapp.managers

import android.app.Activity
import android.content.Intent
import android.net.Uri

class OneImagePickManager {
    companion object {
        val ONE_IMAGE_PICK_CODE = 5000
    }

    fun pickFile(activity : Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)

        intent.type = "image/*"
        activity.startActivityForResult(intent, ONE_IMAGE_PICK_CODE)
    }

    fun onPickFileResult(intent: Intent) : Uri? {
        val filePath = intent.data.toString()
        val uri : Uri = Uri.parse(filePath)
        return uri
    }
}