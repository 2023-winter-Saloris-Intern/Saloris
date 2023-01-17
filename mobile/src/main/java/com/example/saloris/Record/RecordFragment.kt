package com.example.saloris.Record

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.saloris.R
import com.example.saloris.databinding.FragmentRecordBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toTimeUnit


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

class RecordFragment : Fragment() {
    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private var chartData = ArrayList<Entry>() // 데이터배열
    private var lineDataSet = ArrayList<ILineDataSet>() // 데이터배열 -> 데이터 셋
    private var lineData: LineData = LineData()
    lateinit var chart: LineChart

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
        binding.dateChoiceBtn.setOnClickListener {

//          val datepickerHeaderid = binding.datePicker.getChildAt(0)
//              .resources.getIdentifier("date_picker_header", "id", "android")
//          binding.datePicker.findViewById<View>(datepickerHeaderid).visibility = View.GONE

            val cal = java.util.Calendar.getInstance()
            // 사용자가 OK버튼을 누르면 바인딩된 text가 바뀜
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, day ->
                binding.dateText.text = "${year}년 ${month+1}월 ${day}일"
            }
            // 다이얼로그 생성, 다이얼로그를 띄울때 오늘 날짜를 기본 데이터로 잡아준다.
            //DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            //스피너 모드로 바꿔주기 deprecated 됐기 때문에 themes 에서 추가해줘야함
            val dateDialog = DatePickerDialog(requireActivity(), dateSetListener, cal.get(java.util.Calendar.YEAR), cal.get(
                java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH))
                .apply {
                    datePicker.maxDate = System.currentTimeMillis()
                }

            dateDialog.show()
            //dateDialog.datePicker.spinnersShown = true
        }


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
        return binding.root
    }

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
        val marker = MarkerView(requireContext(), R.drawable.graph_marker)
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

}