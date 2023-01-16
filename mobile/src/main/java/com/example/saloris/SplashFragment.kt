package com.example.saloris

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.saloris.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //1
        Handler(Looper.getMainLooper()).postDelayed({
            if (isOnBoardingFinished()) {
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            } else {
                findNavController().navigate(R.id.action_splashFragment_to_IntroSlideFragment)
            }
        }, 5000)
    }

    //2
    private fun isOnBoardingFinished(): Boolean {
        val prefs = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return prefs.getBoolean("finished", false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}