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
import androidx.lifecycle.ViewModelProvider
import com.example.saloris.R
import com.example.saloris.data.InfluxDB
import com.example.saloris.databinding.FragmentRecordBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
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

        chart =  binding.dayChart

        prepareChartData(lineData, chart)
        initChartData()
        initChart()

        binding.calendarView1.setVisibility(View.GONE)
        binding.dayChart.setVisibility(View.GONE)
        binding.heartRateNum.setVisibility(View.GONE)

        binding.dateChoiceBtn.setOnClickListener {
            var CalendarView = R.id.calendar_view1

            val dateText: TextView = requireView().findViewById(R.id.date_text)
            val calendarView: CalendarView = requireView().findViewById(R.id.calendar_view1)

            calendarView.maxDate = System.currentTimeMillis()

            //val dateFormat: DateFormat = SimpleDateFormat("yyyy년MM월dd일")

            val date: Date = Date(calendarView.date)

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
                }

                calendarView.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
                    var day: String = "${year}년 ${month + 1}월 ${dayOfMonth}일"

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

        return binding.root
    }

//    //    Line Chart 시도
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
        val yAxisVals = ArrayList<String>(Arrays.asList("50", "60", "70", "80", "90", "100"))
        yLAxis.valueFormatter = IndexAxisValueFormatter(yAxisVals)
        yLAxis.granularity = 1f

        // 오른쪽 y축 값
        val yRAxix = chart.axisRight
        yRAxix.setDrawLabels(false)
        yRAxix.setDrawAxisLine(false)
        yRAxix.setDrawGridLines(false)

        // 마커 설정
//        val marker = MarkerView(requireContext(), com.example.saloris.R.drawable.graph_marker)
//        marker.chartView = chart
//        chart.marker = marker

        chart!!.description.isEnabled = false // 설명
        chart!!.data = lineData // 데이터 설정

        chart!!.invalidate() // 다시 그리기
    }

    private fun prepareChartData(data: LineData, lineChart: LineChart) {
        lineChart.data = data // LineData 전달
        lineChart.invalidate() // LineChart 갱신해 데이터 표시
    }
}