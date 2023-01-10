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
//@RequiresApi(Build.VERSION_CODES.O)
//suspend fun show(host:String, used_percent : Double): Boolean {
//    //val token = System.getenv()["INFLUX_TOKEN"]
//    val date = LocalDate.of(2022, 12, 27)
//    val zoneId = ZoneId.systemDefault()
//    val start = Instant.from(ZonedDateTime.of(date, LocalTime.MIDNIGHT, zoneId))
//    val stop = Instant.from(ZonedDateTime.of(date, LocalTime.of(23, 59, 59), zoneId))
//    val org = "intern"
//    val bucket = "test"
//    val token = "DNffqyknibW2Op4Xy3zCF9A4Lvun_gOF8dAWHWBR6BRBbXoub0EpOB_sKZLgC1sKayUFefA4uWMgAuqBZTf1lQ=="
//    val client = InfluxDBClientKotlinFactory.create("https://europe-west1-1.gcp.cloud2.influxdata.com", token!!.toCharArray(), org, bucket)
//    val mem = ArrayList<Mem>()
//    val fluxQuery = ("from(bucket: \"test\")\n" +
//            "  |> range(start:$start, stop: $stop)\n" +
//            "  |> filter(fn: (r) => r[\"_measurement\"] == \"mem\")\n" +
//            "  |> filter(fn: (r) => r[\"host\"] == \"host1\")\n" +
//            "  |> filter(fn: (r) => r[\"_field\"] == \"used_percent\")\n")
//    client.use {
//        //val writeApi = client.getWriteKotlinApi()
//        val results = client.getQueryKotlinApi().query(fluxQuery)
//            results.consumeAsFlow()
//            .catch {
//                print("catch")
//            }
//            .collect {
//                val heartRate = Mem(host, used_percent)
//                print("heartrate")
//                print(heartRate)
//                mem.add(heartRate)
//            }
//    }
//    print(mem)
//    return true
//}


}