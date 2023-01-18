package com.example.saloris

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.saloris.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils


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
        Log.d("x",e.x.toString())
        val time_min = Utils.formatNumber(e.x.toInt().toFloat(), 0, true).toInt()
        val minute = (time_min%60).toString()
        var hour =""
        if(type=="Show"){
            hour = (time_min/60+9).toString()
        }else{
            hour = (time_min/60+9).toString()
        }
        val mark = hour + ":"+minute
        if (e is CandleEntry) {

            tvContent.text ="heartRate:"+Utils.formatNumber(e.y.toInt().toFloat(), 0, true)+"time"+mark

        } else {
            tvContent.text ="heartRate:"+Utils.formatNumber(e.y.toInt().toFloat(), 0, true)+"\ntime"+mark
        }// "" +  Utils.formatNumber(e.y.toInt().toFloat(), 0, true)+ "" + Utils.formatNumber(e.high.toInt().toFloat(), 0, true) +
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
    private fun getValue(e:Entry): String? {
        // e => x : 시간(분) y: 심박수
        val t=e.toString().split(',')[1].replace(":", "=").replace(" ","")
        val t1=t.replace("x","time").replace("y","heartrate")
        Log.d("hello",t)
        return t
    }
    private fun getTime(time_min :Int):String{
        val time : String = ""
        val minute = (time_min%60).toString()
        val hour = (time_min/60-9).toString()
        return hour + ":"+minute
    }
}
//import android.content.Context
//import android.util.Log
//import android.view.View
//import android.widget.TextView
//import com.github.mikephil.charting.components.MarkerView
//import com.github.mikephil.charting.data.Entry
//import com.github.mikephil.charting.highlight.Highlight
//
//
//class MyMarkerView (context: Context?, layoutResource: Int) :
//    MarkerView(context, layoutResource) {
//    private val tvContent: TextView
//
//    init {
//        // this markerview only displays a textview
//        Log.d("init","markerviewinit")
//        tvContent = findViewById<View>(R.id.tvContent) as TextView
//    }
//
//    // callbacks everytime the MarkerView is redrawn, can be used to update the
//    // content (user-interface)
//    override fun refreshContent(e: Entry, highlight: Highlight) {
//        tvContent.text = "" + getValue(e) // set the entry-value as the display text
//    }
//
//    fun getXOffset(xpos: Float): Int {
//        // this will center the marker-view horizontally
//        return -(width / 2)
//    }
//
//    fun getYOffset(ypos: Float): Int {
//        // this will cause the marker-view to be above the selected value
//        return -height
//    }
//}
//
//private fun getValue(e:Entry): String? {
//     // e => x : 시간(분) y: 심박수
//    val t=e.toString().split(',')[1].replace(":", "=").replace("x","time").replace("y","heartrate")
//    Log.d("hello",t)
//    return t
//}
