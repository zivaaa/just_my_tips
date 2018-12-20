package com.zivaaa18.imagestorageapp.managers

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.zivaaa18.imagestorageapp.Core
import com.zivaaa18.imagestorageapp.models.SharedImage
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import okhttp3.MultipartBody
import retrofit2.Callback
import java.io.*


class UploadManager(private val core: Core) {

    class FileToUpload(private val uri: Uri) {
        lateinit var stream: InputStream
        var size: Long = -1
        var fileName: String = ""

        fun open(ctx: Context) {
            ctx.contentResolver.openFileDescriptor(uri, "r").apply {
                size = this.statSize
                close()
            }

            fileName = getFileName(ctx) ?: uri.lastPathSegment

            stream = ctx.contentResolver.openInputStream(uri)

        }

        private fun getFileName(ctx: Context): String? {
            val cursor = ctx.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
            var string: String? = null
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        string = cursor.getString(0);
                    }
                } finally {
                    cursor.close();
                }
            }
            return string
        }

        fun close() {
            stream.close()
        }
    }

    fun uploadImage(uri: Uri, fieldName: String, progressListener: ProgressRequestBody.ProgressListener<SharedImage?>) {
        val file = FileToUpload(uri)
        file.open(core.ctx)

        val fileBody = ProgressRequestBody(file, progressListener)
        val filePart = MultipartBody.Part.createFormData(fieldName, file.fileName, fileBody)

        core.api.uploadImage(filePart).enqueue(progressListener)
    }


    class ProgressRequestBody<T>(
        val file: FileToUpload,
        val listener: ProgressRequestBody.ProgressListener<T>
    ) : RequestBody() {

        interface ProgressListener<T> : Callback<T> {
            fun onProgress(written: Long, size: Long)
        }

        override fun contentType(): MediaType? {
            return MediaType.parse("image/*")
        }

        override fun writeTo(sink: BufferedSink) {
            val len = file.size
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var uploaded = 0
            try {
                var read: Int
                while (true) {
                    read = file.stream.read(buffer)
                    if (read == -1) {
                        break
                    }

                    uploaded += read
                    sink.write(buffer, 0, read)
                    Log.d(Core.TAG, "update ${uploaded} / ${len}")
                    listener.onProgress(uploaded.toLong(), len)
                }
            } catch (e: IOException) {
                //@todo catch exception
            } finally {
                file.close()
            }
        }
    }
}