package com.example.saloris.util.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.saloris.R
import com.example.saloris.databinding.DialogWatchConnectBinding
import com.example.saloris.databinding.ItemListBinding
import com.example.saloris.util.MakeToast


class BleListAdapter : RecyclerView.Adapter<BleListAdapter.RecyclerViewHolder>() {
    var bluetoothDevices: ArrayList<BluetoothDevice> = ArrayList()
    private lateinit var binding: DialogWatchConnectBinding
    /* Toast */
    private val toast = MakeToast()
    /* Dialog */
    private var name: String = ""
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

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.bind(bluetoothDevices[position])
        holder.itemView.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(it.context,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            ) {
                return@setOnClickListener
            }
            name = bluetoothDevices[position].name
            showDialog(it.context)
        }
    }
    private fun showDialog(context: Context) {

        val builder = AlertDialog.Builder(context)
        builder
            .setTitle(name)
            .setMessage("이 기기와 연결하시겠습니까?")
            .setIcon(R.drawable.watch)
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, which ->
                    toast.makeToast(context, "다이얼로그 확인")
                    binding.yesBtn.text = "확인 클릭"
                })
            .setNegativeButton("취소",
                DialogInterface.OnClickListener { dialog, which ->
                    toast.makeToast(context, "다이얼로그 취소")
                    binding.noBtn.text = "취소 클릭"
                })
        builder.show()
    }

    override fun getItemCount(): Int {
        return bluetoothDevices.size
    }
}