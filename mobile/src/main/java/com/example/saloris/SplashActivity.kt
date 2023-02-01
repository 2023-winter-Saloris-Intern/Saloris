package com.example.saloris

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ContentInfo
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.saloris.databinding.ActivityMainBinding
import com.example.saloris.databinding.ActivitySplashBinding
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper.replace

class SplashActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    lateinit var binding: ActivitySplashBinding

//    private var currFragment = R.id.splashFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 5000)
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            if (isOnBoardingFinished()) {
//                navController.navigate(R.id.action_splashFragment_to_loginFragment)
//            } else {
//                navController.navigate(R.id.action_splashFragment_to_IntroSlideFragment)
//            }
//        }, 5000)

//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        navController = navHostFragment.navController

    }

    private fun isOnBoardingFinished(): Boolean {
        val prefs = getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return prefs.getBoolean("finished", false)
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}

//Handler(Looper.getMainLooper()).postDelayed({
//    if (isIntroSlideFinished()) {
//        val intent = Intent(applicationContext, MainActivity::class.java)
//        startActivity(intent)
//        finish()
//    } else {
//
//    }
//}, 5000)