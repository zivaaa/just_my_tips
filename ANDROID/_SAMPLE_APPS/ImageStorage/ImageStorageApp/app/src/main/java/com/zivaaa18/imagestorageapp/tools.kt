package com.zivaaa18.imagestorageapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager



class Tools {
    companion object {
        fun isOnline(ctx : Context) : Boolean {
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            val netInfo = cm!!.activeNetworkInfo
            return netInfo != null && netInfo.isConnected
        }

        fun copyToClipboard(ctx : Context, text : String) {
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData : ClipData = ClipData.newPlainText("shared image url", text)
            clipboard.primaryClip = clipData
        }
    }
}