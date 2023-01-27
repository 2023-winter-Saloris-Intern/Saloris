package com.example.saloris.data
//알림
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.saloris.MyMarkerView
import com.example.saloris.R
import com.example.saloris.databinding.ActivityNetworkingBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.influxdb.client.write.Point
import com.influxdb.exceptions.InfluxException
import kotlinx.coroutines.*
import java.nio.charset.StandardCharsets
import java.time.*
import java.util.*

class Networking : AppCompatActivity(), CoroutineScope by MainScope(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {
    var activityContext: Context? = null
    private val wearableAppCheckPayload = "AppOpenWearable"
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"
    private var wearableDeviceConnected: Boolean = false

    private var currentAckFromWearForAppOpenCheck: String? = null
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"

    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"

    private val TAG_GET_NODES: String = "getnodes1"
    private val TAG_MESSAGE_RECEIVED: String = "receive1"

    private var messageEvent: MessageEvent? = null
    private var wearableNodeUri: String? = null
    //chart
    private var chart: LineChart? = null
    private var thread: Thread? = null

    /* User Authentication */
    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityNetworkingBinding
    var Rate = ""
    var newRate = ""
    var last_time=""
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //user auth
        auth = Firebase.auth
        binding = ActivityNetworkingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //chart
        chart = findViewById<View>(R.id.chart) as LineChart
        chart!!.xAxis.position = XAxis.XAxisPosition.BOTTOM // x축 밑으로
        //chart!!.yAxis.axisMinimum=0f
        //chart!!.xAxis.valueFormatter = TimeAxisValueFormat() // x축의 출력 형식을 시:분으로
        chart!!.xAxis.setDrawLabels(true)
        chart!!.xAxis.axisMinimum=0f // 9시부터
        chart!!.xAxis.axisMaximum=1200f // 오전 5시까지..?
        chart!!.axisRight.isEnabled = false // 오른쪽 y축은 없애고
        chart!!.axisLeft.axisMaximum=80f // y축 min,max
        chart!!.axisLeft.axisMinimum=50f
        chart!!.legend.textColor = Color.BLUE
        chart!!.animateXY(10, 10)
        chart!!.invalidate()
        val data = LineData()
        chart!!.data = data
        val mv = MyMarkerView(this, R.layout.custom_marker_view,"Realtime") // markerview
        // set the marker to the chart
        chart!!.setMarker(mv);
        //feedMultiple()

        activityContext = this
        wearableDeviceConnected = false

        var influxDB = InfluxDB()
        binding.checkwearablesButton.setOnClickListener {
            //wearable device가 연결되었는지 확인하는 버튼
            if (!wearableDeviceConnected) {
                val tempAct: Activity = activityContext as AppCompatActivity
                //Couroutine
                initialiseDevicePairing(tempAct)
            }
        }
        binding.stopWearOS.setOnClickListener {
            //워치 앱 종료 버튼
            Log.d("clicked stop","stop")
            //send message to wear os
            sendMessage("stop")
        }
        binding.sendToDB.setOnClickListener {
            //DB에서 가져온 데이터를 출력하는 화면으로 이동
            Log.d("clicked",newRate)
            startActivity(Intent(this@Networking,showData::class.java))
        }
        binding.sendmessageButton.setOnClickListener {
            //워치 진동 버튼 => 누르면 워치에서 진동 발생F
            sendMessage("vibrator")
        }
    }

    private fun sendMessage(message : String){
        println("send Message$wearableDeviceConnected")
        if (wearableDeviceConnected) {
            if (binding.messagelogTextView.text!!.isNotEmpty()) {
                Log.d("send","send button clicked")
                val nodeId: String = messageEvent?.sourceNodeId!!
                // Set the data of the message to be the bytes of the Uri.
                val payload: ByteArray =
                    message.toByteArray()

                // Send the rpc
                // Instantiates clients without member variables, as clients are inexpensive to
                // create. (They are cached and shared between GoogleApi instances.)
                val sendMessageTask =
                    Wearable.getMessageClient(activityContext!!)
                        .sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)

                sendMessageTask.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("send1", "Message sent successfully")
                        val sbTemp = StringBuilder()
                        sbTemp.append("\n")
                        sbTemp.append(message.toString())
                        sbTemp.append(" (Sent to Wearable)")
                        //Log.d("receive1", " $sbTemp")
                        binding.messagelogTextView.append(sbTemp)

                        binding.scrollviewText.requestFocus()
                        binding.scrollviewText.post {
                            binding.scrollviewText.scrollTo(0, binding.scrollviewText.bottom)
                        }
                    } else {
                        Log.d("send1", "Message failed.")
                    }
                }
            } else {
                Toast.makeText(
                    activityContext,
                    "Message content is empty. Please enter some message and proceed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    //chart
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addEntry() {
        val data = chart!!.data
        Log.d("addEntry_mainActivity",data.toString())
        if (data != null) {
            var set = data.getDataSetByIndex(0)
            if (set == null) {
                set = createSet()
                data.addDataSet(set)
            }
            val dateAndTime : LocalDateTime = LocalDateTime.now()
            Log.d("dateAndTime-main",dateAndTime.toString())
            val time_hour = dateAndTime.toString().substring(11,13)
            val time_min = dateAndTime.toString().substring(14,16)
            Log.d("time_hour-main",time_hour)
            Log.d("time_min-main",time_min)
            //현재 시간을 분으로 바꿔서 Entry x축에 넣기
            val time  = (time_hour.toInt())*60+time_min.toInt()
            Log.d("time-main",time.toString())
            //val rand = (Math.random() * 4).toFloat() + 60f
            Log.d("value-main",newRate)
            //TODO time :real time
            val time_to = time.toFloat()-9*60
            data.addEntry(Entry(set.entryCount.toFloat(),newRate.toFloat()), 0)
            Log.d("time_x",time.toString())
            data.notifyDataChanged()
            chart!!.notifyDataSetChanged()
            chart!!.setVisibleXRangeMaximum(10f) // x축을 10까지만 보여주고 그 이후부터는 이동..
            chart!!.moveViewToX(set.entryCount.toFloat()-10f) // 가장 최근 추가한 데이터로 이동
            Log.d("addEntry_mainActivity",(time_to.toFloat()-10f).toString())
        }
    }

    private fun createSet(): LineDataSet {
        //chart : 설정
        Log.d("createSet_mainActivity","createSet")
        val set = LineDataSet(null, "Heart Rate")
        set.fillAlpha = 110

        set.fillColor = Color.parseColor("#d7e7fa")//선 밑에 색채우기
        set.color = Color.parseColor("#0B80C9")//선 색

        set.setCircleColor(Color.parseColor("#FFA1B4DC"))
        //set.setCircleColorHole(Color.BLUE)
        set.valueTextColor = Color.BLUE //TODO : 글자 색 .. chart에 값 확인하기
        set.valueTextSize=10f
        set.setDrawValues(false)
        set.lineWidth = 2f
        set.circleRadius = 6f
        set.setDrawCircleHole(false)
        set.setDrawCircles(false)
        set.valueTextSize = 9f
        set.setDrawFilled(true)
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.highLightColor = Color.rgb(244, 117, 117)
        return set
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun feedMultiple() {
        if (thread != null) thread!!.interrupt()
        val runnable = Runnable { addEntry() }
        Log.d("feedMultiple_mainActivity","feedMultiple")
        thread = Thread {
            runOnUiThread(runnable)
        }
        thread!!.start()
    }
    //DB에 heartrate를 넣는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun insertDB(rate: Int):Boolean {
//val token = System.getenv()["INFLUX_TOKEN"]
        val user="user"
        //사용자 uid
        val Uid=auth.currentUser?.uid
        //val Uid = "T"
        val org = "intern"
        val bucket = "HeartRate"
        //influxDB token
        val token = "yZmCmFFTYYoetepTiOpXDRK8oyL1f_orD6oZH8SXsvlf213z-_iRmXtaf-AjyLe2HS-NhfxcNeY-0K6qR0k6Sw=="
        print(System.getenv())
        val client = InfluxDBClientKotlinFactory.create("https://europe-west1-1.gcp.cloud2.influxdata.com", token!!.toCharArray(), org, bucket)
        client.use {
            val writeApi = client.getWriteKotlinApi()
            try {
                //todo : issleep?
                var issleep = false
                if(rate<65){
                    issleep=true
                }
                val map1 = mutableMapOf<String,Any>("heart" to rate, "isntsleep" to issleep)
                val point = Point
                    .measurement(user)
                    .addTag("Uid", Uid)
                    .addFields(map1)//influxDB의 field의 type은 한번 정하면 바꿀 수 없다. field변경!
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
    //wearOS와 연동되었는지 확인
    @SuppressLint("SetTextI18n")
    private fun initialiseDevicePairing(tempAct: Activity) {
        //Coroutine
        launch(Dispatchers.Default) {
            var getNodesResBool: BooleanArray? = null

            try {
                Log.d("try","try get nodes res bool")
                getNodesResBool =
                    getNodes(tempAct.applicationContext)
            } catch (e: Exception) {
                Log.d("try","try exception")
                e.printStackTrace()
            }

            //UI Thread
            withContext(Dispatchers.Main) {
                getNodesResBool?.get(1)?.let { Log.d("getnodesresbool : ", it.toString()) }
                if (getNodesResBool!![0]) {
                    //if message Acknowlegement Received
                    if (getNodesResBool[1]) {
                        Toast.makeText(
                            activityContext,
                            "Wearable device paired and app is open. Tap the \"Send Message to Wearable\" button to send the message to your wearable device.",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.deviceconnectionStatusTv.text =
                            "Wearable device paired and app is open."
                        //binding.deviceconnectionStatusTv.visibility = View.VISIBLE
                        wearableDeviceConnected = true
                        //binding.sendmessageButton.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            activityContext,
                            "A wearable device is paired but the wearable app on your watch isn't open. Launch the wearable app and try again.",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.deviceconnectionStatusTv.text =
                            "Wearable device paired but app isn't open."
                        //binding.deviceconnectionStatusTv.visibility = View.VISIBLE
                        wearableDeviceConnected = false
                        binding.sendmessageButton.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(
                        activityContext,
                        "No wearable device paired. Pair a wearable device to your phone using the Wear OS app and try again.",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.deviceconnectionStatusTv.text =
                        "Wearable device not paired and connected."
                    //binding.deviceconnectionStatusTv.visibility = View.VISIBLE
                    wearableDeviceConnected = false
                    //binding.sendmessageButton.visibility = View.GONE
                }
            }
        }
    }
    //현재 모바일이랑 연동된 워치의 node를 가져와서 확인
    private fun getNodes(context: Context): BooleanArray {
        val nodeResults = HashSet<String>()
        val resBool = BooleanArray(2)
        resBool[0] = false //nodePresent : true이면 연결되어있다
        resBool[1] = false //wearableReturnAckReceived : true이면 워치에 앱이 열려있다
        val nodeListTask =
            Wearable.getNodeClient(context).connectedNodes
        try {
            // Block on a task and get the result synchronously (because this is on a background thread).
            val nodes =
                Tasks.await(
                    nodeListTask
                )
            Log.e(TAG_GET_NODES, "Task fetched nodes")
            for (node in nodes) {
                Log.e(TAG_GET_NODES, "inside loop")
                nodeResults.add(node.id)
                try {
                    val nodeId = node.id
                    // Set the data of the message to be the bytes of the Uri.
                    val payload: ByteArray = wearableAppCheckPayload.toByteArray()
                    // Send the rpc
                    // Instantiates clients without member variables, as clients are inexpensive to
                    // create. (They are cached and shared between GoogleApi instances.)
                    val sendMessageTask =
                        Wearable.getMessageClient(context)
                            .sendMessage(nodeId, APP_OPEN_WEARABLE_PAYLOAD_PATH, payload)
                    Log.d("sendMessageToWearable","send message")
                    //워치로 메시지를 보내고 5번동안 받으면 앱이 열려있고 안받으면 앱이 없다
                    try {
                        // Block on a task and get the result synchronously (because this is on a background thread).
                        val result = Tasks.await(sendMessageTask)
                        Log.d(TAG_GET_NODES, "send message result : $result")
                        resBool[0] = true
                        //Wait for 1000 ms/1 sec for the acknowledgement message
                        //Wait 1
                        if (currentAckFromWearForAppOpenCheck != wearableAppCheckPayloadReturnACK) {
                            Thread.sleep(100)
                            Log.d(TAG_GET_NODES, "ACK thread sleep 1")
                        }
                        if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                            resBool[1] = true
                            return resBool
                        }
                        //Wait 2
                        if (currentAckFromWearForAppOpenCheck != wearableAppCheckPayloadReturnACK) {
                            Thread.sleep(150)
                            Log.d(TAG_GET_NODES, "ACK thread sleep 2")
                        }
                        if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                            resBool[1] = true
                            return resBool
                        }
                        //Wait 3
                        if (currentAckFromWearForAppOpenCheck != wearableAppCheckPayloadReturnACK) {
                            Thread.sleep(200)
                            Log.d(TAG_GET_NODES, "ACK thread sleep 3")
                        }
                        if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                            resBool[1] = true
                            return resBool
                        }
                        //Wait 4
                        if (currentAckFromWearForAppOpenCheck != wearableAppCheckPayloadReturnACK) {
                            Thread.sleep(250)
                            Log.d(TAG_GET_NODES, "ACK thread sleep 4")
                        }
                        if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                            resBool[1] = true
                            return resBool
                        }
                        //Wait 5
                        if (currentAckFromWearForAppOpenCheck != wearableAppCheckPayloadReturnACK) {
                            Thread.sleep(350)
                            Log.d(TAG_GET_NODES, "ACK thread sleep 5")
                        }
                        if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                            resBool[1] = true
                            return resBool
                        }
                        resBool[1] = false
                        Log.d(
                            TAG_GET_NODES,
                            "ACK thread timeout, no message received from the wearable "
                        )
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                } catch (e1: Exception) {
                    Log.d(TAG_GET_NODES, "send message exception")
                    e1.printStackTrace()
                }
            } //end of for loop
        } catch (exception: Exception) {
            Log.e(TAG_GET_NODES, "Task failed: $exception")
            exception.printStackTrace()
        }
        return resBool
    }


    override fun onDataChanged(p0: DataEventBuffer) {
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onMessageReceived(p0: MessageEvent) {
        try {
            val s =
                String(p0.data, StandardCharsets.UTF_8)
            val messageEventPath: String = p0.path
            if (messageEventPath == APP_OPEN_WEARABLE_PAYLOAD_PATH) {
                //getNodes()에서 워치앱이 열려있는지 확인하기 위해 보낸 메시지의 답을 받는다
                currentAckFromWearForAppOpenCheck = s
                Log.d(
                    TAG_MESSAGE_RECEIVED,
                    "Received acknowledgement message that app is open in wear"
                )
                val sbTemp = StringBuilder()
                sbTemp.append(binding.messagelogTextView.text.toString())
                sbTemp.append("\nWearable device connected.")

                binding.messagelogTextView.text = sbTemp

                binding.checkwearablesButton.visibility = View.GONE
                messageEvent = p0
                wearableNodeUri = p0.sourceNodeId
            } else if (messageEventPath.isNotEmpty() && messageEventPath == MESSAGE_ITEM_RECEIVED_PATH) {
                //워치에서 보낸 심박수를 받는다
                try {
                    binding.messagelogTextView.visibility = View.VISIBLE
                    //binding.textInputLayout.visibility = View.VISIBLE
                    binding.sendmessageButton.visibility = View.VISIBLE
                    val dateAndTime : LocalDateTime = LocalDateTime.now()
                    val sbTemp = StringBuilder()
                    sbTemp.append("\n")
                    sbTemp.append(s)//심박수
                    sbTemp.append( "==="+dateAndTime.toString())
                    //Log.d("receive1", " $sbTemp")
                    binding.messagelogTextView.text=sbTemp
                    newRate = s
                    binding.scrollviewText.requestFocus()
                    binding.scrollviewText.post {
                        binding.scrollviewText.scrollTo(0, binding.scrollviewText.bottom)
                    }
                    Log.d("onMessageReceived_mainActivity","feedMultiple()")
                    //chart에 표시
                    feedMultiple()
                    //DB로 데이터 전송
                    if(last_time!=dateAndTime.toString().substring(14,16))
                        lifecycleScope.launch(Dispatchers.IO) {
                            val isInserted = async { insertDB(newRate.toInt()) }
                            Log.d("isInserted",newRate)
                            Log.d("time", dateAndTime.toString().substring(14,16))
                            last_time = dateAndTime.toString().substring(14,16)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("receive1", "Handled")
        }
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
    }


    override fun onPause() {
        super.onPause()
        try {
            Wearable.getDataClient(activityContext!!).removeListener(this)
            Wearable.getMessageClient(activityContext!!).removeListener(this)
            Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onResume() {
        super.onResume()
        try {
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getMessageClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
