package com.example.saloris.LocalDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HeartRateDao {
    @Query("SELECT * FROM heartrate")
    fun getAll():List<HeartRate>

    @Insert
    fun insertHeartRate(heartRate: HeartRate)

    @Query("DELETE FROM heartrate")
    fun deleteAll()

    @Query("SELECT * FROM heartrate WHERE InsertTime LIKE :Date")
    fun getHeartRate(Date : String):List<HeartRate>
}