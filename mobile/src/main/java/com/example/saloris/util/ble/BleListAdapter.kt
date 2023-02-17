package com.example.saloris.util.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.saloris.R
import com.example.saloris.databinding.DialogWatchConnectBinding
import com.example.saloris.databinding.ItemListBinding
import com.example.saloris.util.MakeToast


class BleListAdapter : RecyclerView.Adapter<BleListAdapter.RecyclerViewHolder>() {
    var bluetoothDevices: BluetoothDevice? = null
    private lateinit var binding: DialogWatchConnectBinding
    private var bleGatt: BluetoothGatt? = null
    private var mContext:Context? = null
    private lateinit var navController: NavController
    /* Toast */
    private val toast = MakeToast()
    /* Dialog */
    private var name: String = ""
    private var temp: Int = 0
    inner class RecyclerViewHolder(private val binding: ItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("MissingPermission")
        fun bind(bluetoothDevice: BluetoothDevice) {
            val bleName = bluetoothDevice.name ?: "noname"

            //println("$bleName: $bleClass: $bleUUID")
            if (bleName != "noname") {
                binding.bleName.text = bleName
                //binding.bleAddress.text = bleAddress
            }

//            val bundle = bundleOf("name" to bleName, "address" to bleAddress)

//            binding.root.setOnClickListener {
//                it.findNavController().navigate(R.id.action_scanFragment_to_startDriveFragment, bundle)
//                val builder = AlertDialog.Builder(requireContext())
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        bluetoothDevices?.let { holder.bind(it) }
        holder.itemView.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(it.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return@setOnClickListener
            }
            name = getName()!!
            val device = bluetoothDevices
            device?.createBond()
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    fun getName(): String? {
        Log.d("getName", "getName!!!!!!!")
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = bluetoothAdapter.bondedDevices
        for (device in pairedDevices) {
            if (device.name.startsWith("Galaxy Watch")) {
                Log.d("getName", "Name: ${device.name}")
                return device.name
            }
        }
        return null
    }

//    private fun showDialog(context: Context, device: BluetoothDevice, view: View) {
//        val builder = AlertDialog.Builder(context)
//        builder
//            .setTitle(name)
//            .setMessage("이 기기와 연결하시겠습니까?")
//            .setIcon(R.drawable.watch)
//            .setPositiveButton("확인",
//                DialogInterface.OnClickListener { dialog, which ->
//                    toast.makeToast(context, "다이얼로그 확인")
//                    bleGatt = DeviceControlActivity(context, bleGatt).connectGatt(device)
//                    view.findNavController().navigate(R.id.action_scanFragment_to_startDriveFragment)
//                    return@OnClickListener
//                })
//            .setNegativeButton("취소",
//                DialogInterface.OnClickListener { dialog, which ->
//                    toast.makeToast(context, "다이얼로그 취소")
//                    return@OnClickListener
//                })
//        builder.create()
//        builder.show()
//    }

    override fun getItemCount(): Int {
        return 1
    }
}