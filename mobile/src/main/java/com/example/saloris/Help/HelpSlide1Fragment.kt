package com.example.saloris.Help

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.saloris.R
import com.example.saloris.databinding.FragmentHelpSlide1Binding
import com.example.saloris.databinding.FragmentIntroSlide1Binding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HelpSlide1Fragment : Fragment() {

    private var _binding: FragmentHelpSlide1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpSlide1Binding.inflate(inflater, container, false)

        /* Bottom Menu */
//        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
//        bottomMenu.visibility = View.GONE

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

//        /* Bottom Menu */
//        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
//        bottomMenu.visibility = View.GONE

        _binding = null
    }

    companion object {
        fun newInstance() = HelpSlide1Fragment()
    }

}