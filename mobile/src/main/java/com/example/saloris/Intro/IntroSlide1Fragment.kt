package com.example.saloris.Intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.saloris.R
import com.example.saloris.databinding.FragmentIntroSlide1Binding

class IntroSlide1Fragment : Fragment() {
//    private var _binding: FragmentIntroSlide1Binding? = null
//    private val binding get() = _binding!!
//
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
//        _binding = FragmentIntroSlide1Binding.inflate(inflater, container, false)
//        return binding.root
//    }

    private var _binding: FragmentIntroSlide1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroSlide1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.sliderViewPager)

        binding.goNextBtn.setOnClickListener {
            viewPager?.currentItem = 1
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = IntroSlide1Fragment()
    }


}