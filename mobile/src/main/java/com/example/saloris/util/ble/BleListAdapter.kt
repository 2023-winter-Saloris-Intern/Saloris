package com.example.saloris.util.ble

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.saloris.MainActivity
import com.example.saloris.R
import com.example.saloris.databinding.FragmentTempBinding
import com.example.saloris.databinding.ItemListBinding
import com.example.saloris.util.CustomDialog
import com.example.saloris.util.OpenDialog


class BleListAdapter : RecyclerView.Adapter<BleListAdapter.RecyclerViewHolder>() {
    var bluetoothDevices: ArrayList<BluetoothDevice> = ArrayList()

    /* Dialog */
    private val dialog = OpenDialog()

    inner class RecyclerViewHolder(private val binding: ItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("MissingPermission")
        fun bind(bluetoothDevice: BluetoothDevice) {
            val bleName = bluetoothDevice.name ?: "noname"
            val bleAddress = bluetoothDevice.address
            val bleClass = bluetoothDevice.bluetoothClass
            val bleType = bluetoothDevice.type
            val bleUUID = bluetoothDevice.uuids ?: "nouuid"
            val ble = bluetoothDevice.uuids
            //println("$bleName: $bleClass: $bleUUID")
            if (bleName != "noname") {
                binding.bleName.text = bleName
                //binding.bleAddress.text = bleAddress
                binding.bleAddress.text = bleUUID.toString()
            }

            val bundle = bundleOf("name" to bleName, "address" to bleAddress)

            binding.root.setOnClickListener {
                it.findNavController().navigate(R.id.action_scanFragment_to_startDriveFragment, bundle)
//                val builder = AlertDialog.Builder(requireContext())
//                builder.setTitle("기기")
//                builder.setMessage("연결")
//                builder.setIcon(R.drawable.watch)
//                builder.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.bind(bluetoothDevices[position])
    }

    override fun getItemCount(): Int {
        return bluetoothDevices.size
    }
}