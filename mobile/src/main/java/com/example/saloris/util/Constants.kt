package com.example.saloris.util

import com.example.saloris.R

val FRAGMENT_INFO = mapOf(
    R.id.registerFragment to Pair(R.string.register, true),
    R.id.homeFragment to Pair(R.string.app_name, false),
    R.id.scanFragment to Pair(R.string.monitor, true),
    R.id.graphFragment to Pair(R.string.graph, true),
    //R.id.graphHrFragment to Pair(R.string.graph_hr, true),
    R.id.settingsFragment to Pair(R.string.setting, true),
    R.id.accountFragment to Pair(R.string.account, true)
)

/* BLE */
const val HEART_RATE_SERVICE_STRING = "0000180d-0000-1000-8000-00805f9b34fb"
const val HEART_RATE_MEASUREMENT_CHARACTERISTIC_STRING = "00002a37-0000-1000-8000-00805f9b34fb"
const val CLIENT_CHARACTERISTIC_CONFIGURATION_DESCRIPTOR_STRING =
    "00002902-0000-1000-8000-00805f9b34fb"
const val SCAN_TIME = 10000L

const val BATTERY_SERVICE_STRING = "0000180f-0000-1000-8000-00805f9b34fb"
const val BATTERY_LEVEL_CHARACTERISTIC_STRING = "00002a19-0000-1000-8000-00805f9b34fb"

const val HEART_RATE_THRESHOLD = 67
const val MAX_WARNING_LEVEL = 10
const val MAX_FITTING_LEVEL = 10

/* FaceMesh */
// Color
val RED_COLOR = floatArrayOf(1f, 0.2f, 0.2f, 1f)
val BLACK_COLOR = floatArrayOf(0.9f, 0.9f, 0.9f, 1f)
val BLUE_COLOR = floatArrayOf(0.0f, 0.5f, 0.9f, 0.5f)
val ORANGE_COLOR = floatArrayOf(1f, 0.9f, 0.5f, 0.2f)
val WHITE_COLOR = floatArrayOf(0.75f, 0.75f, 0.75f, 0.5f)
val GREEN_COLOR = floatArrayOf(0.2f, 1f, 0.2f, 1f)
