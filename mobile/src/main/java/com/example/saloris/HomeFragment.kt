package com.example.saloris

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.RequiredInfo.RequiredInfo
import com.example.saloris.databinding.FragmentHomeBinding
import com.example.saloris.util.MakeToast
import com.example.salorisv.DevicePairing
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime


class HomeFragment : Fragment(), CoroutineScope by MainScope(),
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

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    var btnBackPressedTime: Long = 0

    /* Toast */
    private val toast = MakeToast()

    /* User Authentication */
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    var newRate = ""

    private fun isAutoLogined(): Boolean {
        val autoLoginPref =
            requireContext().getSharedPreferences("autoLogin", Activity.MODE_PRIVATE)
        return autoLoginPref.contains("username")
    }

    private fun isOnBoardingFinished(): Boolean {
        val prefs = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return prefs.getBoolean("finished", false)
    }

    private fun isLoginFinished(): Boolean {
        val autoLoginPref =
            requireActivity().getSharedPreferences("autoLogin", Activity.MODE_PRIVATE)
        return autoLoginPref.getBoolean("finished", true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currTime = System.currentTimeMillis()
                val timeDifference = currTime - btnBackPressedTime

                if (timeDifference in 0..2000) {
                    activity?.finish()
                } else {
                    btnBackPressedTime = currTime
                    toast.makeToast(context, "??? ??? ??? ????????? ???????????????.")
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityContext = this.context
        wearableDeviceConnected = false

        /* User Authentication */
        auth = Firebase.auth
        hideLoading()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        //Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

//        var userInfo = RequiredInfo()
//        userInfo?.userName = auth?.currentUser!!.displayName
//        firestore?.collection("users")?.document(auth?.uid!!)?.update("userName", userInfo.userName)
        /* Bottom Menu */
        val bottomMenu = (requireActivity() as MainActivity).binding.bottomNav
        bottomMenu.visibility = View.VISIBLE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wearableDeviceConnected = false
        navController = Navigation.findNavController(view)
        activityContext = this.context

        //?????? ?????? ?????? ???????????? ??????
        val pref: SharedPreferences =
            requireContext().getSharedPreferences("isFirst", Activity.MODE_PRIVATE)
        val first = pref.getBoolean("isFirst", false)
        if (first == false) {
            Log.d("Is first Time?", "first")
            val editor = pref.edit()
            editor.putBoolean("isFirst", true)
            editor.commit()
            //??? ?????? ????????? ?????? ?????? ??????
            navController.navigate(R.id.action_homeFragment_to_IntroSlideFragment)

        } else {
            Log.d("Is first Time?", "not first")
            /* User Authentication */
            if (auth.currentUser == null) {
                //????????? ?????????
                Log.d("homeFragment", "????????? ?????????")
                if (!isAutoLogined()) {
                    context?.let { toast.makeToast(it, "???????????? ??????????????????.") }
                    navController.navigate(R.id.action_homeFragment_to_loginStartFragment)
                }
            } else {
                //????????? ?????????
                Log.d("homeFragment", "????????? ?????????")
                checkData()
                if (!auth.currentUser?.isEmailVerified!!) {
                    //????????? ????????? ????????? ????????????
                    context?.let { toast.makeToast(it, "??????????????? ??????????????????") }
                }
                binding.userName.text = auth.currentUser!!.displayName
                binding.userName2.text = auth.currentUser!!.displayName
            }
        }

        binding.startBtn.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_driveFragment)
        }

        binding.checkConnect.setOnClickListener {
            //wearable device??? ?????????????????? ???????????? ??????
            Log.d("?????? ?????? ??????","????????????????????????????????????????????????????????????")
            if (!wearableDeviceConnected) {
                val tempAct: Activity = requireActivity() as AppCompatActivity
                //Couroutine
                initialiseDevicePairing(tempAct)
            }
        }
        //wearable device??? ?????????????????? ??????
    }

    fun checkData() {
        Log.d("checkData", "checkData ??????")
        //Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser
        val userRef = firestore.collection("users").document(currentUser!!.uid)
        userRef.get()
            .addOnSuccessListener { document ->
                Log.d("document", "$document!!!!!!!!!!!")
                if (document != null) {
                    showLoading()
                    val userSex = document.getBoolean("userSex")
                    val userBirth = document.getString("userBirth")
                    val userWeight = document.getString("userWeight")
                    val userHeight = document.getString("userHeight")
                    val userSmoke = document.getString("userSmoke")
                    val userDrink = document.getString("userDrink")
                    if (userSex == null || userSmoke == null || userDrink == null
                        || userBirth == null || userHeight == null || userWeight == null) {
                        Log.d("checkData", "?????? ?????? ????????? ????????? ????????????")
                        navController.navigate(R.id.action_homeFragment_to_registerSuccessFragment)
                    } else {
                        Log.d("checkData", "document??? null??? ????????? ?????? ?????? ????????? ????????????")
                        if (!isAutoLogined()) {
                            Log.d("checkData", "isAutoLogined false ??????")
                            navController.navigate(R.id.action_homeFragment_to_loginStartFragment)
                        }
                    }
                } else {
                    Log.d("checkData", "document??? null??????")
                    navController.navigate(R.id.action_homeFragment_to_loginStartFragment)
                }
                hideLoading()
            }
        Log.d("checkData", "checkData ??????")
    }

    private fun showLoading() {
        // ?????? ????????? ?????????
        Log.d("showLoading", "?????? ?????? ?????????")
        _binding?.loadingId?.loadingScreen?.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        // ?????? ????????? ??????
        Log.d("hideLoading", "?????? ?????? ??????")
        _binding?.loadingId?.loadingScreen?.visibility = View.GONE
    }

    private fun sendMessage(message: String) {
        toast.makeToast(requireContext(), "send message")
        val nodeId: String = messageEvent?.sourceNodeId!!
        // Set the data of the message to be the bytes of the Uri.
        val payload: ByteArray =
            message.toByteArray()
        // Send the rpc
        // Instantiates clients without member variables, as clients are inexpensive to
        // create. (They are cached and shared between GoogleApi instances.)
        val sendMessageTask = Wearable.getMessageClient(activityContext!!)
            .sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)
        sendMessageTask.addOnCompleteListener {
            toast.makeToast(requireContext(), "addOnCompleteListener")
            if (it.isSuccessful) {
                toast.makeToast(requireContext(), "isSuccessful")
            } else {
                toast.makeToast(requireContext(), "fail")
            }
        }
    }

    //wearOS??? ?????????????????? ??????
    @SuppressLint("SetTextI18n")
    private fun initialiseDevicePairing(tempAct: Activity) {
        var cardColor = ContextCompat.getColor(requireContext(), R.color.line_primary)

        //Coroutine
        launch(Dispatchers.Default) {
            var getNodesResBool: BooleanArray? = null

            try {
                Log.d("try", "try get nodes res bool")
                getNodesResBool =
                    getNodes(tempAct.applicationContext)
            } catch (e: Exception) {
                Log.d("try", "try exception")
                e.printStackTrace()
            }

            //UI Thread
            withContext(Dispatchers.Main) {
                getNodesResBool?.get(1)?.let { Log.d("getnodesresbool : ", it.toString()) }
                if (getNodesResBool!![0]) {
                    //if message Acknowlegement Received
                    if (getNodesResBool[1]) {
                        //????????? ??????, ?????? ????????????
                        wearableDeviceConnected = true
                        DevicePairing.wearableDeviceConnected = true
                        cardColor = ContextCompat.getColor(requireContext(), R.color.primary)
                        binding.startBtn.setCardBackgroundColor(cardColor)
                        binding.explanationTv.setText("?????? ?????? ????????? ???????????? ??????\n????????? ?????? ???????????? ????????????")

                    } else {
                        //????????? ??????, ?????? ????????????
                        wearableDeviceConnected = false
                        DevicePairing.wearableDeviceConnected = false
                        //binding.sendmessageButton.visibility = View.GONE
                        cardColor = ContextCompat.getColor(requireContext(), R.color.purple_700)
                        binding.startBtn.setCardBackgroundColor(cardColor)
                        binding.explanationTv.setText("?????? ?????? ????????? ???????????? ??????\n?????? ?????? ??????????????? ????????????")

                    }
                } else {
                    //????????? ???????????? ??????
                    wearableDeviceConnected = false
                    DevicePairing.wearableDeviceConnected = false
                    //binding.sendmessageButton.visibility = View.GONE
                    cardColor = ContextCompat.getColor(requireContext(), R.color.grey)
                    binding.startBtn.setCardBackgroundColor(cardColor)
                    binding.explanationTv.setText("?????? ?????? ????????? ????????? ??????\n????????? ????????? ??????????????? ????????????")
                }
            }
        }
    }

    //?????? ??????????????? ????????? ????????? node??? ???????????? ??????
    private fun getNodes(context: Context): BooleanArray {
        val nodeResults = HashSet<String>()
        val resBool = BooleanArray(2)
        resBool[0] = false //nodePresent : true?????? ??????????????????
        resBool[1] = false //wearableReturnAckR eceived : true?????? ????????? ?????? ????????????
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
                    DevicePairing.nodeId = nodeId
                    //DevicePairing.wearableDeviceConnected=wearableDeviceConnected
                    // Send the rpc
                    // Instantiates clients without member variables, as clients are inexpensive to
                    // create. (They are cached and shared between GoogleApi instances.)
                    val sendMessageTask =
                        Wearable.getMessageClient(context)
                            .sendMessage(nodeId, APP_OPEN_WEARABLE_PAYLOAD_PATH, payload)
                    Log.d("sendMessageToWearable", "send message")
                    //????????? ???????????? ????????? 5????????? ????????? ?????? ???????????? ???????????? ?????? ??????
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
                //getNodes()?????? ???????????? ??????????????? ???????????? ?????? ?????? ???????????? ?????? ?????????
                currentAckFromWearForAppOpenCheck = s
                Log.d(
                    TAG_MESSAGE_RECEIVED,
                    "Received acknowledgement message that app is open in wear"
                )

                messageEvent = p0
                wearableNodeUri = p0.sourceNodeId
            } else if (messageEventPath.isNotEmpty() && messageEventPath == MESSAGE_ITEM_RECEIVED_PATH) {
                //???????????? ?????? ???????????? ?????????
                try {
                    val dateAndTime: LocalDateTime = LocalDateTime.now()
                    val sbTemp = StringBuilder()
                    sbTemp.append("\n")
                    sbTemp.append(s)//?????????
                    //sbTemp.append( "==="+dateAndTime.toString())
                    //Log.d("receive1", " $sbTemp")
                    newRate = s
//                    binding.scrollviewText.requestFocus()
//                    binding.scrollviewText.post {
//                        binding.scrollviewText.scrollTo(0, binding.scrollviewText.bottom)
//                    }
                    Log.d("onMessageReceived_mainActivity", "feedMultiple()")
                    //chart??? ??????
                    //feedMultiple()
                    //DB??? ????????? ??????
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        onBackPressedCallback.remove()
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