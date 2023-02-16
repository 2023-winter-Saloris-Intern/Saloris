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

    @Query("DELETE FROM heartrate WHERE Uid = :Uid")
    fun deleteAll(Uid:String)

    @Query("SELECT * FROM heartrate WHERE InsertDate = :Date AND Uid = :Uid")
    fun getHeartRate(Date : String,Uid:String):List<HeartRate>

    @Query("SELECT DISTINCT InsertDate FROM heartrate WHERE Uid = :Uid")
    fun getInsertDate(Uid:String):List<String>
}