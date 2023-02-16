package com.example.saloris.Record

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.os.Build
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.saloris.LocalDB.AppDatabase
import com.example.saloris.MainActivity
import com.example.saloris.MyMarkerView
import com.example.saloris.R
import com.example.saloris.TimeAxisValueFormat
import com.example.saloris.data.HeartRate
import com.example.saloris.databinding.FragmentRecordBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.*
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class MinMaxDecorator(min: CalendarDay, max:CalendarDay): DayViewDecorator {
    val maxDay = max
    val minDay = min

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return (day?.month == maxDay.month && day.day > maxDay.day)
                || (day?.month == minDay.month && day.day < minDay.day)
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object: ForegroundColorSpan(Color.parseColor("#d2d2d2")){})
        view?.setDaysDisabled(true)
    }
}

class RecordFragment : Fragment() {
    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

//    private var chartData = ArrayList<Entry>() // 데이터배열
//    private var lineDataSet = ArrayList<ILineDataSet>() // 데이터배열 -> 데이터 셋
//    private var lineData: LineData = LineData()

    lateinit var chart: LineChart

    private var last_time = ""
    val colors = ArrayList<Int>()

    var sleepArr=ArrayList<Boolean>()
    var count = 0
    var lastSleep =false
    var lastRate = 0f
    var ismidvalue=false
    var Dyear = 0
    var Dmonth =0
    var Dday = 0
    var DayMean = 0f
    var Dayhighest : Int=0
    var Daylowest : Int = 500
    var Daysum =0f
    var Daycount=0f
    var averageArr = ArrayList<Double>()
    var DayArr = ArrayList<String>()
    var sleepX = ArrayList<Float>()
    private lateinit var auth: FirebaseAuth
    // 1. Context를 할당할 변수를 프로퍼티로 선언(어디서든 사용할 수 있게)
    lateinit var mainActivity: MainActivity
    val data = LineData()
    override fun onAttach(context: Context) {
        super.onAttach(context)

        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
    }
    // Item의 클릭 상태를 저장할 array 객체
    private val selectedItems = SparseBooleanArray()

    // 직전에 클릭됐던 Item의 position
    private val prePosition = -1

    var startTimeCalendar = Calendar.getInstance()
    var endTimeCalendar = Calendar.getInstance()

    val currentYear = startTimeCalendar.get(Calendar.YEAR)
    val currentMonth = startTimeCalendar.get(Calendar.MONTH)
    val currentDate = startTimeCalendar.get(Calendar.DATE)

    val stCalendarDay = CalendarDay.from(currentYear, currentMonth, currentDate)
    val enCalendarDay = CalendarDay.from(endTimeCalendar.get(Calendar.YEAR), endTimeCalendar.get(Calendar.MONTH), endTimeCalendar.get(Calendar.DATE))

    val minMaxDecorator = MinMaxDecorator(stCalendarDay, enCalendarDay)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRecordBinding.inflate(inflater, container, false)

        chart =  binding.dayChart

