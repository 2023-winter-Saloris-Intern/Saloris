package com.example.saloris.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.influxdb.annotations.Column
import java.time.Instant

data class  HeartRate(
    @Column(tag = true) val Uid: String,
    @Column val HeartRate: String,
    @Column(timestamp = true) val time: Instant
) {
    @RequiresApi(Build.VERSION_CODES.O)
    constructor(_user: String, HeartRate: String) : this(_user, HeartRate, Instant.now())
}