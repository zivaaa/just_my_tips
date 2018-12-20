# Выбор файла

Чтобы позволить юзеру выбрать файл, надо
 - вызвать неявный интент
 - получить результат

 Следущий класс позволяет вызвать активити выбора файла, а потом получить URi этого файла из результата.
 В данном случае выбор изображения.

 ```
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

 ```

 ### Использование


 ```
//...

private val oneImagePickManager = OneImagePickManager()

//...

fun chooseImage() {
    oneImagePickManager.pickFile(view as Activity)
}

//...

fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode != Activity.RESULT_OK) {
        return
    }

    when (requestCode) {
        OneImagePickManager.ONE_IMAGE_PICK_CODE -> {
            val uri = oneImagePickManager.onPickFileResult(data!!)
            previewImage.setImageURI(uri)
        }
    }
}

 ```
