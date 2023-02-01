package com.example.saloris

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.saloris.databinding.ActivitySplashBinding


class SplashActivity : AppCompatActivity() {

//    private lateinit var navController: NavController
    lateinit var binding: ActivitySplashBinding

    var fragment: Fragment = SplashFragment()

//    private var currFragment = R.id.splashFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

//        val handler = Handler()
//        handler.postDelayed({
//            val intent = Intent(applicationContext, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }, 5000)

        supportFragmentManager.beginTransaction()
            .add(R.id.content, SplashFragment())
            .addToBackStack(SplashActivity::class.java.getSimpleName())
            .commit()
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