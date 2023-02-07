package com.example.saloris

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.databinding.FragmentSettingBinding
import com.example.saloris.util.MakeToast
import com.example.saloris.util.OpenDialog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.nio.charset.StandardCharsets


class SettingFragment : Fragment(), CoroutineScope by MainScope(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener{

    var activityContext: Context? = null

    private val wearableAppCheckPayload = "AppOpenWearable"
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"
    private var wearableDeviceConnected: Boolean = false

    private var currentAckFromWearForAppOpenCheck: String? = null
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"

//    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"

    private val TAG_GET_NODES: String = "getnodes1"

//    private val TAG_MESSAGE_RECEIVED: String = "receive1"

    var last_time = ""

    /* View */
    private lateinit var binding: FragmentSettingBinding
    private lateinit var navController: NavController

    /* Dialog */
    private val dialog = OpenDialog()

    /* Toast */
    private val toast = MakeToast()

    /* User Authentication */
    private lateinit var auth: FirebaseAuth
    var newRate = ""

    private fun deleteAutoLoginInfo() {
        val autoLoginPref =
            requireContext().getSharedPreferences("autoLogin", Activity.MODE_PRIVATE)
        val autoLoginEdit = autoLoginPref.edit()
        autoLoginEdit.clear()
        autoLoginEdit.apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityContext = this.context
        wearableDeviceConnected = false
        if (!wearableDeviceConnected) {
            val tempAct: Activity = activityContext as AppCompatActivity
            //Couroutine
            initialiseDevicePairing(tempAct)
        }

        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        // userName, userId 받아오기
//        binding.userName.text = auth.currentUser!!.displayName
//        binding.userId.text = auth.currentUser!!.email

        // 로그아웃 -> 로그인 화면
        binding.btnLogout.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("로그아웃")
                .setMessage("계정을 로그아웃 하시겠습니까?")
                .setPositiveButton("네",
                    DialogInterface.OnClickListener { dialog, id ->
                        auth.signOut()
                        deleteAutoLoginInfo()
                        navController.navigate(R.id.action_settingFragment_to_loginStartFragment)
                    })
                .setNegativeButton("아니오",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
            builder.show()
        }
        // 도움말
        binding.btnHelp.setOnClickListener {
            navController.navigate(R.id.action_settingFragment_to_helpFragment)
        }
        binding.btnAccountSettings.setOnClickListener {
            navController.navigate(R.id.action_settingFragment_to_accountFragment)
        }
        binding.test.setOnClickListener {
            navController.navigate(R.id.action_settingFragment_to_requiredInfoFragment)
        }

        binding.disconnectBtn.setOnClickListener {
            //wearable device가 연결되었는지 확인하는 버튼
            if (!wearableDeviceConnected) {
                val tempAct: Activity = requireActivity() as AppCompatActivity
                //Couroutine
                initialiseDevicePairing(tempAct)
            }
//            val builder = AlertDialog.Builder(requireContext())
//            builder.setTitle("연결 해제")
//                .setMessage("워치 연결을 정말로 해제하시겠습니까?")
//                .setPositiveButton("네",
//                    DialogInterface.OnClickListener { dialog, id ->
//
//                    })
//                .setNegativeButton("아니오",
//                    DialogInterface.OnClickListener { dialog, id ->
//
//                    })
//            builder.show()
        }
        //wearable device가 연결되었는지 확인
    }

    //wearOS와 연동되었는지 확인
    @SuppressLint("SetTextI18n")
    private fun initialiseDevicePairing(tempAct: Activity) {
        var cardColor = ContextCompat.getDrawable(requireContext(),R.drawable.ligt_blue_round_btn)
        var textColor = ContextCompat.getColor(requireContext(),R.color.black)

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

                val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = requireContext()!!.registerReceiver(null, ifilter)

                val level = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
//                val scale = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
//                val batteryPct = level

                val nodeId = getNodes(requireContext())

//                val client = Wearable.getNodeClient(requireContext())
//                val result = client.getBatteryInfo(nodeId).await()
//                if (result.status.isSuccess) {
//                    val batteryInfo = result.batteryInfo
//                    val level = batteryInfo.chargeLevel
//                    val scale = batteryInfo.chargeStatus
//                }

                if (getNodesResBool!![0]) {
                    //if message Acknowlegement Received
                    if (getNodesResBool[1]) {
                        //워치와 연결, 앱이 열려있음
                        wearableDeviceConnected = true
                        binding.disconnectBtn.setBackgroundDrawable(cardColor)
                        binding.disconnectBtn.setTextColor(textColor)
                        binding.disconnectBtn.setText("워치 연결 정보 있음")
                        binding.watchBattery.setText((level).toString())


                    } else {
                        //워치와 연결, 앱이 닫혀있음
                        wearableDeviceConnected = false
                        //binding.sendmessageButton.visibility = View.GONE
                        binding.disconnectBtn.setBackgroundDrawable(cardColor)
                        binding.disconnectBtn.setTextColor(textColor)
                        binding.disconnectBtn.setText("워치 연결 정보 있음")
                        binding.watchBattery.setText((level).toString())

                    }
                } else {
                    //워치와 연결되지 않음
                    wearableDeviceConnected = false
                    //binding.sendmessageButton.visibility = View.GONE
//                    cardColor = ContextCompat.getColor(requireContext(),R.color.grey)
//                    binding.startBtn.setCardBackgroundColor(cardColor)
//                    binding.explanationTv.setText("운행 시작 버튼이 회색인 경우\n워치와 연결이 안되었다는 뜻이에요")
                    binding.disconnectBtn.setText("워치 연결 정보 없음")
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
            val rateAndBattery =String(p0.data, StandardCharsets.UTF_8).split("/")
            val s =rateAndBattery[0]
            val battery = rateAndBattery[1]
            Log.d("battery",battery)
            binding.watchBattery.setText(battery)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("receive1", "Handled")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onDataChanged(p0: DataEventBuffer) {
        TODO("Not yet implemented")
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