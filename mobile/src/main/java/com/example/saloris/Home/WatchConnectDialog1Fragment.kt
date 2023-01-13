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
import com.example.saloris.databinding.DialogFindPasswordBinding
import com.example.saloris.databinding.FragmentWatchConnectDialog1Binding
import com.example.saloris.util.OpenDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WatchConnectDialog1Fragment() : DialogFragment() {

    /* View */
    private lateinit var binding: FragmentWatchConnectDialog1Binding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentWatchConnectDialog1Binding.inflate(layoutInflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 확인 버튼 입력
        binding.yesBtn.setOnClickListener { dismiss() }
    }


//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        super.onCreateView(inflater, container, savedInstanceState)
//        return inflater.inflate(R.layout.fragment_watch_connect_dialog1, container, false)
//
//        container?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        view?.findViewById<TextView>(R.id.no_btn)?.setOnClickListener {
//            dismiss()
//        }
//    }
}