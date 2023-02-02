package com.example.saloris.Intro

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.saloris.MainActivity
import com.example.saloris.R
import com.example.saloris.databinding.FragmentIntroSlide3Binding
import com.google.android.material.bottomnavigation.BottomNavigationView

class IntroSlide3Fragment : Fragment() {
//    private lateinit var binding: FragmentIntroSlide3Binding
//    private lateinit var navController: NavController
//
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
//
//        binding = FragmentIntroSlide3Binding.inflate(inflater, container, false)
//
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
//
//        binding.startBtn.setOnClickListener {
//            navController.navigate(R.id.action_IntroSlide3Fragment_to_homeFragment)
//        }
//    }

    private var _binding: FragmentIntroSlide3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroSlide3Binding.inflate(inflater, container, false)

        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.sliderViewPager)

        binding.goBackBtn.setOnClickListener {
            viewPager?.currentItem = 1
        }
        binding.startBtn.setOnClickListener {
            findNavController().navigate(R.id.action_IntroSlideFragment_to_loginStartFragment)
            onBoardingFinished()
        }
    }

    private fun onBoardingFinished() {
        val prefs = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("finished", true).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        _binding = null
    }

    companion object {
        fun newInstance() = IntroSlide3Fragment()
    }

}