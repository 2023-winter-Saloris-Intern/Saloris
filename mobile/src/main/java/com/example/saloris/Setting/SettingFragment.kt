package com.example.saloris.Setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import com.example.saloris.R
import com.example.saloris.databinding.FragmentSettingBinding
import com.example.saloris.util.OpenDialog

class SettingFragment : Fragment() {

    /* View */
    private lateinit var binding: FragmentSettingBinding
    private lateinit var navController: NavController

    /* Dialog */
    private val dialog = OpenDialog()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

}