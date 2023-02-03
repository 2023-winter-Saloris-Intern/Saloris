package com.example.saloris.Help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.saloris.R
import com.example.saloris.databinding.FragmentIntroSlideBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class HelpFragment() : Fragment() {
    private var _binding: FragmentIntroSlideBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroSlideBinding.inflate(inflater, container, false)

        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //1
        setupViewPager()
    }

    private fun setupViewPager() {
        val fragmentList = arrayListOf(
            HelpSlide1Fragment.newInstance(),
            HelpSlide2Fragment.newInstance(),
            HelpSlide3Fragment.newInstance()
        )

        val adapter = ViewPager2Adapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        binding.sliderViewPager.adapter = adapter
        //2
        binding.sliderViewPager.isUserInputEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()

        /* Bottom Menu */
//        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
//        bottomMenu.visibility = View.GONE

        _binding = null
    }
}