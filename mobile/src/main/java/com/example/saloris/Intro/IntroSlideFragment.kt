package com.example.saloris.Intro

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.saloris.databinding.FragmentIntroSlideBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator


class IntroSlideFragment() : Fragment() {
    lateinit var binding: FragmentIntroSlideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentIntroSlideBinding.inflate(layoutInflater)
        //setContentView(binding.root)

        initViewPager()

        //return binding.root
    }

    private fun initViewPager() {
        //ViewPager2 Adapter 셋팅
        var viewPager2Adatper = ViewPager2Adapter(this)
        viewPager2Adatper.addFragment(IntroSlide1Fragment())
        viewPager2Adatper.addFragment(IntroSlide2Fragment())
        viewPager2Adatper.addFragment(IntroSlide3Fragment())

        //Adapter 연결
        binding.sliderViewPager.apply {
            adapter = viewPager2Adatper

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                }
            })
        }

        //ViewPager, TabLayout 연결
        TabLayoutMediator(binding.tapLayoutIntro, binding.sliderViewPager) { tab, position ->
            Log.e("YMC", "ViewPager position: ${position}")
            when (position) {
                0 -> tab.text = "Tab1"
                1 -> tab.text = "Tab2"
                2 -> tab.text = "Tab3"
            }
        }.attach()
    }
}