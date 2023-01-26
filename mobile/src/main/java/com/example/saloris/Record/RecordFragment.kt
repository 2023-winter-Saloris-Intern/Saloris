package com.example.saloris.Record

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.saloris.R
import com.example.saloris.databinding.FragmentRecordBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toTimeUnit


//line chart 시도
class TimeAxisValueFormat : IndexAxisValueFormatter() {

    @OptIn(ExperimentalTime::class)
    override fun getFormattedValue(value: Float): String {
        //Float(min) -> date
        var valueToMinutes = DurationUnit.MINUTES.toTimeUnit().toMillis(value.toLong())
        var timeMinutes = Date(valueToMinutes)
        var formatMinutes = SimpleDateFormat("HH:mm")

        return formatMinutes.format(timeMinutes)
    }
}

//data class ChartData(
//    var lableData: String = "",
//    var valData: Double = 0.0
//)

class RecordFragment : Fragment() {
    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private var chartData = ArrayList<Entry>() // 데이터배열
    private var lineDataSet = ArrayList<ILineDataSet>() // 데이터배열 -> 데이터 셋
    private var lineData: LineData = LineData()
    lateinit var chart: LineChart

    //private var chart: LineChart? = null

    //private var lineChart: LineChart? = null

    // Item의 클릭 상태를 저장할 array 객체
    private val selectedItems = SparseBooleanArray()

    // 직전에 클릭됐던 Item의 position
    private val prePosition = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRecordBinding.inflate(inflater, container, false)

        //val dateChoiceBtn: ImageButton = requireView().findViewById(com.example.saloris.R.id.date_choice_btn)

//        binding.deviceScanBtn.setOnClickListener {
//            loadFragment(ScanFragment())
//        }

//        val view = inflater.inflate(R.layout.fragment_record, container, false)
//        val button: Button = view.findViewById(R.id.date_confirm_btn)
//        val dateTextView: TextView = view.findViewById(R.id.date_text)
//        val datePicker: DatePicker = view.findViewById(R.id.date_picker)
//
//        val iYear: Int = datePicker.year
//        val iMonth: Int = datePicker.month+1
//        val iDay: Int = datePicker.dayOfMonth

//        dateTextView.text = "${iYear}년 ${iMonth}월 ${iDay}일"

//        datePicker.setOnDateChangedListener { datePicker, year, month, dayOfMonth ->
//            dateTextView.text = "${year}년 ${month+1}월 ${dayOfMonth}일"
//        }
//        button.setOnClickListener { // 버튼 click 시 선택 된 날짜 정보 불러옴
//            dateTextView.setText("Selected Date: "+ datePicker.getDayOfMonth()+"/"+ (datePicker.getMonth() + 1)+"/"+datePicker.getYear());
//        }

        // clickevent로 수정 필요!
//        binding.dateChoiceBtn.setOnClickListener {
//
////          val datepickerHeaderid = binding.datePicker.getChildAt(0)
////              .resources.getIdentifier("date_picker_header", "id", "android")
////          binding.datePicker.findViewById<View>(datepickerHeaderid).visibility = View.GONE
//
//            val cal = java.util.Calendar.getInstance()
//            // 사용자가 OK버튼을 누르면 바인딩된 text가 바뀜
//            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, day ->
//                binding.dateText.text = "${year}년 ${month + 1}월 ${day}일"
//            }
//            // 다이얼로그 생성, 다이얼로그를 띄울때 오늘 날짜를 기본 데이터로 잡아준다.
//            //DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
//            //스피너 모드로 바꿔주기 deprecated 됐기 때문에 themes 에서 추가해줘야함
//            val dateDialog = DatePickerDialog(
//                requireActivity(), dateSetListener, cal.get(java.util.Calendar.YEAR), cal.get(
//                    java.util.Calendar.MONTH
//                ), cal.get(java.util.Calendar.DAY_OF_MONTH)
//            )
//                .apply {
//                    datePicker.maxDate = System.currentTimeMillis()
//                }
//
//            dateDialog.show()
//            //dateDialog.datePicker.spinnersShown = true
//        }

        binding.calendarView1.setVisibility(View.GONE)
        binding.dayChart.setVisibility(View.GONE)

