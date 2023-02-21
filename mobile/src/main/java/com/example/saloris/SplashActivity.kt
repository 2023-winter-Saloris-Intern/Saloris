package com.example.saloris

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.saloris.databinding.ActivitySplashBinding


class SplashActivity : AppCompatActivity() {

    private val binding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //화면 클릭시 MainActivity로
        findViewById<ConstraintLayout>(com.example.saloris.R.id.splash_activity).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            handler?.removeCallbacksAndMessages(null)
        }
        setInitialize()
    }

    private fun setInitialize() {
        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Handler가 메모리 누수를 일으키지 않도록 합니다.
        handler?.removeCallbacksAndMessages(null)
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

