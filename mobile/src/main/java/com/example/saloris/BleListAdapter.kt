package com.example.saloris

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.saloris.databinding.ConnectingWatchRecyclerviewBinding

class BleListAdapter : RecyclerView.Adapter<BleListAdapter.RecyclerViewHolder>() {
    var bluetoothDevices: ArrayList<BluetoothDevice> = ArrayList()

    inner class RecyclerViewHolder(private val binding: ConnectingWatchRecyclerviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("MissingPermission")
        fun bind(bluetoothDevice: BluetoothDevice) {
            val bleName = bluetoothDevice.name ?: "noname"
            val bleAddress = bluetoothDevice.address

            binding.bleName.text = bleName
            //binding.bleAddress.text = bleAddress

            val bundle = bundleOf("name" to bleName, "address" to bleAddress)

//            binding.root.setOnClickListener {
//                it.findNavController().navigate(R.id.action_scanFragment_to_stateActivity, bundle)
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val binding = ConnectingWatchRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return RecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.bind(bluetoothDevices[position])
    }

    override fun getItemCount(): Int {
        return bluetoothDevices.size
    }
}