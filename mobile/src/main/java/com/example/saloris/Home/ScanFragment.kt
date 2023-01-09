package com.example.saloris.Home

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.util.ble.BleListAdapter
import com.example.saloris.databinding.FragmentScanBinding
import com.example.saloris.util.HEART_RATE_SERVICE_STRING
import com.example.saloris.util.MakeToast
import com.example.saloris.util.SCAN_TIME
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.concurrent.schedule

class ScanFragment : Fragment() {
    /* View */
    private lateinit var navController: NavController
    private lateinit var binding: FragmentScanBinding

    /* Toast */
    private val toast = MakeToast()

    /* User Authentication */
    private lateinit var auth: FirebaseAuth

    /* Tutorial */
    private fun isFirst(): Boolean {
        val firstPref = requireContext().getSharedPreferences("isFirst", Activity.MODE_PRIVATE)
        return firstPref.getBoolean("scan", true)
    }

    private fun setFirstFalse() {
        val firstPref = requireContext().getSharedPreferences("isFirst", Activity.MODE_PRIVATE)
        val firstEdit = firstPref.edit()
        firstEdit.putBoolean("scan", false)
        firstEdit.apply()
    }

    /* Permission */
    private val locationPermissionList = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

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
                AlertDialog.Builder(requireContext())
                    .setTitle("알림")
                    .setMessage("권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                    .setPositiveButton("설정") { _, _ -> openAndroidSetting() }
                    .setNegativeButton("취소", null)
                    .create()
                    .show()
            } else {
                binding.deviceScanBtn.isChecked = true
            }
        }

    private fun openAndroidSetting() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            addCategory(Intent.CATEGORY_DEFAULT)
            data = Uri.parse("package:${activity?.packageName}")
        }
        startActivity(intent)
    }
    private val wachInfo = mutableListOf<WachInfo>()
    private val WatchListAdapter = WatchListAdapter(wachInfo)


    /* BLE */
    private val bleListAdapter = BleListAdapter()

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var scanResults: ArrayList<BluetoothDevice> = ArrayList()
    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            addScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                addScanResult(result)
            }
        }

        override fun onScanFailed(_error: Int) {
            Log.e("Scan Fail Code", "$_error")
        }

        private fun addScanResult(result: ScanResult) {
            val device: BluetoothDevice = result.device

            for (scanResult in scanResults) {
                if (device.address == scanResult.address) return
            }
            scanResults.add(result.device)
            bleListAdapter.notifyItemInserted(scanResults.size - 1)
        }
    }

    private fun startScan() {
        val filters: MutableList<ScanFilter> = ArrayList()
        val scanFilter: ScanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID.fromString(HEART_RATE_SERVICE_STRING)))
            .build()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
        scanResults.clear()

        /* Permission */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                binding.deviceScanBtn.post { binding.deviceScanBtn.isChecked = false }
                requestPermissionLauncher.launch(bluetoothPermissionList)
                return
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                binding.deviceScanBtn.post { binding.deviceScanBtn.isChecked = false }
                requestPermissionLauncher.launch(locationPermissionList)
                return
            }
        }

        /* BLE */
        if (bluetoothAdapter?.isEnabled == true) {
            filters.add(scanFilter)
            Log.d("State", "Start Scan!")
            bluetoothAdapter?.bluetoothLeScanner?.startScan(filters, settings, scanCallback)
        }
    }
    private fun stopScan() {
        /* Permission */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        if (bluetoothAdapter?.isEnabled == true) {
            Log.d("State", "Stop Scan!")
            bluetoothAdapter!!.bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* User Authentication */
        auth = Firebase.auth

        /* Toolbar */
//        toolbar = (requireActivity() as MainActivity).binding.layoutToolbar.toolbar
//        toolbar.addOnLayoutChangeListener(onLayoutChangeListener)
//        setMenuCamera()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanBinding.inflate(inflater,container,false)
        bleListAdapter.bluetoothDevices = scanResults
        binding.watchListRecyclerview.adapter = bleListAdapter
        return binding.root

//        wachInfo.add(
//            WachInfo("R.drawable.watch","000","galaxy Watch 5"))
//        wachInfo.add(
//            WachInfo("R.drawable.watch","000","galaxy Watch Active"))
//        wachInfo.add(
//            WachInfo("R.drawable.watch","000","galaxy Watch 4"))
//
//        binding.watchListRecyclerview.layoutManager= LinearLayoutManager(requireContext())
//        binding.watchListRecyclerview.adapter=WatchListAdapter
//        WatchListAdapter.setItemClickListener(object : WatchListAdapter.OnItemClickListener{
//            override fun onClick(v: View, position: Int) {
//                //loadFragment(DetailInformationFragment())
//            }
//        })

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        /* Tutorial */
        if (isFirst()) {
            // Tutorial
            println("Welcome to Tutorial!") // TODO: 튜토리얼 화면 구현
            setFirstFalse()
        }

//        binding.userName.text = auth.currentUser!!.displayName
        binding.deviceScanBtn.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                button.isEnabled = false
                //binding.loading.visibility = View.VISIBLE

                if (bluetoothAdapter == null) {
                    context?.let { toast.makeToast(it, "블루투스를 지원하지 않습니다.") }
                    button.post { button.isChecked = false }
                    return@setOnCheckedChangeListener
                }
                if (!bluetoothAdapter!!.isEnabled) {
                    context?.let { toast.makeToast(it, "블루투스가 꺼져 있습니다.") }
                    button.post { button.isChecked = false }
                    return@setOnCheckedChangeListener
                }

                startScan()
                Timer(false).schedule(SCAN_TIME) {
                    button.post { button.isChecked = false }
                }
            } else {
                button.isEnabled = true
                //binding.loading.visibility = View.GONE

                stopScan()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        binding.deviceScanBtn.isChecked = true
    }

    override fun onDestroyView() {
        super.onDestroyView()

//        /* Toolbar */
//        val toolbar = (requireActivity() as MainActivity).binding.layoutToolbar.toolbar
//        toolbar.removeOnLayoutChangeListener(onLayoutChangeListener)
//        menuCamera?.isVisible = false
    }

}