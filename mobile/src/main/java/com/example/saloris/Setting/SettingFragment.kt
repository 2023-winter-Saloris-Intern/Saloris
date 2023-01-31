package com.example.saloris.Setting

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.MainActivity
import com.example.saloris.R
import com.example.saloris.databinding.FragmentSettingBinding
import com.example.saloris.util.MakeToast
import com.example.saloris.util.OpenDialog
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.HashSet

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
//
//    private var messageEvent: MessageEvent? = null
//    private var wearableNodeUri: String? = null
//
//    private lateinit var onBackPressedCallback: OnBackPressedCallback
//    var btnBackPressedTime: Long = 0

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
            auth.signOut()
            deleteAutoLoginInfo()
            navController.navigate(R.id.action_settingFragment_to_loginStartFragment)
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
                //initialiseDevicePairing(tempAct)
            }
        }

        binding.disconnectBtn.setOnClickListener {
            //wearable device가 연결되었는지 확인하는 버튼
            if (!wearableDeviceConnected) {
                val tempAct: Activity = requireActivity() as AppCompatActivity
                //Couroutine
                initialiseDevicePairing(tempAct)
            }
        }
        //wearable device가 연결되었는지 확인
    }

    //wearOS와 연동되었는지 확인
    @SuppressLint("SetTextI18n")
    private fun initialiseDevicePairing(tempAct: Activity) {
        var cardColor = ContextCompat.getDrawable(requireContext(),R.drawable.blue_round_button)
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
                if (getNodesResBool!![0]) {
                    //if message Acknowlegement Received
                    if (getNodesResBool[1]) {
                        //워치와 연결, 앱이 열려있음
                        wearableDeviceConnected = true
                        cardColor = ContextCompat.getDrawable(requireContext(),R.drawable.ligt_blue_round_btn)
                        binding.disconnectBtn.setBackgroundDrawable(cardColor)
                        binding.disconnectBtn.setTextColor(textColor)
                        //binding.watchInfo.setText()
                    } else {
                        //워치와 연결, 앱이 닫혀있음
                        wearableDeviceConnected = false
                        cardColor = ContextCompat.getDrawable(requireContext(),R.drawable.ligt_blue_round_btn)
                        //binding.sendmessageButton.visibility = View.GONE
                        binding.disconnectBtn.setBackgroundDrawable(cardColor)
                        binding.disconnectBtn.setTextColor(textColor)

                    }
                } else {
                    //워치와 연결되지 않음
                    wearableDeviceConnected = false
                    //binding.sendmessageButton.visibility = View.GONE
//                    cardColor = ContextCompat.getColor(requireContext(),R.color.grey)
//                    binding.startBtn.setCardBackgroundColor(cardColor)
//                    binding.explanationTv.setText("운행 시작 버튼이 회색인 경우\n워치와 연결이 안되었다는 뜻이에요")
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
//        try {
//            val s =
//                String(p0.data, StandardCharsets.UTF_8)
//            val messageEventPath: String = p0.path
//            if (messageEventPath == APP_OPEN_WEARABLE_PAYLOAD_PATH) {
//                //getNodes()에서 워치앱이 열려있는지 확인하기 위해 보낸 메시지의 답을 받는다
//                currentAckFromWearForAppOpenCheck = s
//                Log.d(
//                    TAG_MESSAGE_RECEIVED,
//                    "Received acknowledgement message that app is open in wear"
//                )
//
//                messageEvent = p0
//                wearableNodeUri = p0.sourceNodeId
//            } else if (messageEventPath.isNotEmpty() && messageEventPath == MESSAGE_ITEM_RECEIVED_PATH) {
//                //워치에서 보낸 심박수를 받는다
//                try {
//                    val dateAndTime : LocalDateTime = LocalDateTime.now()
//                    val sbTemp = StringBuilder()
//                    sbTemp.append("\n")
//                    sbTemp.append(s)//심박수
//                    //sbTemp.append( "==="+dateAndTime.toString())
//                    //Log.d("receive1", " $sbTemp")
//                    newRate = s
////                    binding.scrollviewText.requestFocus()
////                    binding.scrollviewText.post {
////                        binding.scrollviewText.scrollTo(0, binding.scrollviewText.bottom)
////                    }
//                    Log.d("onMessageReceived_mainActivity","feedMultiple()")
//                    //chart에 표시
//                    //feedMultiple()
//                    //DB로 데이터 전송
////                    lifecycleScope.launch(Dispatchers.IO) {
////                        val isInserted = async { insertDB(newRate) }
////                        Log.d("isInserted",newRate)
////                        Log.d("time", dateAndTime.toString())
////                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.d("receive1", "Handled")
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        /* Bottom Menu */
        val bottomMenu = (requireActivity() as MainActivity).binding.bottomNav
        bottomMenu.visibility = View.VISIBLE
    }

//    override fun onDetach() {
//        super.onDetach()
//        onBackPressedCallback.remove()
//    }

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