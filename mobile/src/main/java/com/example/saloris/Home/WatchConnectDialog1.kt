package com.example.saloris.Home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.saloris.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PartySortDialog1Fragment() : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_watch_connect_dialog1, container, false)

        container?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.findViewById<TextView>(R.id.no_btn)?.setOnClickListener {
            dismiss()
        }
    }
}