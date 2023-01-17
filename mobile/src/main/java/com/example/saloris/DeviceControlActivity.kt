package com.example.saloris

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat


private val TAG = "gattClienCallback"

class DeviceControlActivity(private val context: Context?, private var bluetoothGatt: BluetoothGatt?) {
    private var device : BluetoothDevice? = null
    private val gattCallback : BluetoothGattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server.")
                    if (context?.let {
                            ActivityCompat.checkSelfPermission(it,
                                Manifest.permission.BLUETOOTH_CONNECT)
                        } != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    Log.i(TAG, "Attempting to start service discovery: " +
                            bluetoothGatt?.discoverServices())
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server.")
                    disconnectGattServer()
                }
            }

        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i(TAG, "Connected to GATT_SUCCESS.")
                    if (context?.let {
                            ActivityCompat.checkSelfPermission(it,
                                Manifest.permission.BLUETOOTH_CONNECT)
                        } != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    broadcastUpdate("Connected "+ device?.name)
                }
                else -> {
                    Log.w(TAG, "Device service discovery failed, status: $status")
                    broadcastUpdate("Fail Connect "+device?.name)
                }
            }
        }
        private fun broadcastUpdate(str: String) {
            val mHandler : Handler = object : Handler(Looper.getMainLooper()){
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    Toast.makeText(context,str,Toast.LENGTH_SHORT).show()
                }
            }
            mHandler.obtainMessage().sendToTarget()
        }
        private fun disconnectGattServer() {
            Log.d(TAG, "Closing Gatt connection")
            // disconnect and close the gatt
            if (bluetoothGatt != null) {
                if (context?.let {
                        ActivityCompat.checkSelfPermission(it,
                            Manifest.permission.BLUETOOTH_CONNECT)
                    } != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }
    }

    fun connectGatt(device:BluetoothDevice):BluetoothGatt?{
        this.device = device

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context?.let {
                    ActivityCompat.checkSelfPermission(it,
                        Manifest.permission.BLUETOOTH_CONNECT)
                } != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            bluetoothGatt = device.connectGatt(context, false, gattCallback,
                BluetoothDevice.TRANSPORT_LE)
        }
        else {
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        }
        return bluetoothGatt
    }
}


//class DeviceControlActivity: AppCompatActivity() {
//    private var deviceAddress: String = ""
//    private var bluetoothService: BluetoothLeService? = null
//
//    private val serviceConnection = object: ServiceConnection {
//        override fun onServiceDisconnected(name: ComponentName?) {
//            bluetoothService = null
//        }
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            bluetoothService = (service as BluetoothLeService.LocalBinder).service
//            bluetoothService?.connect(deviceAddress)
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        deviceAddress = intent.getStringExtra("address").toString()
//
//        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
//        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE
//        )
//    }
//    var connected: Boolean = false
//    val gattUpdateReceiver = object: BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            val action = intent?.action
//            when (action) {
//                BluetoothLeService.ACTION_GATT_CONNECTED -> connected = true
//                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
//                    connected = false
//                    Toast.makeText(this@DeviceControlActivity, "dis", Toast.LENGTH_SHORT).show()
//                }
//                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
//                    bluetoothService?.let {
//                        SelectChacrateristicData(it.getSupportedGattServices())
//                    }
//                }
//                BluetoothLeService.ACTION_DATA_AVAILABLE -> {
//                    val resp: String? = intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
//                    // resp 처리 구현
//            }
//        }
//    }
//    private var writeCharacteristic: BluetoothGattCharacteristic? = null
//    private var notifyCharacteristic: BluetoothGattCharacteristic? = null
//
//    private fun SelectCharacteristicData(gattServices: List<BluetoothGattService>) {
//        for (gattService in gattServices) {
//            var gattCharacteristics: List<BluetoothGattCharacteristic> = gattService.characteristics
//
//            for (gattCharacteristic in gattCharacteristics) {
//                when (gattCharacteristic.uuid) {
//                    BluetoothLeService.UUID_DATA_WRITE -> writeCharacteristic = gattCharacteristic
//                    BluetoothLeService.UUID_DATA_NOTIFY -> notifyCharacteristic = gattCharacteristic
//                }
//            }
//        }
//    }
//
//    private fun SendData(data: String) {
//        writeCharacteristic?.let {
//            if (it.properties or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
//                bluetoothService?.writeCharacteristic(it, data)
//            }
//        }
//
//        notifyCharacteristic?.let {
//            if (it.properties or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
//                bluetoothService?.setCharacteristicNotification(it, true)
//            }
//        }
//    }
//    override fun onPause() {
//        super.onPause()
//        unregisterReceiver(gattUpdateReceiver)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        unbindService(serviceConnection)
//        bluetoothService = null
//    }
//}