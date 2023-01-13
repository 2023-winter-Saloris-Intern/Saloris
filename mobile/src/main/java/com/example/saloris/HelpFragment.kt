package com.example.saloris

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.databinding.FragmentHelpBinding
import com.example.saloris.databinding.FragmentSettingBinding
import com.example.saloris.util.MakeToast
import com.google.android.material.bottomnavigation.BottomNavigationView

class HelpFragment : Fragment() {

    /* View */
    private lateinit var binding: FragmentHelpBinding
    private lateinit var navController: NavController

    /* Toast */
    private val toast = MakeToast()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHelpBinding.inflate(layoutInflater, container, false)

        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        navController.navigate(R.id.action_helpFragment_to_IntroSlideFragment)
    }
}