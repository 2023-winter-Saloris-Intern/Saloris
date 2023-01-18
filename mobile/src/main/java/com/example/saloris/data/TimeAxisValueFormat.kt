package com.example.saloris


import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
//x축의 값을 시 : 분 형식으로 바꿔서 출력하도록
class TimeAxisValueFormat :IndexAxisValueFormatter(){
    override fun getFormattedValue(value: Float): String {
        val valueToMinutes = TimeUnit.MINUTES.toMillis(value.toLong())
        var timeMimutes = Date(valueToMinutes)
        var formatMinutes = SimpleDateFormat("HH:mm") // 시 : 분 형식

        return formatMinutes.format(timeMimutes)
    }
}