package com.example.salorisv

import com.google.android.gms.wearable.MessageEvent

class DevicePairing {
    companion object {

        private var messageEvent: MessageEvent? = null
        private var wearableNodeUri: String? = null
        var wearableDeviceConnected: Boolean = false
        internal var nodeId : String? = null

        fun getWearableNodeUri() : String? {
            return wearableNodeUri
        }
        fun getMessageEvent():MessageEvent?{
            return messageEvent
        }
        fun getwearableDeviceConnected() : Boolean?{
           return wearableDeviceConnected
        }
        fun getNodeId(): String?{
            return nodeId
        }


    }
}