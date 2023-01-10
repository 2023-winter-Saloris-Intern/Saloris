package com.example.saloris.data

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.saloris.R
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter

class showData : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_data)
        val button = findViewById<Button>(R.id.button)
        val text = findViewById<TextView>(R.id.textView)
        button.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val year = 2023
                val month = 1
                val day = 9
                val date = LocalDate.of(year, month, day)
                //show ( uid , 선택 시간)
                val show_Data = async { show("C", date) }
                text.text = show_Data.await().toString()
                Log.d("did", "C")
            }
        }

    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun show(Uid: String, date: LocalDate): String {
        //val token = System.getenv()["INFLUX_TOKEN"]
        var all_string = ""
        Log.d("date", date.javaClass.toString())
        val zoneId = ZoneId.systemDefault()
//        val start = Instant.from(ZonedDateTime.of(date.minusDays(1), LocalTime.of(15,0,0), zoneId))
//        val stop = Instant.from(ZonedDateTime.of(date, LocalTime.of(14, 59, 59), zoneId))
        val start = Instant.from(ZonedDateTime.of(date, LocalTime.of(0, 0, 0), zoneId))
        val stop = Instant.from(ZonedDateTime.of(date, LocalTime.of(23, 59, 59), zoneId))
        Log.d("start time", start.toString())
        Log.d("stop time", stop.toString())
        val user = "user"
        val org = "intern"
        val bucket = "HeartRate"
        val token =
            "kCQhpgwgcQB4H_5Nr4Uf_lFIZjFQkaDA4IeWm5nBmt9WjfpeyIEsdZM95iGaQcx0BMKT0x-sUjHTOKGSApwjrw=="
        val client = InfluxDBClientKotlinFactory.create(
            "https://europe-west1-1.gcp.cloud2.influxdata.com",
            token!!.toCharArray(),
            org,
            bucket
        )
        val heartrate = ArrayList<HeartRate>()
        val fluxQuery = ("from(bucket: \"HeartRate\")\n" +
                "  |> range(start:$start, stop: $stop)\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"HeartRate\")\n" +
                "  |> filter(fn: (r) => r[\"Uid\"] == \"$Uid\")\n")
        client.use {
            //val writeApi = client.getWriteKotlinApi()
            val results = client.getQueryKotlinApi().query(fluxQuery)
            Log.d("show", results.toString())
            results.consumeAsFlow()
                .catch {
                    print("catch")
                }
                .collect {
                    val heartRate = it.value
                    val time = it.time?.atZone(zoneId)
                    // : LocalDateTime = LocalDateTime.parse (it.time.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'")).plusHours(9)
                    Log.d("type of time", time?.javaClass.toString())
                    Log.d("heartRate", time.toString() + "\t" + heartRate.toString())
                    val h_string =
                        "heartRate : " + time.toString() + "\t" + heartRate.toString() + "\n"
                    //heartrate.add(heartRate)
                    all_string += h_string
                }
        }
        //print(heartrate)
        return all_string
    }


}