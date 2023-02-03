package com.example.saloris.Help

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.saloris.Intro.IntroSlide3Fragment
import com.example.saloris.R
import com.example.saloris.databinding.FragmentHelpSlide2Binding
import com.example.saloris.databinding.FragmentHelpSlide3Binding
import com.example.saloris.databinding.FragmentIntroSlide3Binding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HelpSlide3Fragment : Fragment() {

    private var _binding: FragmentHelpSlide3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpSlide3Binding.inflate(inflater, container, false)

        /* Bottom Menu */
//        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
//        bottomMenu.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.sliderViewPager)

        binding.goBackBtn.setOnClickListener {
            viewPager?.currentItem = 1
        }
        binding.startBtn.setOnClickListener {
            findNavController().navigate(R.id.action_helpFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

//        /* Bottom Menu */
//        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
//        bottomMenu.visibility = View.GONE

        _binding = null
    }

    companion object {
        fun newInstance() = HelpSlide3Fragment()
    }

}