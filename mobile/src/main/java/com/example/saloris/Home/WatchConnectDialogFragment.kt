package com.example.saloris.Home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.saloris.R
import com.example.saloris.databinding.DialogWatchConnectBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class WatchConnectDialogFragment(
    watchConnectDialogInterface: WatchConnectDialogInterface,
    pos: Int
) : DialogFragment() {
    private var watchConnectDialogInterface: WatchConnectDialogInterface? = null
    private var pos: Int? = null
    init {
        this.pos = pos
        this.watchConnectDialogInterface = watchConnectDialogInterface
    }

    /* View */
    private var _binding: DialogWatchConnectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogWatchConnectBinding.inflate(layoutInflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.deviceNameTv.text = ""

        binding.yesBtn.setOnClickListener {
            Log.d("WatchConnectDialog", "yes btn")
            this.watchConnectDialogInterface?.onYesButtonClick(pos!!)
            dismiss()
        }

        binding.noBtn.setOnClickListener {
            Log.d("WatchConnectDialog", "no btn")
            dismiss()
        }
        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface WatchConnectDialogInterface {
        fun onYesButtonClick(id: Int)
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