package com.example.saloris.data

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.saloris.MyMarkerView
import com.example.saloris.R
import com.example.saloris.TimeAxisValueFormat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter

class showData : AppCompatActivity() {
    /* User Authentication */
    private lateinit var auth: FirebaseAuth

    private var chart: LineChart? = null
    private var thread: Thread? = null

    private var last_time = ""
    val colors = ArrayList<Int>()
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContentView(R.layout.activity_show_data)

        //DatePicker - 캘린더
        val dayText : TextView = findViewById(R.id.day_text)
        val datePicker : DatePicker =findViewById(R.id.datePicker)
        val iYear : Int = datePicker.year
        val iMonth : Int = datePicker.month+1
        val iDay : Int = datePicker.dayOfMonth

        dayText.text = "${iYear}년 ${iMonth}월 ${iDay}일"
        var Dyear=iYear
        var Dmonth = iMonth
        var Dday =iDay
        val setDateButton =findViewById<Button>(R.id.setDateButton)
        val button = findViewById<Button>(R.id.button)
        val text = findViewById<TextView>(R.id.textView)

        val TAG = this.javaClass.simpleName
        val heartRateArr = java.util.ArrayList<HeartRate>()
        //chart
        chart = findViewById<View>(R.id.lineChart) as LineChart
        chart!!.xAxis.position = XAxis.XAxisPosition.BOTTOM
        //chart!!.yAxis.axisMinimum=0f
        chart!!.xAxis.valueFormatter = TimeAxisValueFormat()
        chart!!.xAxis.setDrawLabels(true)
        chart!!.xAxis.axisMinimum=0f
        chart!!.xAxis.axisMaximum=1200f
        chart!!.axisRight.isEnabled = false
        chart!!.axisLeft.axisMaximum=100f // y축 min,max
        chart!!.axisLeft.axisMinimum=50f
        chart!!.legend.textColor = Color.BLUE
        chart!!.animateXY(1000, 1000)
        chart!!.invalidate()
        val data = LineData()
        chart!!.data = data

        //feedMultiple()
        val mv = MyMarkerView(this, R.layout.custom_marker_view,"Show")
        // set the marker to the chart
        chart!!.setMarker(mv);

