# Retrofit2 

### Как прочитать файл по его Uri и отправить на сервер с возможностью тречить прогресс отправки.

Это пример отправки файла по его uri. Загрузка происходит посредством multipart формы.

Api метод:

```
interface Api {
    //...

    @Multipart
    @POST("images/upload")
    fun uploadImage(@Part file : MultipartBody.Part) : Call<SharedImage>
}

```

Теперь надо переписать RequestBody, чтобы в момент отправки данных вызывался калбэк, который будет принимать количество отправленных данных и оставшихся.

```

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
                    listener.onProgress(uploaded.toLong(), len)
                }
            } catch (e: IOException) {
                //@todo catch exception
            } finally {
                file.close()
            }
        }
    }

```

В данном случае только для картинок, а contentType() можно переписать на что-нибудь более динамичное.
 ``` ProgressListener<T> ``` наследует ``` Callback<T> ``` чтобы не писать в функцию два калбека - для прогресса и на результат по завершению.
 
 
Класс ``` FileToUpload ``` это просто обертка содержащая поток байтов файла и информацию о нем.

```

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

```


 Как использовать

```

//...
        val file = FileToUpload(uri)
        file.open(ctx)

        val fileBody = ProgressRequestBody(file, progressListener)
        val filePart = MultipartBody.Part.createFormData("filendName", file.fileName, fileBody)

		//SharedImage - just a my special type.
		
        api.uploadImage(filePart).enqueue(object : UploadManager.ProgressRequestBody.ProgressListener<SharedImage?> {
            override fun onProgress(written: Long, size: Long) {
                handler.post {
					//To be sure it is Main Thread
                    var d : Float = written.toFloat() / size.toFloat()
                    view.onPublishUploadProgress((d * 100).toLong())
                }
            }

            override fun onFailure(call: Call<SharedImage?>, t: Throwable) {
                view.showToast(t.message ?: "ERROR")
            }

            override fun onResponse(call: Call<SharedImage?>, response: Response<SharedImage?>) {
                view.showToast("Succeeded")
            }
        })
//...

```

