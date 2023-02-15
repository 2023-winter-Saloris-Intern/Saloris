package com.example.saloris.LocalDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(primaryKeys = ["InsertDate", "InsertTime"])
data class HeartRate(
    val InsertTime : String,
    val InsertDate : String,

    @ColumnInfo
    val HeartRate : Int?,
    @ColumnInfo
    val Sleep : Boolean?,
    @ColumnInfo
    val Uid : String?
    )
