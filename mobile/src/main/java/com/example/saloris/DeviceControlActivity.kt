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
        //원격 장치에 대한 원격 서비스, 특성 및 설명자 목록이 업데이트 되었을 때 호출되는 콜백
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
        // 토스트로 보여줄 메세지를 파라미터로 받아, Handler의 handMessage로 나타낸다.
        // 토스트도 하나의 UI작업이기 때문에 Thread 안에서 호출하면 에러가 발생한다.
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
                }
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }
    }
    //기기연결
    fun connectGatt(device:BluetoothDevice):BluetoothGatt?{
        this.device = device

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context?.let {
                    ActivityCompat.checkSelfPermission(it,
                        Manifest.permission.BLUETOOTH_CONNECT)
                } != PackageManager.PERMISSION_GRANTED
            ) {
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