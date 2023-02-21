package com.example.saloris

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.res.Configuration
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saloris.RequiredInfo.RequiredInfo
import com.example.saloris.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.net.Socket


class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.S)
    private val bluetoothPermissionList = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val deniedList = result.filter { !it.value }.map { it.key }
            Log.d("State", "$deniedList")
            if (deniedList.isNotEmpty()) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                    .setPositiveButton("설정") { _, _ -> openAndroidSetting() }
                    .setNegativeButton("취소", null)
                    .create()
                    .show()
            } else {
            }
        }
    private fun openAndroidSetting() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            addCategory(Intent.CATEGORY_DEFAULT)
            data = Uri.parse("package:${this@MainActivity?.packageName}")
        }
        startActivity(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        with(newConfig) {
            println(orientation)
            println(screenLayout)
        }
    }

    /* User Authentication */
    //private lateinit var auth: FirebaseAuth
    /* User Authentication */
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
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
                //이 프래그먼트들은 바텀바가 안보임
                R.id.driveFragment,
                R.id.settingsFragment,
                R.id.IntroSlideFragment,
                R.id.IntroSlide1Fragment,
                R.id.loginStartFragment,
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.registerSuccessFragment,
                R.id.findPasswordFragment,
                R.id.requiredInfo1Fragment,
                R.id.requiredInfo2Fragment,
                R.id.requiredInfo3Fragment,
                R.id.requiredInfo4Fragment,
                R.id.requiredInfo5Fragment,
                R.id.requiredInfoFragment,
                -> {
                    binding.bottomNav.visibility = View.GONE
                    binding.layoutToolbar.toolbar.visibility = View.GONE
                }
                else -> {
                    //나머지 프래그먼트들은 바텀바가 보임
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* view */
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //bluetooth permission
        requestPermissionLauncher.launch(bluetoothPermissionList)
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
        //Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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