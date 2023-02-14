package com.example.saloris.LocalDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class HeartRate(
    @PrimaryKey val InsertTime : String,
    @ColumnInfo
    val HeartRate : Int?,
    @ColumnInfo
    val Sleep : Boolean?
    )
