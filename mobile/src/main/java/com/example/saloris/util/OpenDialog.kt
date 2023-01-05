package com.example.saloris.util

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

class OpenDialog {
    fun openDialog(activity: FragmentActivity, dialog: DialogFragment, tagName: String) {
        dialog.isCancelable = true
        dialog.arguments = Bundle().apply { putString("contents", tagName) }
        dialog.show(activity.supportFragmentManager, tagName)
    }
}