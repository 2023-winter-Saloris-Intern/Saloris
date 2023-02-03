package com.example.saloris.Intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.saloris.R
import com.example.saloris.databinding.FragmentIntroSlideBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class IntroSlideFragment() : Fragment() {
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
            IntroSlide1Fragment.newInstance(),
            IntroSlide2Fragment.newInstance(),
            IntroSlide3Fragment.newInstance()
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
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        _binding = null
    }
}

//    lateinit var binding: FragmentIntroSlideBinding
//    private var viewPager: ViewPager2? = null
////    private lateinit var navController: NavController
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        val view: View = inflater.inflate(R.layout.fragment_intro_slide, container, false)
//        viewPager = view.findViewById(R.id.sliderViewPager)
//
//        /* Bottom Menu */
//        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
//        bottomMenu.visibility = View.GONE
//
//        return view
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        val pagerAdapter = ViewPager2Adapter(requireActivity())
//        // 6개의 fragment add
//        pagerAdapter.addFragment(IntroSlide1Fragment())
//        pagerAdapter.addFragment(IntroSlide2Fragment())
//        pagerAdapter.addFragment(IntroSlide3Fragment())
//
//        // adapter 연결
//        viewPager?.adapter = pagerAdapter
//        viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                Log.e("ViewPagerFragment", "Page ${position + 1}")
//            }
//        })
//
//        binding.sliderViewPager.adapter = pagerAdapter
//        //2
//        binding.sliderViewPager.isUserInputEnabled = false

//        navController = Navigation.findNavController(view)
//
//        binding.startBtn.setOnClickListener {
//            navController.navigate(R.id.action_IntroSlideFragment_to_homeFragment)
//        }
//
//        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
//
//        navController.navigate(R.id.action_IntroSlideFragment_to_homeFragment)
//
//    }
//}

//}

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = FragmentIntroSlideBinding.inflate(layoutInflater)
//        //setContentView(binding.root)
//
//        binding.sliderViewPager.adapter = ViewPager2Adapter(this)
//
////        initViewPager()
//
//        //return binding.root
//    }

//    private fun initViewPager() {
//        //ViewPager2 Adapter 셋팅
//        var viewPager2Adatper = ViewPager2Adapter(this)
//        viewPager2Adatper.addFragment(IntroSlide1Fragment())
//        viewPager2Adatper.addFragment(IntroSlide2Fragment())
//        viewPager2Adatper.addFragment(IntroSlide3Fragment())
//
//        //Adapter 연결
//        binding.sliderViewPager.apply {
//            adapter = viewPager2Adatper
//
//            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//                override fun onPageSelected(position: Int) {
//                    super.onPageSelected(position)
//                }
//            }) //ViewPager, TabLayout 연결
////        TabLayoutMediator(binding.tapLayoutIntro, binding.sliderViewPager) { tab, position ->
////            Log.e("YMC", "ViewPager position: ${position}")
////            when (position) {
////                0 -> tab.text = "Tab1"
////                1 -> tab.text = "Tab2"
////                2 -> tab.text = "Tab3"
////            }
////        }.attach()
//        }
//        TabLayoutMediator(binding.tapLayoutIntro, binding.sliderViewPager) { tab, position ->
//            Log.e("YMC", "ViewPager position: ${position}")
//            when (position) {
//                0 -> tab.text = "Tab1"
//                1 -> tab.text = "Tab2"
//                2 -> tab.text = "Tab3"
//            }
//        }.attach()
//    }
//}