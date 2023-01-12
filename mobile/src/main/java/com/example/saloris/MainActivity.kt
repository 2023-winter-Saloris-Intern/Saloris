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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    /* Toolbar */
    private var currFragment = R.id.homeFragment

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.toolbar_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                android.R.id.home -> {
                    navController.popBackStack()
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
                    binding.layoutToolbar.toolbar.visibility = View.GONE
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

//        addMenuProvider(menuProvider)
        /* Toolbar */
//        with(binding.layoutToolbar.toolbarTitle) {
//            text = "심박수 그래프"
//        }그래프
//        addMenuProvider(menuProvider)
//        setSupportActionBar(binding.layoutToolbar.toolbar)
//        supportActionBar?.let {
//            it.setDisplayShowTitleEnabled(false)
//            it.setDisplayHomeAsUpEnabled(true)
//            it.setHomeAsUpIndicator(R.drawable.ic_back)
//        }
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