        binding.dateChoiceBtn.setOnClickListener{
            var CalendarView = R.id.calendar_view1

            val dateText: TextView = requireView().findViewById(R.id.date_text)
            val calendarView: CalendarView = requireView().findViewById(R.id.calendar_view1)

            calendarView.maxDate = System.currentTimeMillis()

            //val dateFormat: DateFormat = SimpleDateFormat("yyyy년MM월dd일")

            val date: Date = Date(calendarView.date)

            //dateText.text = dateFormat.format(date)

            if (selectedItems.get(prePosition))
            {
                selectedItems.delete(prePosition)
                binding.dateChoiceBtn.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                binding.calendarView1.setVisibility(View.VISIBLE)
                binding.dateConfirmBtn.setVisibility(View.VISIBLE)
                binding.dayChart.setVisibility(View.GONE)

                binding.dateConfirmBtn.setOnClickListener {
                    binding.dateConfirmBtn.setVisibility(View.GONE)
                    binding.calendarView1.setVisibility(View.GONE)
                    binding.dateChoiceBtn.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                    binding.dayChart.setVisibility(View.VISIBLE)
                }

                calendarView.setOnDateChangeListener{ calendarView, year, month, dayOfMonth ->
                    var day: String = "${year}년 ${month+1}월 ${dayOfMonth}일"

                    dateText.text = day
                }

            } else {
                selectedItems.delete(prePosition)
                selectedItems.put(prePosition,true)
                binding.dateChoiceBtn.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                binding.calendarView1.setVisibility(View.GONE);
                binding.dateConfirmBtn.setVisibility(View.GONE);
                //binding.dayChart.setVisibility(View.VISIBLE)
            }

        }

//        chart = findFragmentById(com.example.saloris.R.id.day_chart)
//
//        val values: ArrayList<Map.Entry<*, *>> = ArrayList()
//
//        for (i in 0..9) {
//            val `val` = (Math.random() * 10).toFloat()
//            values.add(MutableMap.MutableEntry<Any?, Any?>(i, `val`))
//        }
//
//        val set1: LineDataSet
//        set1 = LineDataSet(values, "DataSet 1")
//
//        val dataSets: ArrayList<ILineDataSet> = ArrayList()
//        dataSets.add(set1) // add the data sets
//
//
//        // create a data object with the data sets
//
//        // create a data object with the data sets
//        val data = LineData(dataSets)
//
//        // black lines and points
//
//        // black lines and points
//        set1.color = Color.BLACK
//        set1.setCircleColor(Color.BLACK)
//
//        // set data
//
//        // set data
//        chart.setData(data)

//        그래프 출력
//        chart = view?.findViewById(R.id.day_chart) as LineChart
//
//        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
//
//        chart.axisRight.isEnabled = false
//        chart.legend.textColor = Color.WHITE
//        chart.animateXY(2000, 2000)
//        chart.invalidate()
//
//        val data = LineData()
//        chart.data = data


//        binding.datePicker.setOnClickListener(){
//            val cal = java.util.Calendar.getInstance()
//            // 사용자가 OK버튼을 누르면 바인딩된 text가 바뀜
//            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, day ->
//                binding.dateText.text = "${year}년. ${month+1}월. ${day}일"
//            }
//            // 다이얼로그 생성, 다이얼로그를 띄울때 오늘 날짜를 기본 데이터로 잡아준다.
//            //DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
//            //스피너 모드로 바꿔주기 deprecated 됐기 때문에 themes 에서 추가해줘야함
//            val dateDialog = DatePickerDialog(requireActivity(), dateSetListener, cal.get(java.util.Calendar.YEAR), cal.get(
//                java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH))
//            dateDialog.show()
//            dateDialog.datePicker.spinnersShown = true
//
//        }

//        view?.let { setChartView(it) }


        return binding.root
    }


//    Line Chart 시도
    private fun initChartData() {
        //더미데이터
        chartData.add(Entry(-240f, 0f))
        chartData.add(Entry(-200f, 30f))
        chartData.add(Entry(-50f, 100f))
        chartData.add(Entry(-120f, 20f))
        chartData.add(Entry((1200).toFloat(), 0f))

        var set = LineDataSet(chartData, "set1")
        lineDataSet.add(set)
        lineData = LineData(lineDataSet)

        set.lineWidth = 2F
        set.setDrawValues(false)
        set.highLightColor = Color.TRANSPARENT
        set.mode = LineDataSet.Mode.STEPPED

    }

    private fun initChart() {
        chart.run {
            setDrawGridBackground(false)
            setBackgroundColor(Color.WHITE)
            legend.isEnabled = false
        }

        val xAxis = chart.xAxis
        xAxis.setDrawLabels(true) // label 표시 여부
        xAxis.axisMaximum = 1200f // 60min * 24hour
        xAxis.axisMinimum = -240f
        xAxis.labelCount = 5
        xAxis.valueFormatter = TimeAxisValueFormat()

        xAxis.textColor = Color.BLACK
        xAxis.position = XAxis.XAxisPosition.BOTTOM // x축 라벨 위치
        xAxis.setDrawLabels(true) // gridLine 표시
        xAxis.setDrawAxisLine(true) // AxisLine 표시

        // 왼쪽 y축 값
        val yLAxis = chart.axisLeft
        yLAxis.axisMaximum = 4.5f // y축 최대값
        yLAxis.axisMinimum = -0.5f // y축 최소값

        // 왼쪽 y축 도메인 변경
        val yAxisVals = ArrayList<String>(Arrays.asList("20", "50", "80", "110", "140"))
        yLAxis.valueFormatter = IndexAxisValueFormatter(yAxisVals)
        yLAxis.granularity = 1f

        // 오른쪽 y축 값
        val yRAxix = chart.axisRight
        yRAxix.setDrawLabels(false)
        yRAxix.setDrawAxisLine(false)
        yRAxix.setDrawGridLines(false)

        // 마커 설정
        val marker = MarkerView(requireContext(), com.example.saloris.R.drawable.graph_marker)
        marker.chartView = chart
        chart.marker = marker

        chart!!.description.isEnabled = false // 설명
        chart!!.data = lineData // 데이터 설정

        chart!!.invalidate() // 다시 그리기
    }

    private fun prepareChartData(data: LineData, lineChart: LineChart) {
        lineChart.data = data // LineData 전달
        lineChart.invalidate() // LineChart 갱신해 데이터 표시
    }


