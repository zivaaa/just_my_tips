package com.zivaaa18.chattyapp

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment

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
                activity!!.layoutInflater.inflate(R.layout.waiting_dialog, null)
            )
            .create().apply {
                //Делаем фон диалога прозрачным
                window.setBackgroundDrawableResource(android.R.color.transparent)
            }
    }
}