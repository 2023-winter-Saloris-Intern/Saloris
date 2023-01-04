package com.example.saloris

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.res.Configuration
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import com.example.saloris.Home.HomeFragment
import com.example.saloris.Record.RecordFragment
import com.example.saloris.Setting.SettingFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.saloris.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        with(newConfig) {
            println(orientation)
            println(screenLayout)
        }
    }

    /* User Authentication */
    private lateinit var auth: FirebaseAuth

    /* View */
    private lateinit var navController: NavController
    lateinit var binding: ActivityMainBinding

    lateinit var bottomNav: BottomNavigationView
    /* Toolbar */
    private var currFragment = R.id.homeFragment

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
    private val onDestinationChangedListener =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            currFragment = destination.id
            when (currFragment) {
                R.id.registerFragment,
                R.id.homeFragment,
                R.id.scanFragment,
                R.id.graphFragment, //R.id.graphHrFragment,
                R.id.settingsFragment, R.id.accountFragment -> {
                    binding.layoutToolbar.toolbar.visibility = View.VISIBLE
//                    supportActionBar?.setDisplayHomeAsUpEnabled(FRAGMENT_INFO[destination.id]!!.second)
//                    binding.layoutToolbar.toolbarTitle.text =
//                        getString(FRAGMENT_INFO[destination.id]!!.first)
                }
                else -> {
                    binding.layoutToolbar.toolbar.visibility = View.GONE
                }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* view */
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overridePendingTransition(0, 0)
        /* Status Bar & Navigation Bar */
        val barColor = ContextCompat.getColor(this, R.color.white)
        with(window) {
            statusBarColor = barColor
            navigationBarColor = barColor
        }
        with(WindowInsetsControllerCompat(window, window.decorView)) {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        /* User Authentication */
        auth = FirebaseAuth.getInstance()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener(onDestinationChangedListener)

        /* Bottom Menu */
        binding.bottomNav.apply {
            setupWithNavController(navController)
            setOnItemSelectedListener { item ->
                NavigationUI.onNavDestinationSelected(item, navController, false)
                true
            }
        }
    }
}
//        Log.d("LifeCycleTest", "onCreate")
//        loadFragment(HomeFragment())
//        //bottomNav = findViewById(R.id.bottomNav) as BottomNavigationView
//        binding.bottomNav.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.home -> {
//                    Log.d("clickTest", "homeclick!")
//                    loadFragment(HomeFragment())
//                    return@setOnItemSelectedListener true
//                }
//                R.id.record -> {
//                    Log.d("clickTest", "friendclick!")
//                    loadFragment(RecordFragment())
//                    return@setOnItemSelectedListener true
//                }
//                R.id.setting -> {
//                    Log.d("clickTest", "mypagelick!")
//                    loadFragment(SettingFragment())
//                    return@setOnItemSelectedListener true
//                }
//            }
//            false
//        }
//    }
//    private fun loadFragment(fragment: Fragment) {
//        Log.d("clickTest", "click!->" + fragment.tag)
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.fragment_container, fragment)
//        transaction.addToBackStack(null)
//        transaction.commit()
//    }