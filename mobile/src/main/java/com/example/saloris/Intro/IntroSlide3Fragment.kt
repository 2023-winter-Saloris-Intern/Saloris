package com.example.saloris.Intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.R
import com.example.saloris.databinding.FragmentIntroSlide3Binding

class IntroSlide3Fragment : Fragment() {
    private lateinit var binding: FragmentIntroSlide3Binding
    private lateinit var navController: NavController

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {

        binding = FragmentIntroSlide3Binding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        navController = Navigation.findNavController(view)
//
//        binding.startBtn.setOnClickListener {
//            navController.navigate(R.id.action_IntroSlide3Fragment_to_homeFragment)
//        }
    }
}