package com.example.saloris

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.databinding.FragmentDriveBinding
import com.example.saloris.util.MakeToast
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.HashSet

class DriveFragment : Fragment(), CoroutineScope by MainScope(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {
    /* View */
    private lateinit var binding: FragmentDriveBinding
    private lateinit var navController: NavController
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

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    var btnBackPressedTime: Long = 0
    var newRate = ""

    /* Toast */
    private val toast = MakeToast()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityContext = this.context
        wearableDeviceConnected = false
        if (!wearableDeviceConnected) {
            val tempAct: Activity = activityContext as AppCompatActivity
            //Couroutine
            initialiseDevicePairing(tempAct)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDriveBinding.inflate(layoutInflater, container, false)
        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        wearableDeviceConnected = false
        navController = Navigation.findNavController(view)
        activityContext = this.context

        binding.checkConnect.setOnClickListener {
            //워치 진동 버튼 => 누르면 워치에서 진동 발생
            toast.makeToast(requireContext(), "vibrate")
            sendMessage("vibrator")
        }
        binding.finishDriveBtn.setOnClickListener {
            binding.driveState.setText("운행 종료")
            navController.navigate(R.id.action_driveFragment_to_homeFragment)
        }
    }

    override fun onDataChanged(p0: DataEventBuffer) {
        TODO("Not yet implemented")
    }
    private fun sendMessage(message : String){
        toast.makeToast(requireContext(), "send message")
        println(wearableDeviceConnected)
        if (wearableDeviceConnected) {
            if (binding.heartRate.text!!.isNotEmpty()) {
                toast.makeToast(requireContext(), "send message")
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
                    toast.makeToast(requireContext(), "addOnCompleteListener")
                    if (it.isSuccessful) {
                        toast.makeToast(requireContext(), "연결 성공")


                    } else {
                        toast.makeToast(requireContext(), "다시 연결")
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
    //wearOS와 연동되었는지 확인
    @SuppressLint("SetTextI18n")
    private fun initialiseDevicePairing(tempAct: Activity) {
        var cardColor = ContextCompat.getColor(requireContext(),R.color.line_primary)

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
                        wearableDeviceConnected = true
                    } else {
                        wearableDeviceConnected = false
                    }
                } else {
                    wearableDeviceConnected = false
                }
            }
        }
    }
    //현재 모바일이랑 연동된 워치의 node를 가져와서 확인
    private fun getNodes(context: Context): BooleanArray {
        val nodeResults = HashSet<String>()
        val resBool = BooleanArray(2)
        resBool[0] = false //nodePresent : true이면 연결되어있다
        resBool[1] = false //wearableReturnAckR eceived : true이면 워치에 앱이 열려있다
        val nodeListTask =
            Wearable.getNodeClient(context).connectedNodes
        try {
            // Block on a task and get the result synchronously (because this is on a background thread).
            val nodes =
                Tasks.await(
                    nodeListTask
                )
            //Log.e(TAG_GET_NODES, "Task fetched nodes")
            for (node in nodes) {
                //Log.e(TAG_GET_NODES, "inside loop")
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

                binding.heartRate.text = sbTemp

                messageEvent = p0
                wearableNodeUri = p0.sourceNodeId
            } else if (messageEventPath.isNotEmpty() && messageEventPath == MESSAGE_ITEM_RECEIVED_PATH) {
                //워치에서 보낸 심박수를 받는다
                try {
                    val dateAndTime : LocalDateTime = LocalDateTime.now()
                    val sbTemp = StringBuilder()
                    sbTemp.append(s)//심박수
                    var textColor = ContextCompat.getColor(requireContext(),R.color.black)
                    if (s.toInt() > 90){
                        textColor = ContextCompat.getColor(requireContext(),R.color.line_warning)
                        binding.heartRate.setTextColor(textColor)
                    }
                    else if (s.toInt() < 70){
                        textColor = ContextCompat.getColor(requireContext(),R.color.teal_200)
                        binding.heartRate.setTextColor(textColor)
                        sendMessage("vibrate")
                    } else {
                        textColor = ContextCompat.getColor(requireContext(),R.color.black)
                        binding.heartRate.setTextColor(textColor)
                    }
                    binding.heartRate.text = sbTemp.toString()
                    newRate = s
                    Log.d("onMessageReceived_mainActivity","feedMultiple()")
                    //chart에 표시
                    //feedMultiple()
                    //DB로 데이터 전송
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        val isInserted = async { insertDB(newRate) }
//                        Log.d("isInserted",newRate)
//                        Log.d("time", dateAndTime.toString())
//                    }
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
        TODO("Not yet implemented")
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