//    bar Chart 시도
//    private fun setChartView(view: View) {
//        var chart = view.findViewById<BarChart>(R.id.day_chart)
//        setChart(chart)
//    }
//
//    private fun initBarDataSet(barDataSet: BarDataSet) {
//        //Changing the color of the bar
//        barDataSet.color = Color.parseColor("#304567")
//        //Setting the size of the form in the legend
//        barDataSet.formSize = 15f
//        //showing the value of the bar, default true if not set
//        barDataSet.setDrawValues(false)
//        //setting the text size of the value of the bar
//        barDataSet.valueTextSize = 12f
//    }
//
//    private fun setChart(barChart: BarChart) {
//        initBarChart(barChart)
//
//        barChart.setScaleEnabled(false) //Zoom In/Out
//
//        val valueList = ArrayList<Double>()
//        val entries: ArrayList<BarEntry> = ArrayList()
//        val title = "걸음 수"
//
//        //input data
//        for (i in 0..5) {
//            valueList.add(i * 100.1)
//        }
//
//        //fit the data into a bar
//        for (i in 0 until valueList.size) {
//            val barEntry = BarEntry(i.toFloat(), valueList[i].toFloat())
//            entries.add(barEntry)
//        }
//        val barDataSet = BarDataSet(entries, title)
//        val data = BarData(barDataSet)
//        barChart.data = data
//        barChart.invalidate()
//    }
//
//    private fun initBarChart(barChart: BarChart) {
//        //hiding the grey background of the chart, default false if not set
//        barChart.setDrawGridBackground(false)
//        //remove the bar shadow, default false if not set
//        barChart.setDrawBarShadow(false)
//        //remove border of the chart, default false if not set
//        barChart.setDrawBorders(false)
//
//        //remove the description label text located at the lower right corner
//        val description = Description()
//        description.setEnabled(false)
//        barChart.setDescription(description)
//
//        //X, Y 바의 애니메이션 효과
//        barChart.animateY(1000)
//        barChart.animateX(1000)
//
//
//        //바텀 좌표 값
//        val xAxis: XAxis = barChart.getXAxis()
//        //change the position of x-axis to the bottom
//        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        //set the horizontal distance of the grid line
//        xAxis.granularity = 1f
//        xAxis.textColor = Color.RED
//        //hiding the x-axis line, default true if not set
//        xAxis.setDrawAxisLine(false)
//        //hiding the vertical grid lines, default true if not set
//        xAxis.setDrawGridLines(false)
//
//
//        //좌측 값 hiding the left y-axis line, default true if not set
//        val leftAxis: YAxis = barChart.getAxisLeft()
//        leftAxis.setDrawAxisLine(false)
//        leftAxis.textColor = Color.RED
//
//
//        //우측 값 hiding the right y-axis line, default true if not set
//        val rightAxis: YAxis = barChart.getAxisRight()
//        rightAxis.setDrawAxisLine(false)
//        rightAxis.textColor = Color.RED
//
//
//        //바차트의 타이틀
//        val legend: Legend = barChart.getLegend()
//        //setting the shape of the legend form to line, default square shape
//        legend.form = Legend.LegendForm.LINE
//        //setting the text size of the legend
//        legend.textSize = 11f
//        legend.textColor = Color.YELLOW
//        //setting the alignment of legend toward the chart
//        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
//        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
//        //setting the stacking direction of legend
//        legend.orientation = Legend.LegendOrientation.HORIZONTAL
//        //setting the location of legend outside the chart, default false if not set
//        legend.setDrawInside(false)
//    }
}