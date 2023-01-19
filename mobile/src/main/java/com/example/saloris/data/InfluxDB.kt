package com.example.saloris.data

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.influxdb.client.write.Point
import com.influxdb.exceptions.InfluxException
import java.time.Instant

class InfluxDB {
    @RequiresApi(Build.VERSION_CODES.O)
    open suspend fun insertDB(heartRate:String): Boolean {
        //val token = System.getenv()["INFLUX_TOKEN"]
        val user="user"
        val Uid="namin"
        val org = "intern"
        val bucket = "HeartRate"
        val token = "kCQhpgwgcQB4H_5Nr4Uf_lFIZjFQkaDA4IeWm5nBmt9WjfpeyIEsdZM95iGaQcx0BMKT0x-sUjHTOKGSApwjrw=="
        print(System.getenv())
        val client = InfluxDBClientKotlinFactory.create("https://europe-west1-1.gcp.cloud2.influxdata.com", token!!.toCharArray(), org, bucket)
        client.use {
            val writeApi = client.getWriteKotlinApi()
            try {
                val point = Point
                    .measurement(user)
                    .addTag("Uid", Uid)
                    .addField("HeartRate",heartRate)
                    .time(Instant.now(), WritePrecision.NS);

                writeApi.writePoint(point)
            } catch (ie: InfluxException) {
                Log.e("InfluxException", "Insert: ${ie.cause}")
                return false
            }
        }
        client.close()
        return true
    }
}