        binding.calendarView1.setVisibility(View.GONE)
        binding.dayChart.setVisibility(View.GONE)
        binding.heartRateNum.setVisibility(View.GONE)
        auth = Firebase.auth
        binding.dateChoiceBtn.setOnClickListener {
            //var CalendarView = R.id.calendar_view1

            val dateText: TextView = requireView().findViewById(R.id.date_text)
            //val calendarView: CalendarView = requireView().findViewById(R.id.calendar_view1)
            var calendarView: MaterialCalendarView = requireView().findViewById(R.id.calendar_view1)

            val dateFormat: DateFormat = SimpleDateFormat("yyyy년MM월dd일")

            //val date: Date = Date(calendarView.date)

//            val today = CalendarDay.today()
//            calendarView.state().edit().setMaximumDate(today).commit()

            //calendarView.maxDate = System.currentTimeMillis()

            DayArr.clear()
            averageArr.clear()
            lifecycleScope.launch(Dispatchers.IO) {
                val Uid = auth.currentUser?.uid
                val a = async {
                    if (Uid != null) {
                        //datefromDB(Uid)
                        var db =
                            Room.databaseBuilder(
                                requireContext().applicationContext,
                                AppDatabase::class.java,
                                "heartRateDB"
                            ).build()
                        var DateArr =  db.heartRateDao().getInsertDate(Uid)
                        Log.d("date from db",DateArr.toString())
                    }
                }
                if (a.await() != null) {
                    mainActivity.runOnUiThread(Runnable {
                        //todo : 데이터가 있는 날짜를 이용해 ui변경(색 or 선택제한)S
                        //잘 돌아갈지는 모르겠다..

                        //val dayArr = DayArr //arrayListOf("yyyy-MM-dd") // 이 부분에 데이터 포멧이 아니고 실제 데이터가 들어가야함..
                        val format = org.threeten.bp.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")

                        //val dateString = "2023-02-13"

                        val calendarDays = DayArr.map {
                            val date = org.threeten.bp.LocalDate.parse(it,format)
                            CalendarDay.from(date)
                        }
                        val decorator = EventDecorator(calendarDays)
                        calendarView.addDecorator(decorator)
                    });
                }
            }
            //dateText.text = dateFormat.format(date)

            if (selectedItems.get(prePosition)) {
                selectedItems.delete(prePosition)
                binding.dateChoiceBtn.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                binding.calendarView1.setVisibility(View.VISIBLE)
                binding.dateConfirmBtn.setVisibility(View.VISIBLE)
                binding.graphBtn.setVisibility(View.GONE)
                binding.dayChart.setVisibility(View.GONE)
                binding.heartRateNum.setVisibility(View.GONE)

                binding.dateConfirmBtn.setOnClickListener {
                    binding.dateConfirmBtn.setVisibility(View.GONE)
                    binding.calendarView1.setVisibility(View.GONE)
                    binding.dateChoiceBtn.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                    binding.graphBtn.setVisibility(View.VISIBLE)
                    binding.dayChart.setVisibility(View.VISIBLE)
                    binding.heartRateNum.setVisibility(View.VISIBLE)
                    //초기화
                    Dayhighest=0
                    Daylowest=500
                    Daysum=0f
                    Daycount=0f
                    //colors 초기화
                    colors.clear()
                    //markerview null오류 해결 => hilight 초기화
                    chart!!.highlightValue(null)
                    Log.d("colors",colors.toString())
                    last_time=""
                    sleepArr.clear()
                    count=0
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
                                //show(Uid,date)
                                var db =
                                    Room.databaseBuilder(
                                        requireContext().applicationContext,
                                        AppDatabase::class.java,
                                        "heartRateDB"
                                    ).build()
                                Log.d("date",date.toString())
                                Log.d("fromDB!!!!!!!!!!",db!!.heartRateDao().getHeartRate(date.toString(),Uid).toString())
                                var heartRateList = db!!.heartRateDao().getHeartRate(date.toString(),Uid)
                                //todo chart!!!
                                for(dao in heartRateList){
                                    Log.d("time",dao.InsertDate+"T"+dao.InsertTime)
                                    Log.d("heartrate",dao.HeartRate.toString())
                                    Log.d("sleep",dao.Sleep.toString())
                                    sleepArr.add(dao.Sleep!!)
                                    addEntry(dao.InsertDate+"T"+dao.InsertTime,dao.HeartRate.toString())

                                }
                                var nonSleeplineColor = ContextCompat.getColor(requireContext(),R.color.chart_line_nonsleep)
                                colors.add(nonSleeplineColor)
                                Log.d("colors",colors.toString())
                            }
                        }
                        if(show_Data.await()!=null){
                            mainActivity.runOnUiThread(Runnable { // 메시지 큐에 저장될 메시지의 내용
                                if(Dayhighest!=0) {
                                    binding.average.text = (round(((Daysum/Daycount))*100)/100).toString()
                                    binding.highest.text = Dayhighest.toString();
                                    binding.lowest.text = Daylowest.toString();
                                    if(Dayhighest>80f){
                                        chart!!.axisLeft.axisMaximum=150f
                                    }
                                }else{
                                    binding.average.text = "-";
                                    binding.highest.text ="-";
                                    binding.lowest.text = "-";
                                }
                            });
                        }

                        //text.text= show_Data.await().toString()
                        if (Uid != null) {
                            Log.d("did",Uid)
                        }
                        Log.d("DB end",LocalDateTime.now().toString())
                    }

                }

                calendarView.setOnDateChangedListener { widget, date, selected ->
                    var day: String = "${date.year}년 ${date.month}월 ${date.day}일"
                    Dyear = date.year
                    Dmonth = date.month
                    Dday = date.day
                    dateText.text = day
                }

            } else {
                selectedItems.delete(prePosition)
                selectedItems.put(prePosition, true)
                binding.dateChoiceBtn.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                binding.calendarView1.setVisibility(View.GONE);
                binding.dateConfirmBtn.setVisibility(View.GONE);
                //binding.dayChart.setVisibility(View.VISIBLE)
            }

        }
        val TAG = this.javaClass.simpleName
        val heartRateArr = java.util.ArrayList<HeartRate>()
        //chart
        chart = binding.dayChart
        chart!!.xAxis.position = XAxis.XAxisPosition.BOTTOM
        //chart!!.yAxis.axisMinimum=0f
        chart!!.xAxis.valueFormatter = TimeAxisValueFormat()
        chart!!.xAxis.setDrawLabels(true)
        chart!!.xAxis.axisMinimum=0f
        chart!!.xAxis.axisMaximum=1200f
        chart!!.axisRight.isEnabled = false
        chart!!.axisLeft.axisMaximum=150f // y축 min,max
        chart!!.axisLeft.axisMinimum=30f
        chart!!.legend.textColor = Color.BLUE
        chart!!.animateXY(1000, 1000)
        chart!!.invalidate()
        chart!!.data = data

        //feedMultiple()
        val mv = MyMarkerView(mainActivity, R.layout.custom_marker_view,"Show")
        // set the marker to the chart
        chart!!.setMarker(mv);

        chart.setOnChartValueSelectedListener(object: OnChartValueSelectedListener{
            var sleepCardColor = ContextCompat.getDrawable(requireContext(),R.drawable.sleep_state_btn)
            var nonSleepCardColor = ContextCompat.getDrawable(requireContext(),R.drawable.nonsleep_state_btn)
            var sleepTextColor = ContextCompat.getColor(requireContext(),R.color.heart_rate)
            var nonSleepBGColor = ContextCompat.getColor(requireContext(),R.color.purple_500)
            var nonSleepTextColor = ContextCompat.getColor(requireContext(),R.color.black)

            override fun onValueSelected(e: Entry, h: Highlight){
                val xAxisLabel = e.x.toString()
                Log.d("x in record",xAxisLabel)
                Log.d("sleepXArr",sleepX.toString())
                if(xAxisLabel.toFloat() in sleepX) {
                    Log.d("Sleep!!!!!", "the value is sleep!!")
                    binding.sleepState.text = "혹시 졸았나요?\uD83D\uDE34"
                    binding.sleepState.setBackgroundDrawable(sleepCardColor)
                    binding.sleepState.setTextColor(sleepTextColor)
                    val mv = MyMarkerView(mainActivity, R.layout.custom_marker_view_red,"Show")
                    // set the marker to the chart
                    chart!!.setMarker(mv);
                }else{
                    binding.sleepState.text = " - "
                    binding.sleepState.setBackgroundDrawable(nonSleepCardColor)
                    binding.sleepState.setTextColor(nonSleepTextColor)
                    //mv.setBackgroundColor(nonSleepBGColor)
                    val mv = MyMarkerView(mainActivity, R.layout.custom_marker_view,"Show")
                    // set the marker to the chart
                    chart!!.setMarker(mv);
                }
            }
            override fun onNothingSelected() {
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* Bottom Menu */
        val bottomMenu = (requireActivity() as MainActivity).binding.bottomNav
        bottomMenu.visibility = View.VISIBLE
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
            Log.d("count", count.toString())
            var SleeplineColor = ContextCompat.getColor(requireContext(),R.color.chart_line_sleep)
            var nonSleeplineColor = ContextCompat.getColor(requireContext(),R.color.chart_line_nonsleep)
            if(last_time!=""){
                if((last_time.toInt()+1)<time) {//거리가 멀어서 색 무색으로
                    Log.d("notin", last_time + time.toString())
                    //값이 없는 부분 색 삭제
                    colors.add(Color.TRANSPARENT)
                }else{
                    Log.d("lastSleep",lastSleep.toString())
                    Log.d("Sleep",sleepArr[count].toString())
                    Log.d("count",count.toString())
                    if(lastSleep==sleepArr[count]){
                        //두개 같은 값 => 변화 없음
                        Log.d("sleepis","same")
                        colors.add(if(sleepArr[count]) SleeplineColor else nonSleeplineColor)
                    }else{
                        Log.d("sleepis","not same")
                        data.addEntry(Entry(((time.toFloat()-0.5)).toFloat(),((newRate.toFloat()+lastRate)/2.0).toFloat()), 0)
                        colors.add(if(lastSleep) SleeplineColor else nonSleeplineColor)
                        colors.add(if(sleepArr[count]) SleeplineColor else nonSleeplineColor)
                        Log.d("time",((time.toFloat()-0.5)).toString())
                        Log.d("midrate",((newRate.toFloat()+lastRate)/2.0).toString())
                    }
                }
            }
            if(sleepArr[count]==true){
                sleepX.add(time.toFloat())
                Log.d("sleepX ",time.toString())
            }
            if(newRate.toInt()!=0) // 0이 있으면 0이 최소로 나와서
            {
                Daylowest= min(newRate.toInt(),Daylowest)
                Daysum+=newRate.toInt()
                Dayhighest=max(newRate.toInt(),Dayhighest)
                Daycount++
                DayMean=(round(((Daysum/Daycount))*100)/100)
            }
            Log.d("min max",Daylowest.toString()+Dayhighest.toString())
            lastSleep = sleepArr[count]
            count++
            last_time=time.toString()
            lastRate=newRate.toFloat()
            data.addEntry(Entry(time.toFloat(),newRate.toFloat()), 0)
//            data.notifyDataChanged()
//            chart!!.notifyDataSetChanged()
            chart!!.setVisibleXRangeMaximum(10f) // x축을 10까지만 보여주고 그 이후부터는 이동..
            chart!!.moveViewToX(time.toFloat()-10f) // 가장 최근 추가한 데이터로 이동
            chart!!.setVisibleYRangeMaximum(50f,YAxis.AxisDependency.LEFT) // x축을 10까지만 보여주고 그 이후부터는 이동..
            chart!!.moveViewTo(time.toFloat()-10f,DayMean,YAxis.AxisDependency.LEFT)
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
    suspend fun datefromDB(Uid:String): String{
        var month_offset = "0"
        val org = "intern"
        val userTime = auth.currentUser?.metadata?.creationTimestamp
        val currentDateTime =
            userTime?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() }.toString() + "Z"
        //Log.d("Current!!!!!!!!!!!!!!!",currentDateTime.toString())
        //Log.d("!!!!!!!!!!metadata",userTime.toString())
        val bucket = "HeartRate"
        val token = "yZmCmFFTYYoetepTiOpXDRK8oyL1f_orD6oZH8SXsvlf213z-_iRmXtaf-AjyLe2HS-NhfxcNeY-0K6qR0k6Sw=="
        val client3 = InfluxDBClientKotlinFactory.create("https://europe-west1-1.gcp.cloud2.influxdata.com", token!!.toCharArray(), org, bucket)
        val fluxQueryMean = ("import \"experimental/date/boundaries\"\n" +
                "thisMonth = boundaries.month(month_offset: $month_offset)\n" +
                "from(bucket: \"HeartRate\")\n" +
                "  |> range(start: $currentDateTime, stop: thisMonth.stop)\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"user\")\n" +
                "  |> filter(fn: (r) => r[\"Uid\"] ==\"$Uid\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"heart\")\n" +
                "  |> aggregateWindow(every: 1d, fn: mean, createEmpty: false, timeSrc: \"_start\")")
        //Log.d("Query!!!!!!!!!!!!!!!!!!",fluxQueryMean)
        client3.use {
            //val writeApi = client.getW배터리 정보 가져오기riteKotlinApi()
            val results = client3.getQueryKotlinApi().query(fluxQueryMean)
            Log.d("show",results.toString())
            results.consumeAsFlow()
                .catch {
                    print("catch")
                }
                .collect {
                    val time = it.time.toString().substring(0,10)
                    //Log.d("time!!!!!!!!!!!!!!!!!",it.time.toString())
                    val average = it.value
                    averageArr.add(average as Double)
                    DayArr.add(time)
//                    Log.d("is same?",time.toString().substring(8,10)+" == "+ Dday.toString())
//                    if(time.toString().substring(8,10) == Dday.toString()){
//                        val average = it.value
//                        Log.d("average",average.toString())
//                        DayMean=(round((average as Double)*100)/100).toString()
//                    }
                    Log.d("average and time ",time+average.toString())
                }
        }
        client3.close()
        return ""
    }
    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun show(Uid:String,date: LocalDate): String {
        //val token = System.getenv()["INFLUX_TOKEN"]
        var all_string = ""
        Log.d("date",date.javaClass.toString())
        val zoneId = ZoneId.systemDefault()
//        TODO : DB에서 가져오는 시간 범위 확인하기!
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
        val client2 = InfluxDBClientKotlinFactory.create("https://europe-west1-1.gcp.cloud2.influxdata.com", token!!.toCharArray(), org, bucket)
        val client3 = InfluxDBClientKotlinFactory.create("https://europe-west1-1.gcp.cloud2.influxdata.com", token!!.toCharArray(), org, bucket)
        val heartrate = ArrayList<HeartRate>()
//        //query 사용자의 평균을 출력
//        val fluxQueryMean = ("from(bucket: \"HeartRate\")\n" +
//                "  |> range(start:$start, stop: $stop)\n" +
//                "  |> filter(fn: (r) => r[\"_measurement\"] == \"user\")\n" +
//                "  |> filter(fn: (r) => r[\"Uid\"] ==\"$Uid\")\n" +
//                "  |> filter(fn: (r) => r[\"_field\"] == \"heart\")\n" +
//                "  |> mean(column: \"_value\")")
        //query issleep 출력
        val fluxQueryright = ("from(bucket: \"HeartRate\")\n" +
                "  |> range(start:$start, stop: $stop)\n" +
                "  |> filter(fn: (r) =>r[\"_field\"] == \"isntsleep\")\n" +
                "  |> filter(fn: (r) => r[\"Uid\"] == \"$Uid\")\n")
        Log.d("qureyissleep",fluxQueryright)
        client2.use {
            //val writeApi = client.getWriteKotlinApi()
            val results = client.getQueryKotlinApi().query(fluxQueryright)
            Log.d("show",results.toString())
            results.consumeAsFlow()
                .catch {
                    print("catch")
                }
                .collect {
                    Log.d("result",it.toString())
                    //하나씩 값 가져와서
                    val issleep =it.value
                    val  time = it.time?.atZone(zoneId)
                    Log.d("issleep",time.toString()+"\t"+issleep.toString())
                    sleepArr.add(issleep as Boolean)
                }
        }
        sleepArr.add(false)
        sleepArr.add(false)
        sleepArr.add(false)
        sleepArr.add(false)
        sleepArr.add(false)
        Log.d("sleepArr",sleepArr.toString())
        Log.d("sleepArrLength",sleepArr.size.toString())
        client2.close()
        //query heart rate
        val fluxQueryleft = ("from(bucket: \"HeartRate\")\n" +
                "  |> range(start:$start, stop: $stop)\n" +
                "  |> filter(fn: (r) =>r[\"_field\"] == \"heart\")\n" +
                "  |> filter(fn: (r) => r[\"Uid\"] == \"$Uid\")\n")
        Log.d("Qurey",fluxQueryleft)
        client.use {
            //val writeApi = client.getWriteKotlinApi()
            val results = client.getQueryKotlinApi().query(fluxQueryleft)
            Log.d("show",results.toString())
            results.consumeAsFlow()
                .catch {
                    print("catch")
                }
                .collect {
                    Log.d("result",it.toString())
                    //하나씩 값 가져와서
                    val heartRate =it.value
                    //val issleep = it.target
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
        client.close()
        var nonSleeplineColor = ContextCompat.getColor(requireContext(),R.color.chart_line_nonsleep)
        colors.add(nonSleeplineColor)
        Log.d("colors",colors.toString())
        //print(heartrate)
        return all_string
    }
}

class EventDecorator() : DayViewDecorator {

    private lateinit var dates : HashSet<CalendarDay>

    constructor(dates: Collection<CalendarDay>) : this() {
        this.dates=HashSet(dates)
    }

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(DotSpan(10F, Color.BLUE))
    }
}