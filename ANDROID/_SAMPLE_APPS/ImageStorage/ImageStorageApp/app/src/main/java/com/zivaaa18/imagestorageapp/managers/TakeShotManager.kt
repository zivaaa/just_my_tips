package com.zivaaa18.imagestorageapp.managers

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/*
    Read More on: https://developer.android.com/training/camera/photobasics

    //------------//

    add file provider to manifest
    -----------------------------
     <provider
          android:name="androidx.core.content.FileProvider"
          android:authorities="COM.EXAMPLE.APP.fileprovider"
          android:exported="false"
          android:grantUriPermissions="true">
          <meta-data
          android:name="android.support.FILE_PROVIDER_PATHS"
          android:resource="@xml/file_paths"></meta-data>
     </provider>

     //------------//

     add xml/file_paths.xml
     -----------------------------
     <paths xmlns:android="http://schemas.android.com/apk/res/android">
         <external-path name="my_images" path="Android/data/COM.EXAMPLE.APP/files/images" />
     </paths>

     //------------//

*/
class TakeShotManager {
    companion object {
        val CODE_IMAGE_SHOT = 51
        val AUTHORITY = "com.zivaaa18.imagestorageapp.fileprovider"
    }

    private var mCurrentPhotoPath: String = ""

    fun takeShot(activity: Activity) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(activity)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        activity,
                        AUTHORITY,
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    activity.startActivityForResult(takePictureIntent, CODE_IMAGE_SHOT)
                }
            }
        }
    }

    fun setCurrentPath(path: String) {
        mCurrentPhotoPath = path
    }

    fun getCurrentPath(): String {
        return mCurrentPhotoPath
    }

    /**
     * Scan image to galery and return file URi
     */
    @Throws(IOException::class)
    fun onResult(activity: Activity): Uri? {
        scanImage(activity, getCurrentPath())
        return Uri.fromFile(File(getCurrentPath()).also { if (!it.exists()) throw IOException("File not found!") })
    }

    /**
     * To get bitmap from result intent directly
     */
    fun getBitmapFromIntent(intent: Intent): Bitmap {
        return intent.extras.get("data") as Bitmap
    }

    /**
     * To get resized bitmap
     */
    fun getResizedBitmap(activity: Activity, intent: Intent, width: Int, height: Int): Bitmap {
        return getResizedBitmap(width, height)
    }


    private fun getResizedBitmap(width: Int, height: Int): Bitmap {
        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(getCurrentPath(), this)
            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.min(photoW / width, photoH / height)

            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        return BitmapFactory.decodeFile(getCurrentPath(), bmOptions)
    }


    private fun scanImage(activity: Activity, imagePath: String) {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(imagePath)
            mediaScanIntent.data = Uri.fromFile(f)
            activity.sendBroadcast(mediaScanIntent)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(activity: Activity): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "ts_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            setCurrentPath(absolutePath)
        }
    }
}