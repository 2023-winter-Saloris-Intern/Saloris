package com.example.saloris.util.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.saloris.R
import com.example.saloris.databinding.ConnectingWatchRecyclerviewBinding
import com.example.saloris.databinding.ItemListBinding

class BleListAdapter : RecyclerView.Adapter<BleListAdapter.RecyclerViewHolder>() {
    var bluetoothDevices: ArrayList<BluetoothDevice> = ArrayList()

    inner class RecyclerViewHolder(private val binding: ItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.R)
        @SuppressLint("MissingPermission")
        fun bind(bluetoothDevice: BluetoothDevice) {
            val bleName = bluetoothDevice.name ?: "noname"
            val bleAddress = bluetoothDevice.address
            val bleClass = bluetoothDevice.bluetoothClass
            val bleType = bluetoothDevice.type
            val bleUUID = bluetoothDevice.uuids ?: "nouuid"
            val ble = bluetoothDevice.bondState
            println("$bleName: $bleClass: $ble")
            if (bleName != "noname") {
                binding.bleName.text = bleName
                //binding.bleAddress.text = bleAddress
                binding.bleAddress.text = ble.toString()
            }


            val bundle = bundleOf("name" to bleName, "address" to bleAddress)

            binding.root.setOnClickListener {
                it.findNavController().navigate(R.id.action_scanFragment_to_recordFragment, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return RecyclerViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.bind(bluetoothDevices[position])
    }

    override fun getItemCount(): Int {
        return bluetoothDevices.size
    }
}