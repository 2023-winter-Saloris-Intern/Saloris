/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.saloris

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseUpdate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.wear.ambient.AmbientModeSupport
import com.example.saloris.databinding.FragmentExerciseBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.wearable.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


/**
 * Fragment showing the exercise controls and current exercise metrics.
 */
@AndroidEntryPoint
class ExerciseFragment : Fragment(), AmbientModeSupport.AmbientCallbackProvider, CoroutineScope by MainScope(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    @Inject
    lateinit var healthServicesManager: HealthServicesManager

    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!

    private var serviceConnection = ExerciseServiceConnection()

    private var cachedExerciseState = ExerciseState.ENDED
    private var activeDurationCheckpoint =
        ExerciseUpdate.ActiveDurationCheckpoint(Instant.now(), Duration.ZERO)

    private var uiBindingJob: Job? = null

    private var lastTime = ""
    private lateinit var ambientController: AmbientModeSupport.AmbientController
    private lateinit var ambientModeHandler: AmbientModeHandler
    private lateinit var serviceIt : Intent
    //chart
    private var chart: LineChart? = null
    private var thread: Thread? = null
    //heart rate
    private var newRate = ""

    //min, mean, max
    private var min_heart = 150
    private var mean_heart = 0
    private var max_heart = 0
    private var count=0
    private var sum = 0

    //runOnUiThread를 fragment에서 사용하기 위해
    //Context를 할당할 변수를 프로퍼티로 선언(어디서든 사용할 수 있게)
    private lateinit var mainActivity: MainActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //message to mobile
        serviceIt = Intent(activity, NetWorking::class.java)
        activity?.startService(serviceIt)

        //chart
        chart = binding.chart
        chart!!.xAxis.position = XAxis.XAxisPosition.BOTTOM // x축 밑으로
        chart!!.xAxis.setDrawLabels(true)
        chart!!.xAxis.axisMinimum=0f // 9시부터
        chart!!.xAxis.axisMaximum=1200f // 오전 5시까지
        chart!!.axisRight.isEnabled = false // 오른쪽 y축은 없애고
        chart!!.axisLeft.isEnabled=false
        chart!!.xAxis.isEnabled=false
        chart!!.axisLeft.axisMaximum=100f // y축 min,max
        chart!!.axisLeft.axisMinimum=50f
        chart!!.legend.textColor = Color.BLUE
        chart!!.animateXY(10, 10)
        chart!!.invalidate()
        val data = LineData()
        chart!!.data = data

        activity?.let { register(it) }
        binding.startEndButton.setOnClickListener {
            // App could take a perceptible amount of time to transition between states; put button into
            // an intermediary "disabled" state to provide UI feedback.
            it.isEnabled = false
            startEndExercise()
        }
        binding.pauseResumeButton.setOnClickListener {
            // App could take a perceptible amount of time to transition between states; put button into
            // an intermediary "disabled" state to provide UI feedback.
            Log.d("stop service","stop")
            activity?.stopService(serviceIt)
            it.isEnabled = false
            pauseResumeExercise()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val capabilities =
                    healthServicesManager.getExerciseCapabilities() ?: return@repeatOnLifecycle
                val supportedTypes = capabilities.supportedDataTypes

                // Set enabled state for relevant text elements.
                binding.heartRateText.isEnabled = DataType.HEART_RATE_BPM in supportedTypes
            }
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.keyPressFlow.collect {
                    healthServicesManager.markLap()
                }
            }
        }

        // Ambient Mode
        ambientModeHandler = AmbientModeHandler()
        ambientController = AmbientModeSupport.attach(requireActivity())
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ambientEventFlow.collect {
                    ambientModeHandler.onAmbientEvent(it)
                }
            }
        }

        // Bind to our service. Views will only update once we are connected to it.
        ExerciseService.bindService(requireContext().applicationContext, serviceConnection)
        bindViewsToService()
    }

    //chart
    private fun createSet(): LineDataSet {
        //chart : 설정
        val set = LineDataSet(null, "Heart Rate")
        set.fillAlpha = 110
        set.fillColor = Color.parseColor("#d7e7fa")//선 밑에 색채우기
        set.color = Color.parseColor("#0B80C9")//선 색

        set.setCircleColor(Color.parseColor("#FFA1B4DC"))
        set.valueTextColor = Color.BLUE
        set.valueTextSize=10f
        set.setDrawValues(false)
        set.lineWidth = 2f
        set.circleRadius = 6f
        set.setDrawCircleHole(false)
        set.setDrawCircles(false)
        set.valueTextSize = 9f
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.highLightColor = Color.rgb(244, 117, 117)
        return set
    }
    //chart
    private fun addEntry() {
        val data = chart!!.data
        if (data != null) {
            var set = data.getDataSetByIndex(0)
            if (set == null) {
                set = createSet()
                data.addDataSet(set)
            }
            //time :real time
            data.addEntry(Entry(set.entryCount.toFloat(),newRate.toFloat()), 0)
            data.notifyDataChanged()
            chart!!.notifyDataSetChanged()
            chart!!.setVisibleXRangeMaximum(10f) // x축을 10까지만 보여주고 그 이후부터는 이동..
            chart!!.moveViewToX(set.entryCount.toFloat()-10f) // 가장 최근 추가한 데이터로 이동
        }
    }

    private fun feedMultiple() {
        if (thread != null) thread!!.interrupt()
        val runnable = kotlinx.coroutines.Runnable { addEntry() }
        Log.d("feedMultiple_mainActivity","feedMultiple")
        thread = Thread {
            mainActivity.runOnUiThread(runnable)
        }
        thread!!.start()
    }

    private fun register(ctx: Context) {
        LocalBroadcastManager.getInstance(ctx).registerReceiver(
            testReceiver, IntentFilter("test")
        )
    }
    private val testReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val stop = intent.getStringExtra("stop")
            startEndExercise()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Unbind from the service.
        ExerciseService.unbindService(requireContext().applicationContext, serviceConnection)
        _binding = null
    }

    private fun startEndExercise() {
        if (cachedExerciseState.isEnded) {
            tryStartExercise()
        } else {
            checkNotNull(serviceConnection.exerciseService) {
                "Failed to achieve ExerciseService instance"
            }.endExercise()
        }
    }

    private fun tryStartExercise() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (healthServicesManager.isTrackingExerciseInAnotherApp()) {
                // Show the user a confirmation screen.
                //findNavController().navigate(R.id.to_newExerciseConfirmation)
            } else if (!healthServicesManager.isExerciseInProgress()) {
                checkNotNull(serviceConnection.exerciseService) {
                    "Failed to achieve ExerciseService instance"
                }.startExercise()
            }
        }
    }

    private fun pauseResumeExercise() {
        val service = checkNotNull(serviceConnection.exerciseService) {
            "Failed to achieve ExerciseService instance"
        }
        System.exit(0)
        if (cachedExerciseState.isPaused) {
            service.resumeExercise()
        } else {
            service.pauseExercise()
        }
    }

    private fun bindViewsToService() {
        if (uiBindingJob != null) return

        uiBindingJob = viewLifecycleOwner.lifecycleScope.launch {
            serviceConnection.repeatWhenConnected { service ->
                // Use separate launch blocks because each .collect executes indefinitely.
                launch {
                    service.exerciseState.collect {
                        updateExerciseStatus(it)
                    }
                }
                launch {
                    service.latestMetrics.collect {
                        it?.let { updateMetrics(it) }
                    }
                }
                launch {
                    service.activeDurationCheckpoint.collect {
                        // We don't update the chronometer here since these updates come at irregular
                        // intervals. Instead we store the duration and update the chronometer with
                        // our own regularly-timed intervals.
                        activeDurationCheckpoint = it
                    }
                }
            }
        }
    }

    private fun unbindViewsFromService() {
        uiBindingJob?.cancel()
        uiBindingJob = null
    }

    private fun updateExerciseStatus(state: ExerciseState) {
        val previousStatus = cachedExerciseState
        if (previousStatus.isEnded && !state.isEnded) {
            // We're starting a new exercise. Clear metrics from any prior exercise.
            resetDisplayedFields()
        }
//
//        if (state == ExerciseState.ACTIVE && !ambientController.isAmbient) {
//            startChronometer()
//        } else {
//            stopChronometer()
//        }

        updateButtons(state)
        cachedExerciseState = state
    }

    private fun updateButtons(state: ExerciseState) {
        binding.startEndButton.setText(if (state.isEnded) "다시시작" else "일시중지")
        binding.startEndButton.isEnabled = true
        binding.pauseResumeButton.setText(if (state.isPaused) "시작" else "종료")
        binding.pauseResumeButton.isEnabled = true
    }

    private fun updateMetrics(latestMetrics: DataPointContainer) {
        latestMetrics.getData(DataType.HEART_RATE_BPM).let{
            if (it.isNotEmpty()) {
                binding.heartRateText.text=it.last().value.roundToInt().toString()
                Log.d("Heart Rate_ExerciseFragment_updateMetrics",it.last().value.roundToInt().toString())

                val heart_rate_value =it.last().value.roundToInt().toString()

                if(heart_rate_value!="0") {
                    min_heart =min(min_heart, heart_rate_value.toInt())
                    max_heart =max(max_heart, heart_rate_value.toInt())
                    sum += heart_rate_value.toInt()
                    count += 1
                    mean_heart = sum / count
                    binding.minHeart.text= min_heart.toString()
                    binding.meanHeart.text= mean_heart.toString()
                    binding.maxHeart.text= max_heart.toString()
                }
                //TODO : send to mobile 1s
                var battery = getBatteryRemain(mainActivity)
                Log.d("Battery!!",battery.toString())
                val dateAndtime: String = LocalDateTime.now().toString().substring(14,16)
                serviceIt.putExtra("heartRate",heart_rate_value)
                serviceIt.putExtra("battery",battery.toString())
                activity?.startService(serviceIt)
                Log.d("time_ExerciseFragment_updateMetrics",dateAndtime)
                Log.d("putValue_ExerciseFragment_updateMetrics",heart_rate_value)
                lastTime=dateAndtime

                //todo chart
                newRate=heart_rate_value
                if(newRate!="0") {
                    feedMultiple()
                }
            }
        }

    }
    private fun getBatteryRemain(context: Context): Int {
        val intentBattery =
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intentBattery!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intentBattery.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = level / scale.toFloat()
        return (batteryPct * 100).toInt()
    }
    private fun resetDisplayedFields() {
        getString(R.string.empty_metric).let {
            binding.heartRateText.text = it
        }
        //binding.elapsedTime.text = formatElapsedTime(Duration.ZERO, true)
    }

    // -- Ambient Mode support

    private fun setAmbientUiState(isAmbient: Boolean) {
        // Change icons to white while in ambient mode.
        val iconTint = if (isAmbient) {
            Color.WHITE
        } else {
            resources.getColor(R.color.primary_orange, null)
        }
        ColorStateList.valueOf(iconTint).let {
            //binding.clockIcon.imageTintList = it
            binding.heartRateIcon.imageTintList = it
        }

        // Hide the buttons in ambient mode.
        val buttonVisibility = if (isAmbient) View.INVISIBLE else View.VISIBLE
        buttonVisibility.let {
            binding.startEndButton.visibility = it
            binding.pauseResumeButton.visibility = it
        }
    }

    private fun performOneTimeUiUpdate() {
        val service = checkNotNull(serviceConnection.exerciseService) {
            "Failed to achieve ExerciseService instance"
        }
        updateExerciseStatus(service.exerciseState.value)

        service.latestMetrics.value?.let { updateMetrics(it) }

        activeDurationCheckpoint = service.activeDurationCheckpoint.value
        //updateChronometer()
    }

    inner class AmbientModeHandler {
        internal fun onAmbientEvent(event: AmbientEvent) {
            when (event) {
                is AmbientEvent.Enter -> onEnterAmbient()
                is AmbientEvent.Exit -> onExitAmbient()
                is AmbientEvent.Update -> onUpdateAmbient()
            }
        }

        private fun onEnterAmbient() {
            // Note: Apps should also handle low-bit ambient and burn-in protection.
            unbindViewsFromService()
            setAmbientUiState(true)
            performOneTimeUiUpdate()
        }

        private fun onExitAmbient() {
            performOneTimeUiUpdate()
            setAmbientUiState(false)
            bindViewsToService()
        }

        private fun onUpdateAmbient() {
            performOneTimeUiUpdate()
        }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = MyAmbientCallback()

    private inner class MyAmbientCallback : AmbientModeSupport.AmbientCallback() {
        override fun onEnterAmbient(ambientDetails: Bundle) {
            super.onEnterAmbient(ambientDetails)
        }

        override fun onUpdateAmbient() {
            super.onUpdateAmbient()
        }

        override fun onExitAmbient() {
            super.onExitAmbient()
        }
    }

    override fun onDataChanged(p0: DataEventBuffer) {
        TODO("Not yet implemented")
    }

    private val TAG_MESSAGE_RECEIVED = "receive1"
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"
    private var activityContext: Context? = null
    private var messageEvent: MessageEvent? = null
    private var mobileNodeUri: String? = null
    private var mobileDeviceConnected: Boolean = false
    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"

    override fun onMessageReceived(p0: MessageEvent) {
        Log.d("message","message recieved")
        try {
            Log.d(TAG_MESSAGE_RECEIVED, "onMessageReceived event received")
            val s1 = String(p0.data, StandardCharsets.UTF_8)
            val messageEventPath: String = p0.path

            Log.d(
                TAG_MESSAGE_RECEIVED,
                "onMessageReceived() A message from watch was received:"
                        + p0.requestId
                        + " "
                        + messageEventPath
                        + " "
                        + s1
            )

            //Send back a message back to the source node
            //This acknowledges that the receiver activity is open
            if (messageEventPath.isNotEmpty() && messageEventPath == APP_OPEN_WEARABLE_PAYLOAD_PATH) {
                Log.d("message","connecting")
                try {
                    // Get the node id of the node that created the data item from the host portion of
                    // the uri.
                    val nodeId: String = p0.sourceNodeId
                    // Set the data of the message to be the bytes of the Uri.
                    val returnPayloadAck = wearableAppCheckPayloadReturnACK
                    val payload: ByteArray = returnPayloadAck.toByteArray()

                    // Send the rpc
                    // Instantiates clients without member variables, as clients are inexpensive to
                    // create. (They are cached and shared between GoogleApi instances.)
                    val sendMessageTask =
                        Wearable.getMessageClient(activityContext!!)
                            .sendMessage(nodeId, APP_OPEN_WEARABLE_PAYLOAD_PATH, payload)

                    Log.d(
                        TAG_MESSAGE_RECEIVED,
                        "Acknowledgement message successfully with payload : $returnPayloadAck"
                    )

                    messageEvent = p0
                    mobileNodeUri = p0.sourceNodeId

                    sendMessageTask.addOnCompleteListener {
                        Log.d("message","send to mobile")
                        if (it.isSuccessful) {
                            Log.d(TAG_MESSAGE_RECEIVED, "Message sent successfully")

                            val sbTemp = StringBuilder()
                            sbTemp.append("\nMobile device connected.")
                            Log.d("receive1", " $sbTemp")

                            mobileDeviceConnected = true
                        } else {
                            Log.d(TAG_MESSAGE_RECEIVED, "Message failed.")
                        }
                    }
                } catch (e: Exception) {
                    Log.d(
                        TAG_MESSAGE_RECEIVED,
                        "Handled in sending message back to the sending node"
                    )
                    e.printStackTrace()
                }
            }//emd of if
            else if (messageEventPath.isNotEmpty() && messageEventPath == MESSAGE_ITEM_RECEIVED_PATH) {
                try {

                    val sbTemp = StringBuilder()
                    sbTemp.append("\n")
                    sbTemp.append(s1)
                    sbTemp.append(" - (Received from mobile)")
                    Log.d("receive1", " $sbTemp")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.d(TAG_MESSAGE_RECEIVED, "Handled in onMessageReceived")
            e.printStackTrace()
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