        //캘린더 날짜 선택
        datePicker.setOnDateChangedListener{
                datePicker,year,month,dayOfMonth->
            dayText.text =  "${year}년 ${month+1}월 ${dayOfMonth}일"
            Dyear = year
            Dmonth = month+1
            Dday = dayOfMonth
            Log.d("select day",Dyear.toString()+Dmonth.toString()+Dday.toString())
            datePicker.visibility= View.GONE
            //chart!!.visibility =View.VISIBLE

        }
        //다시 캘린더 날짜 선택을 하기 위해 캘린더를 보이게 하는 버튼
        setDateButton.setOnClickListener{
            datePicker.visibility= View.VISIBLE
            //chart!!.visibility =View.GONE
        }
        //선택한 날짜의 데이터를 가져와서 차트로 출력
        button.setOnClickListener{
            //colors 초기화
            colors.clear()
            Log.d("colors",colors.toString())
            last_time=""
            data.removeDataSet(0)
            Log.d("data",data.toString())
            Log.d("data",chart!!.data.toString())
            Log.d("DB start",LocalDateTime.now().toString())
            lifecycleScope.launch(Dispatchers.IO) {
                val Uid=auth.currentUser?.uid
                val year=Dyear
                val month = Dmonth
                val day =Dday
                val date = LocalDate.of(year, month, day)
                //show ( uid , 선택 시간) : 선택한 날짜의 데이터를 차트로 출력
                val show_Data = async {
                    if (Uid != null) {
                        show(Uid,date)
                    }
                }
                text.text= show_Data.await().toString()
                if (Uid != null) {
                    Log.d("did",Uid)
                }
                Log.d("DB end",LocalDateTime.now().toString())
            }
        }

    }
    //chart
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addEntry(time:String,newRate:String) {
        val data = chart!!.data
        if (data != null) {
            var set = data.getDataSetByIndex(0)
            if (set == null) {
                set = createSet()
                data.addDataSet(set)
            }
            //val dateAndTime : LocalDateTime = LocalDateTime.now()
            Log.d("dateAndTime",time.toString())
            val time_hour = time.toString().substring(11,13)
            val time_min = time.toString().substring(14,16)
            Log.d("time_hour",time_hour)
            Log.d("time_min",time_min)
            //TODO : time check!!
            val time  = (time_hour.toInt()-9)*60+time_min.toInt()
            Log.d("time",time.toString())
            Log.d("last_time",last_time)
            if(last_time!="" && (last_time.toInt()+1)<time){
                //처음 시작하는 점이 아니고, 이전 출력시간+1 보다 크면 측정하지 않은 부분이다
                Log.d("color",last_time + time.toString())
                //값이 없는 부분 색 삭제
                colors.add(Color.TRANSPARENT)
            }else if(last_time!=""){
                //값이 있는 부분은 파란색 선으로
                colors.add(Color.BLUE)
            }
            last_time=time.toString()
            data.addEntry(Entry(time.toFloat(),newRate.toFloat()), 0)
//            data.notifyDataChanged()
//            chart!!.notifyDataSetChanged()
            chart!!.setVisibleXRangeMaximum(10f) // x축을 10까지만 보여주고 그 이후부터는 이동..
            chart!!.moveViewToX(time.toFloat()-10f) // 가장 최근 추가한 데이터로 이동
        }
    }
    //chart 설정
    private fun createSet(): LineDataSet {
        val set = LineDataSet(null, "Heart Rate")
        set.fillAlpha = 110
        set.fillColor = Color.parseColor("#d7e7fa")
        set.color = Color.parseColor("#0B80C9")
        set.setCircleColor(Color.parseColor("#FFA1B4DC"))
        //set.setCircleColorHole(Color.BLUE)
        set.valueTextColor = Color.BLACK
        set.setDrawValues(false)
        set.lineWidth = 2f
        set.circleRadius = 6f
        set.setDrawCircleHole(false)
        set.setDrawCircles(false)
        set.valueTextSize = 9f
        set.setDrawFilled(true)
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.highLightColor = Color.rgb(244, 117, 117)
        set.colors = colors
        return set
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun show(Uid:String,date:LocalDate): String {
        //val token = System.getenv()["INFLUX_TOKEN"]
        var all_string = ""
        Log.d("date",date.javaClass.toString())
        val zoneId = ZoneId.systemDefault()
        //TODO : DB에서 가져오는 시간 범위 확인하기!
//        val start = Instant.from(ZonedDateTime.of(date.minusDays(1), LocalTime.of(15,0,0), zoneId))
//        val stop = Instant.from(ZonedDateTime.of(date, LocalTime.of(14, 59, 59), zoneId))
        val start = Instant.from(ZonedDateTime.of(date, LocalTime.of(0,0,0), zoneId))
        val stop = Instant.from(ZonedDateTime.of(date, LocalTime.of(23, 59, 59), zoneId))
        Log.d("start time",start.toString())
        Log.d("stop time",stop.toString())
        val user="user"
        val org = "intern"
        val bucket = "HeartRate"
        val token = "yZmCmFFTYYoetepTiOpXDRK8oyL1f_orD6oZH8SXsvlf213z-_iRmXtaf-AjyLe2HS-NhfxcNeY-0K6qR0k6Sw=="
        val client = InfluxDBClientKotlinFactory.create("https://europe-west1-1.gcp.cloud2.influxdata.com", token!!.toCharArray(), org, bucket)
        val heartrate = ArrayList<HeartRate>()
        //query
        val fluxQuery = ("from(bucket: \"HeartRate\")\n" +
                "  |> range(start:$start, stop: $stop)\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"HeartRate\")\n" +
                "  |> filter(fn: (r) => r[\"Uid\"] == \"$Uid\")\n")
        client.use {
            //val writeApi = client.getWriteKotlinApi()
            val results = client.getQueryKotlinApi().query(fluxQuery)
            Log.d("show",results.toString())
            results.consumeAsFlow()
                .catch {
                    print("catch")
                }
                .collect {
                    //하나씩 값 가져와서
                    val heartRate =it.value
                    val  time = it.time?.atZone(zoneId)
                    // : LocalDateTime = LocalDateTime.parse (it.time.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'")).plusHours(9)
                    Log.d("type of time", time?.javaClass.toString())
                    Log.d("heartRate",time.toString()+"\t"+heartRate.toString())
                    val h_string = "heartRate : "+time.toString()+"\t"+heartRate.toString()+"\n"
                    //chart 데이터에 저장
                    addEntry(time.toString(),heartRate.toString())
                    //heartrate.add(heartRate)
                    all_string += h_string
                }
        }
        colors.add(Color.BLUE)
        Log.d("colors",colors.toString())
        //print(heartrate)
        return all_string
    }



}