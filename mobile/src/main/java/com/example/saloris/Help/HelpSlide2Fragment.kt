
package com.example.saloris.Help

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.saloris.R
import androidx.viewpager2.widget.ViewPager2
import com.example.saloris.Intro.IntroSlide2Fragment

import com.example.saloris.databinding.FragmentHelpSlide1Binding
import com.example.saloris.databinding.FragmentHelpSlide2Binding
import com.example.saloris.databinding.FragmentIntroSlide1Binding
import com.example.saloris.databinding.FragmentIntroSlide2Binding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HelpSlide2Fragment : Fragment() {

    private var _binding: FragmentHelpSlide2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpSlide2Binding.inflate(inflater, container, false)

        /* Bottom Menu */
//        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
//        bottomMenu.visibility = View.GONE

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
        fun newInstance() = HelpSlide2Fragment()
    }

}