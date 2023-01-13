package com.example.saloris.Intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.saloris.R
import com.example.saloris.databinding.FragmentIntroSlide1Binding

class IntroSlide1Fragment : Fragment() {
    private var _binding: FragmentIntroSlide1Binding? = null
    private val binding get() = _binding!!

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        _binding = FragmentIntroSlide1Binding.inflate(inflater, container, false)
        return binding.root
    }

}