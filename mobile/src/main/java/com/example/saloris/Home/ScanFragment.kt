package com.example.saloris.Home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.saloris.R
import com.example.saloris.databinding.FragmentScanBinding
import com.example.saloris.databinding.FragmentSettingBinding

class ScanFragment : Fragment() {
    private lateinit var binding: FragmentScanBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

}