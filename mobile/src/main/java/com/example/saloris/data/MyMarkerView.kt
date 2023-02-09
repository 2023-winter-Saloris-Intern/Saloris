package com.example.saloris

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils

//mpAndroidChart 에서 markerView를 생성
class MyMarkerView(context: Context?, layoutResource: Int,type:String) :
    MarkerView(context, layoutResource) {
    private val tvContent: TextView
    private val type = type
    init {
        tvContent = findViewById<View>(R.id.tvContentHead) as TextView
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry, highlight: Highlight) {
        //선택한 것의 x축의 값 =시간을 분으로 표현한 것
        Log.d("x",e.x.toString())

        val time_min = Utils.formatNumber(e.x.toInt().toFloat(), 0, true).toInt()
        val minute = (time_min%60).toString()
        var hour =""
        if(type=="Show"){
            hour = (time_min/60+9).toString()
        }else{
            hour = (time_min/60+9).toString()
        }
        // 시 : 분 형식으로 바꿔서
        val mark = hour + "시"+minute+"분"
        if (e is CandleEntry) {
            //markerView layout의 text에 넣기
            tvContent.text =mark+"심박수 "+Utils.formatNumber(e.y.toInt().toFloat(), 0, true)+"\n"+mark
        } else {
            tvContent.text ="심박수 "+Utils.formatNumber(e.y.toInt().toFloat(), 0, true)+"\n"+mark
        }// "" +  Utils.formatNumber(e.y.toInt().toFloat(), 0, true)+ "" + Utils.formatNumber(e.high.toInt().toFloat(), 0, true) +
        super.refreshContent(e, highlight)
    }
    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
}
