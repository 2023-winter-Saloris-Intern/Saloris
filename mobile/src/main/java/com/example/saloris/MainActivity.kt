package com.example.saloris

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent

import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import com.example.saloris.Home.HomeFragment
import com.example.saloris.Record.RecordFragment
import com.example.saloris.Setting.SettingFragment


class MainActivity : AppCompatActivity() {

    lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        Log.d("LifeCycleTest","onCreate")
        loadFragment(HomeFragment())
        bottomNav = findViewById(R.id.bottomNav) as BottomNavigationView
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    Log.d("clickTest","homeclick!")
                    loadFragment(HomeFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.record -> {
                    Log.d("clickTest","friendclick!")
                    loadFragment(RecordFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.setting -> {
                    Log.d("clickTest","mypagelick!")
                    loadFragment(SettingFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

    }
    private fun loadFragment(fragment: Fragment){
        Log.d("clickTest","click!->"+fragment.tag)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}