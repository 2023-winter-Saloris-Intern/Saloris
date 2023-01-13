package com.example.saloris.util

import android.app.Dialog
import android.content.Context
import com.example.saloris.R

class CustomDialog(context: Context) : Dialog(context) {
    private val dialog = Dialog(context)

    fun showDia(){
        dialog.setContentView(R.layout.dialog_watch_connect)
        dialog.show()
    }
}