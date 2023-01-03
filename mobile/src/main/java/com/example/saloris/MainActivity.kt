package com.example.saloris

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent

import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import com.example.saloris.Home.HomeFragment
import com.example.saloris.Record.RecordFragment
import com.example.saloris.Setting.SettingFragment

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.saloris.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityMainBinding
    lateinit var bottomNav: BottomNavigationView
    /* Toolbar */
    //private var currFragment = R.id.mainFragment

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.toolbar_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                android.R.id.home -> {
                    //navController.popBackStack()
                }
            }
            return true
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* view */
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overridePendingTransition(0, 0)

        /* User Authentication */
        auth = FirebaseAuth.getInstance()
        //setContentView(R.layout.fragment_login)
        Log.d("LifeCycleTest", "onCreate")
        loadFragment(HomeFragment())
        //bottomNav = findViewById(R.id.bottomNav) as BottomNavigationView
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    Log.d("clickTest", "homeclick!")
                    loadFragment(HomeFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.record -> {
                    Log.d("clickTest", "friendclick!")
                    loadFragment(RecordFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.setting -> {
                    Log.d("clickTest", "mypagelick!")
                    loadFragment(SettingFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

    }

    private fun loadFragment(fragment: Fragment) {
        Log.d("clickTest", "click!->" + fragment.tag)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}