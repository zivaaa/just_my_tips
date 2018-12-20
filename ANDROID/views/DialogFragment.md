# DialogFragment 

Тут будет пара примеров и некоторые настройки DialogFragment, который используется при построении диалоговых окон в приложениях.

1. Простой алерт с одной кнопкой OK. 

``` 
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment

class ShowSubjectDialog : DialogFragment() {
    companion object {
        val DATA_KEY: String = "data_subject"

        /**
            Метод обертка создающий фрагмент и задающий в него некоторые начальные данные,
            которые можно будет прочитать позже
         */

        fun getInstance(dataToShow: String = ""): ShowSubjectDialog {
            return ShowSubjectDialog().apply {
                var bundle: Bundle = Bundle()
                bundle.putString(DATA_KEY, dataToShow)
                arguments = bundle
            }
        }
    }

    /**
     * Создаем алерт и настраиваем его
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dataOfSubject = arguments?.getString(DATA_KEY, "no value saved") //ищем сохраненные данные
        return AlertDialog.Builder(activity)
            .setTitle(dataOfSubject)  //задаем заголовок
            .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                //нстраиваем обработчик на клик по кнопке OK
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    //Some action
                }
            })
            .create()
    }
}
```

1.1. Вызываем его в активити.

```
ShowSubjectDialog.getInstance("some Title").show(supportFragmentManager, "some_new_tag")
```

2.0.  Прозрачный, модальный диалог (не закрывается при нажатии на область ) со спиннером.

spinner.xml - наполнение диалога которое будем встраивать при создании. Тут только тест и спиннер.

``` 
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
                                             xmlns:app="http://schemas.android.com/apk/res-auto"
                                             xmlns:tools="http://schemas.android.com/tools"
                                             android:layout_width="match_parent"
                                             android:layout_height="match_parent"

>

    <ProgressBar
            style="?android:attr/progressBarStyle"
            android:layout_width="120dp"
            android:layout_height="120dp"
            android:id="@+id/progressBar" android:layout_marginTop="8dp"
            app:layout_constraintTop_toTopOf="parent" android:layout_marginBottom="8dp"
            app:layout_constraintBottom_toBottomOf="parent" app:layout_constraintStart_toStartOf="parent"
            android:layout_marginStart="8dp" app:layout_constraintEnd_toEndOf="parent" android:layout_marginEnd="8dp"/>
    <TextView
            android:text="Just loading..."
            android:textColor="@color/colorAccent"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/textView" app:layout_constraintStart_toStartOf="parent" android:layout_marginStart="8dp"
            app:layout_constraintEnd_toEndOf="parent" android:layout_marginEnd="8dp"
            app:layout_constraintTop_toBottomOf="@+id/progressBar" android:layout_marginTop="8dp"
            app:layout_constraintHorizontal_bias="0.501"/>
</android.support.constraint.ConstraintLayout>
```

Тут мы также имеем getInstance метод который будет создавать фрагмент. Но он еще и задает ``` setCancelable(false) ```. 
Этот диалог не будет скрыт пока его не закроем из кода.

```
class SpinnerDialog : DialogFragment() {
    companion object {
        fun getInstance()  : SpinnerDialog{
            return SpinnerDialog().apply { setCancelable(false) }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NO_TITLE, theme)
        return AlertDialog.Builder(activity)
            .setView(
                activity!!.layoutInflater.inflate(R.layout.spinner, null)
            )
            .create().apply {
                //Делаем фон диалога прозрачным
                window.setBackgroundDrawableResource(android.R.color.transparent)
            }
    }
}
```

2.1 Показываем из активити и сворачиаем из нее же. Осталось только вызвать метод ``` setLoadingState() ``` и все.

``` 

    var dialog: SpinnerDialog? = null

    fun setLoadingState() {
        dialog = SpinnerDialog.getInstance()
        dialog?.show(supportFragmentManager, "just_a_tag")
    }

    fun unsetLoadingState() {
        if (dialog != null) {
            dialog?.dismiss()
        }
    }

```

3. Возврат занчений в активити.

``` @TODO ```
