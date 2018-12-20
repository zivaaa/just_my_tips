package com.zivaaa18.imagestorageapp

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ProgressDialogFragment : DialogFragment() {
    companion object {
        val SHOW_TAG = "progress_dialog"

        fun getInstance(): ProgressDialogFragment {
            return ProgressDialogFragment().apply {
                isCancelable = false
            }
        }
    }

    lateinit var progress: ProgressBar
    lateinit var progressText: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NO_TITLE, theme)
        val view = activity!!.layoutInflater.inflate(R.layout.progress_layout, null)
        progress = view!!.findViewById(R.id.progressUploading)
        progressText = view!!.findViewById(R.id.progressText)

        return AlertDialog.Builder(activity)
            .setView(view)
            .create()
            .also {
                it.window.setBackgroundDrawableResource(android.R.color.transparent)
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    fun setProgress(percentage: Int) {
        progress.setProgress(percentage)
        progressText.text = "${percentage} %"
    }
}