package com.example.saloris.Intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.saloris.MainActivity
import com.example.saloris.R
import com.example.saloris.databinding.FragmentIntroSlide1Binding
import com.example.saloris.databinding.FragmentIntroSlide2Binding
import com.google.android.material.bottomnavigation.BottomNavigationView

//class IntroSlide2Fragment : Fragment() {
//    private var _binding: FragmentIntroSlide2Binding? = null
//    private val binding get() = _binding!!
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
//        _binding = FragmentIntroSlide2Binding.inflate(inflater, container, false)
//        return binding.root
//    }
// }

class IntroSlide2Fragment : Fragment() {

    private var _binding: FragmentIntroSlide2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroSlide2Binding.inflate(inflater, container, false)

        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.sliderViewPager)

        binding.goBackBtn.setOnClickListener {
            viewPager?.currentItem = 0
        }
        binding.goNextBtn.setOnClickListener {
            viewPager?.currentItem = 2
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = IntroSlide2Fragment()
    }

}
