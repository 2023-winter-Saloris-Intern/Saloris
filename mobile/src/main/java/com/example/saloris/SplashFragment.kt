package com.example.saloris

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.saloris.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

//    val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//    val navController = navHostFragment.navController
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        //1
        Handler(Looper.getMainLooper()).postDelayed({
            if (isOnBoardingFinished()) {

                if(!isLoginFinished()) {
                    navController.navigate(R.id.action_splashFragment_to_HomeFragment)
                }

                else if(isLoginFinished()) {
                    navController.navigate(R.id.action_splashFragment_to_loginStartFragment)
                }

            } else {
                navController.navigate(R.id.action_splashFragment_to_IntroSlideFragment)
            }
        }, 5000)
    }

//        val handler = Handler()
//        handler.postDelayed({
//            val intent = Intent(applicationContext, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }, 5000)

    //2
    private fun isOnBoardingFinished(): Boolean {
        val prefs = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return prefs.getBoolean("finished", false)
    }

    private fun isLoginFinished(): Boolean {
        val autoLoginPref =
            requireActivity().getSharedPreferences("autoLogin", Activity.MODE_PRIVATE)
        return autoLoginPref.getBoolean("finished", true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}