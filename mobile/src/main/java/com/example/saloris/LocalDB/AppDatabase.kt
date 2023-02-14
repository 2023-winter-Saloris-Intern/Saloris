package com.example.saloris.LocalDB

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HeartRate::class], version = 1)
abstract class AppDatabase: RoomDatabase(){
    abstract fun heartRateDao() : HeartRateDao
}