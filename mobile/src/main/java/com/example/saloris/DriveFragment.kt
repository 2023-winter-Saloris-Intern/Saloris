package com.example.saloris

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import com.example.saloris.LocalDB.AppDatabase
import com.example.saloris.LocalDB.HeartRate
import com.example.saloris.databinding.FragmentDriveBinding
import com.example.saloris.facemesh.FaceMeshResultGlRenderer
import com.example.saloris.util.*
import com.example.salorisv.DevicePairing
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FcmBroadcastProcessor.reset
import com.google.mediapipe.solutioncore.CameraInput
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView
import com.google.mediapipe.solutions.facemesh.FaceMesh
import com.google.mediapipe.solutions.facemesh.FaceMeshOptions
import com.google.mediapipe.solutions.facemesh.FaceMeshResult
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.influxdb.client.write.Point
import com.influxdb.exceptions.InfluxException
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.timer
import kotlin.math.pow
import kotlin.math.sqrt


@Suppress("DEPRECATION")
class DriveFragment : Fragment(), CoroutineScope by MainScope(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {
    // 1. Context를 할당할 변수를 프로퍼티로 선언(어디서든 사용할 수 있게)
    lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
    }

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

    private val BatteryLow = 35
    /* User Authentication */
    private lateinit var auth: FirebaseAuth

    //private val socketCoroutine = SocketCoroutine()
    private var socketAsyncTask: SocketAsyncTask? = null

    private val cameraPermissionList = arrayOf(
        Manifest.permission.CAMERA
    )
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    var btnBackPressedTime: Long = 0

    var newRate = "0"
    var Rate = ""
    var last_time = ""
    var last_battery=true
    /* Toast */
    private val toast = MakeToast()

    /* FaceMesh */
    private var isCameraOn = false

    private var isFaceOn = false
    private var isFaceGood = false
    private var isFaceValid = false

    private lateinit var prefs: SharedPreferences
    private lateinit var faceMeshSettings: BooleanArray
    private lateinit var faceMeshColors: ArrayList<FloatArray>
    private var alarmState: Boolean = false
    private var timer2 = Timer(true)

    private lateinit var faceMesh: FaceMesh
    private lateinit var guidelineAnimation: AnimationDrawable

    private lateinit var cameraInput: CameraInput
    private lateinit var glSurfaceView: SolutionGlSurfaceView<FaceMeshResult>

    private var count: Int = 0          // 총 블링크 카운트
    private var blink: Int = 0          // 현재 눈 감은상태
    private var totalBlink: Int = 0     // 누적 눈 깜빡임
    private var longClosedCount: Int = 0// 3초 이상 눈 감은 카운트
    private var longClosedEye: Int = 0  // 3초 이상 눈 감은 누적 횟수
    private var longClosedState: Boolean = false // 3초 이상 눈 감은 상태

    private var afterState: Boolean = false // 두 번째 3초 이상 눈감은 상태 확인
    private var face: String = ""       // 현재 얼굴방향
    private var leftEye: String = ""    // 왼쪽 눈 방향
    private var rightEye: String = ""   // 오른쪽 눈 방향
    private var ear: Float = 0.0f
    private var mar: Float = 0.0f
    private var moe: Float = 0.0f

    private fun colorLoad(value: Int): FloatArray {
        return when (value) {
            1 -> WHITE_COLOR
            2 -> ORANGE_COLOR
            3 -> BLUE_COLOR
            4 -> RED_COLOR
            5 -> GREEN_COLOR

            else -> BLACK_COLOR
        }
    }

    private fun initFaceMesh() {
        // Initializes a new MediaPipe Face Mesh solution instance in the streaming mode.
        // refineLandmark - 눈, 입술 주변으로 분석 추가.
        faceMesh = FaceMesh(
            requireContext(),
            FaceMeshOptions.builder()
                .setStaticImageMode(false)
                .setRefineLandmarks(true)
                .setRunOnGpu(true)
                .build()
        )
        faceMesh.setErrorListener { message: String, _: RuntimeException? ->
            Log.e("State", "MediaPipe Face Mesh error:$message")
            toast.makeToast(requireContext(), "인식이 되지 않습니다.")
        }
    }

    private var fittingLevel = 0
    private var timerCheck = true

    private var sum1minHeartRate = 0
    private var count1minHeartRate = 0

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initGlSurfaceView() {
        // Initializes a new Gl surface view with a user-defined FaceMeshResultGlRenderer.
        glSurfaceView =
            SolutionGlSurfaceView(activityContext, faceMesh.glContext, faceMesh.glMajorVersion)
        glSurfaceView.setSolutionResultRenderer(
            FaceMeshResultGlRenderer(faceMeshSettings, faceMeshColors)
        )
        faceMesh.setResultListener { faceMeshResult: FaceMeshResult? ->
            checkLandmark(faceMeshResult)

            lifecycleScope.launch(Dispatchers.Main) {
                if (isFaceValid) {
//                    with(binding.guideline) {
//                        if (mainActivity::guidelineAnimation.isInitialized)
//                            guidelineAnimation.stop()
//                        setBackgroundResource(R.drawable.face_guideline_complete)
//                    }
//                    binding.faceFittingWarningText.text = getString(R.string.face_fitting_complete)
                    // 인식 완료 1초 후 화면 변경
                    if (timerCheck) {
                        timer2 = timer(period = 100) {
                            timerCheck = false
                            fittingLevel += 1
                            Log.d("fittingLevel", "$fittingLevel")
                            if (fittingLevel >= MAX_FITTING_LEVEL) {
                                lifecycleScope.launch(Dispatchers.Main) {
//                                    binding.stateFitting.visibility = View.INVISIBLE
                                    binding.stateFitting2.visibility = View.VISIBLE
//                                    binding.faceFittingWarningText.visibility = View.INVISIBLE
//                                    if (isBluetoothOn) {
//                                        binding.state.visibility = View.VISIBLE
//                                    }
                                }
                                cancel()
                            }
                        }
                    }
                } else {
                    reset()
                    binding.faceFitting.visibility = View.VISIBLE
//                    binding.state.visibility = View.GONE
//                    if (isBluetoothOn) {
//                        binding.stateFitting.visibility = View.VISIBLE
//                    }
//                    if (!isFaceOn) {
//                        binding.faceFittingWarningText.text = getString(R.string.face_fitting_init)
//                    }
//                    if (!isFaceGood) {
//                        binding.faceFittingWarningText.text = getString(R.string.face_fitting_warn)
//                    }
                }
            }

            glSurfaceView.setRenderData(faceMeshResult, true)
            glSurfaceView.requestRender()
        }
    }

    private fun postGlSurfaceView() {
        cameraInput = CameraInput(mainActivity)
        cameraInput.setNewFrameListener { faceMesh.send(it) }

        glSurfaceView.post { startCamera() }
        glSurfaceView.visibility = View.VISIBLE
    }

    private fun startCamera() {
        cameraInput.start(mainActivity,
            faceMesh.glContext,
            CameraInput.CameraFacing.FRONT,
            480,
            640)
    }

    /* date 시간 구하기 */
    private var startTime: Long = 0
    private var beforeTime: Long = 0
    private var startCheck: Boolean = true
    private var beforeCheck: Boolean = true

    private fun getTime(): Long {
        var now = System.currentTimeMillis()
        //var date = Date(now)

        //var dateFormat = SimpleDateFormat("yyyy-MM-dd")
        //var getTime = dateFormat.format(date)

        return now
    }

    //시간 차 구하기
    private fun betweenTime(before: Long): Long {
        //var nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(getTime())
        //var beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(before)
        var now = System.currentTimeMillis()
        var diffSec = (now - before) / 1000

        return diffSec
    }

    // landmark 분석
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkLandmark(result: FaceMeshResult?) {
        if (result == null || result.multiFaceLandmarks().isEmpty()) {
            isFaceOn = false
            isFaceGood = true
            isFaceValid = false

            return
        }

        with(result.multiFaceLandmarks()[0].landmarkList) {
            val mouth = MOUTH_INDEX.map { get(it) }
            val lEye = LEFT_EYE_INDEX.map { get(it) }
            val rEye = RIGHT_EYE_INDEX.map { get(it) }
            val base = BASE_INDEX.map { get(it) }
            val pitching = FACE_PITCHING.map { get(it) }

            // 얼굴이 가이드라인 안에 있는지 0.10 0.95 0.26 0.86
            isFaceOn =
                !(pitching[0].y < 0.08 || pitching[1].y > 0.97 || pitching[2].x < 0.24 || pitching[3].x > 0.88)

            val nose = result.multiFaceLandmarks()[0].getLandmark(4).z
            isFaceGood = -0.14 < nose && nose < -0.04

            isFaceValid = isFaceOn && isFaceGood
            if (!isFaceValid) return

            val leftEAR = ((distance(lEye[1].x, lEye[5].x, lEye[1].y, lEye[5].y))
                    + (distance(lEye[2].x, lEye[4].x, lEye[2].y, lEye[4].y))) /
                    (2 * (distance(lEye[0].x, lEye[3].x, lEye[0].y, lEye[3].y)))
            val rightEAR = ((distance(rEye[1].x, rEye[5].x, rEye[1].y, rEye[5].y))
                    + (distance(rEye[2].x, rEye[4].x, rEye[2].y, rEye[4].y))) /
                    (2 * (distance(rEye[0].x, rEye[3].x, rEye[0].y, rEye[3].y)))

            ear = (leftEAR + rightEAR) / 2
            mar = ((distance(mouth[1].x, mouth[7].x, mouth[1].y, mouth[7].y))
                    + (distance(mouth[2].x, mouth[6].x, mouth[2].y, mouth[6].y))
                    + (distance(mouth[3].x, mouth[5].x, mouth[3].y, mouth[5].y))) /
                    (2 * (distance(mouth[0].x, mouth[4].x, mouth[0].y, mouth[4].y)))
            moe = mar / ear

            // 좌, 우 눈 길이
            val leftEyeDistance = distance(lEye[6].x, base[1].x, lEye[6].y, base[1].y).pow(2)
            val rightEyeDistance = distance(rEye[6].x, base[1].x, rEye[6].y, base[1].y).pow(2)

            // 얼굴 방향 측정
            face = faceDirection(mouth[8].z, mouth[9].z, lEye[0].z, rEye[3].z, base[0].z, base[2].z)

            // 한 눈을 감았을 떄 EAR 평균 0.14
            if (ear < 0.09) {
                if (!longClosedState) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Log.d("blink", "blink")
                        with(binding.blink) {
                            text = getString(R.string.blink)
                            setTextColor(ContextCompat.getColor(activityContext!!, R.color.drowsy))
                            visibility = View.INVISIBLE
                        }
                    }
                }
                count++
                blink = 0
                leftEye = getString(R.string.blink)
                rightEye = getString(R.string.blink)
                if (beforeCheck) {
                    beforeTime = getTime()
                    beforeCheck = false
                }
                if (startCheck) {
                    startTime = getTime()
                    startCheck = false
                }
                if (longClosedCount == 1 && (betweenTime(startTime) % 1) == 0L) {
                    lifecycleScope.launch(Dispatchers.Main) {
//                        with(binding.timerFitting) {
//                            text = (15 - betweenTime(startTime)).toString()
//                            setTextColor(ContextCompat.getColor(activityContext!!, R.color.white))
//                            visibility = View.VISIBLE
//                        }
//                        with(binding.longClosedFitting) {
//                            text = longClosedCount.toString()
//                            setTextColor(ContextCompat.getColor(activityContext!!, R.color.white))
//                            visibility = View.VISIBLE
//                        }
                    }
                    if (betweenTime(startTime) <= 0) {
                        lifecycleScope.launch(Dispatchers.Main) {
//                            with(binding.timerFitting) {
//                                text = 0.toString()
//                                setTextColor(ContextCompat.getColor(activityContext!!,
//                                    R.color.white))
//                                visibility = View.VISIBLE
//                            }
//                            with(binding.longClosedFitting) {
//                                text = 0.toString()
//                                setTextColor(ContextCompat.getColor(activityContext!!,
//                                    R.color.white))
//                                visibility = View.VISIBLE
//                            }
                        }
                    }
                }
                if (!longClosedState) {
                    if ((betweenTime(beforeTime) % 1) == 0L && betweenTime(beforeTime) != 0L) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            with(binding.blink) {
                                text = betweenTime(beforeTime).toString()
                                setTextColor(ContextCompat.getColor(activityContext!!,
                                    R.color.drowsy))
                                visibility = View.INVISIBLE
                            }
//                            with(binding.longClosedFitting) {
//                                text = longClosedCount.toString()
//                                setTextColor(ContextCompat.getColor(activityContext!!,
//                                    R.color.white))
//                                visibility = View.VISIBLE
//                            }
                        }
                    }
                }
                if (betweenTime(beforeTime) >= 2) {
                    playSoundAndVibration(requireContext())
                    longClosedCount++
                    longClosedEye++
                    longClosedState = true
                    beforeCheck = true
                    lifecycleScope.launch(Dispatchers.Main) {
                        with(binding.blink) {
                            text = getString(R.string.long_closed_eye)
                            setTextColor(ContextCompat.getColor(activityContext!!, R.color.red))
                            visibility = View.INVISIBLE
                        }
                    }
                    if (longClosedCount == 0) {
                        startTime = getTime()
                    }
                    if (betweenTime(startTime) > 15) {
                        longClosedCount = 1
                        afterState = false
                        startTime = getTime()
                    } else if (longClosedCount >= 2 && betweenTime(startTime) <= 15) {
                        if (alarmState) {
                            startWarningOn()
                        } else {
                            startWarningOff()
                        }
                        longClosedCount = 1
                        afterState = false
                        startTime = getTime()
                    }
                }
            } else {
                if (count > 2 || ear > 0.09) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        with(binding.blink) {
                            text = getString(R.string.blink)
                            setTextColor(ContextCompat.getColor(activityContext!!, R.color.drowsy))
                            visibility = View.GONE
                        }
                    }
                    if (longClosedCount == 1 && (betweenTime(startTime) % 1) == 0L) {
                        lifecycleScope.launch(Dispatchers.Main) {
//                            with(binding.timerFitting) {
//                                text = (15 - betweenTime(startTime)).toString()
//                                setTextColor(ContextCompat.getColor(activityContext!!,
//                                    R.color.white))
//                                visibility = View.VISIBLE
//                            }
//                            with(binding.longClosedFitting) {
//                                text = longClosedCount.toString()
//                                setTextColor(ContextCompat.getColor(activityContext!!,
//                                    R.color.white))
//                                visibility = View.VISIBLE
//                            }
                        }
                    }
                    count = 0
                    blink = 1
                    totalBlink++
                    beforeCheck = true
                    startCheck = false
                    if (longClosedCount == 0 || afterState == true) {
                        startCheck = true
                    }
                    longClosedState = false
                    stopWarning()
                }
                if (leftEAR < 0.22) {
                    leftEye = getString(R.string.blink)
                    rightEye = eyeDirection(leftEyeDistance, rightEyeDistance)
                } else if (rightEAR < 0.22) {
                    leftEye = eyeDirection(leftEyeDistance, rightEyeDistance)
                    rightEye = getString(R.string.blink)
                } else {
                    leftEye = eyeDirection(leftEyeDistance, rightEyeDistance)
                    rightEye = eyeDirection(leftEyeDistance, rightEyeDistance)
                }
            }
        }
    }

    private fun distance(rx: Float, lx: Float, ry: Float, ly: Float): Float {
        return sqrt((rx - lx).pow(2) + (ry - ly).pow(2))
    }

    private fun eyeDirection(ld: Float, rd: Float): String {
        return if ((ld - rd) > 0.004)
            getString(R.string.left)
        else if ((ld - rd) < -0.0035)
            getString(R.string.right)
        else
            getString(R.string.front)
    }

    private fun faceDirection(
        lez: Float, rez: Float, lmz: Float, rmz: Float, hp: Float, cp: Float,
    ): String {
        val fdRatio = (lez + lmz) - (rez + rmz)

        return if (hp - cp < -0.05)
            getString(R.string.down)
        else {
            if (fdRatio > 0.15)
                getString(R.string.left)
            else if (fdRatio < -0.15)
                getString(R.string.right)
            else
                getString(R.string.front)
        }
    }

    /* Warn */
    private val toneGenerator1 = ToneGenerator(AudioManager.STREAM_MUSIC, 200)
    private val toneGenerator2 = ToneGenerator(AudioManager.STREAM_MUSIC, 500)

    private var warningLevel = 0
    private var standard = 100

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startWarningOn() {
        toneGenerator1.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 500)

        lifecycleScope.launch(Dispatchers.Main) {
            with(binding.warningFilter) {
                visibility = View.VISIBLE
                (drawable as AnimationDrawable).start()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startWarningOff() {
        //toneGenerator1.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 500)

        lifecycleScope.launch(Dispatchers.Main) {
            with(binding.warningFilter) {
                visibility = View.VISIBLE
                (drawable as AnimationDrawable).start()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun continueWarning() {
        toneGenerator2.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 500)

        lifecycleScope.launch(Dispatchers.Main) {
            with(binding.warningFilter) {
                visibility = View.VISIBLE
                (drawable as AnimationDrawable).start()
            }
        }
    }

    private fun stopWarning() {
        toneGenerator1.stopTone()

        lifecycleScope.launch(Dispatchers.Main) {
            with(binding.warningFilter) {
                visibility = View.GONE
                (drawable as AnimationDrawable).stop()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val deniedList = result.filter { !it.value }.map { it.key }
            Log.d("State", "$deniedList")
            if (deniedList.isNotEmpty()) {
                if (deniedList.any { it == Manifest.permission.CAMERA }) {
                    println("any1: $isCameraOn")
                    isCameraOn = false
                    println("any2: $isCameraOn")
                }
//                AlertDialog.Builder(mainActivity)
//                    .setTitle("알림")
//                    .setMessage("권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
//                    .setPositiveButton("설정") { _, _ -> openAndroidSetting() }
//                    .setNegativeButton("취소", null)
//                    .create()
//                    .show()
            } else {
                isCameraOn = true
            }
        }

    private fun playSoundAndVibration(context: Context) {
        // 소리 재생
//        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
//        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0)
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        //소리를 울리기 위해 Ringtone 객체 참조하기
        val ringtone =
            RingtoneManager.getRingtone(context, uri)
        ringtone.play()

        // 진동
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
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
        /* Bottom Menu */
        val bottomMenu = mainActivity.findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        //user auth
        auth = Firebase.auth
        //chart
        // 화면 항상 켜짐
        /* Permission - Camera */
        if (ActivityCompat.checkSelfPermission(
                mainActivity, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(cameraPermissionList)
        }

        /* FaceMesh */
        prefs = activityContext!!.getSharedPreferences("faceSetting", Context.MODE_PRIVATE)
        faceMeshSettings = booleanArrayOf(
            prefs.getBoolean("eye", true),
            prefs.getBoolean("eyeBrow", true),
            prefs.getBoolean("eyePupil", true),
            prefs.getBoolean("lib", true),
            prefs.getBoolean("faceMesh", false),
            prefs.getBoolean("faceLine", true)
        )
        faceMeshColors = arrayListOf(
            colorLoad(prefs.getInt("eyeColor", 5)),
            colorLoad(prefs.getInt("eyeBrowColor", 4)),
            colorLoad(prefs.getInt("eyePupilColor", 1)),
            colorLoad(prefs.getInt("libColor", 3)),
            colorLoad(prefs.getInt("faceMeshColor", 1)),
            colorLoad(prefs.getInt("faceLineColor", 1))
        )
        prefs = activityContext!!.getSharedPreferences("alarm", Context.MODE_PRIVATE)
        alarmState = prefs.getBoolean("alarmState", false)

//        lifecycleScope.launch(Dispatchers.IO) {
//            while (true) {
//                saveAndWarn()
//                delay(1000)
//            }
//        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDriveBinding.inflate(layoutInflater, container, false)
//        lifecycleScope.launch {
//            delay(2000) // 2초 대기
//            socketCoroutine.connect("192.168.0.103", 9999)
//            socketCoroutine.sendData(newRate)
//        } driveFragment가 실행되면 바로 데이터를 받는다.
        lifecycleScope.launch {
            delay(2000) // 2초 대기
            SocketAsyncTask().execute()
        }
        initFaceMesh()
        initGlSurfaceView()
        postGlSurfaceView()
        with(binding.preview) {
            removeAllViewsInLayout()
            addView(glSurfaceView)
            requestLayout()
        }
//        if(getArguments()!=null) {
//            Log.d("from home ", getArguments()?.getString("data").toString())
//        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        wearableDeviceConnected = false
        navController = Navigation.findNavController(view)
        activityContext = this.context

        binding.checkConnect.setOnClickListener {
//            lifecycleScope.launch {
//                socketCoroutine.connect("192.168.0.103", 9999)
//                socketCoroutine.sendData(newRate)
//            }
            //만약 같은 데이터를 반복해서 받는다면 위에 lifecycleScope 함수를 주석 처리 후 밑에 있는 코드로 실행
//            SocketAsyncTask().execute()

            //워치 진동 버튼 => 누르면 워치에서 진동 발생
            if (!wearableDeviceConnected) {
                val tempAct: Activity = activityContext as AppCompatActivity
                //Couroutine
                initialiseDevicePairing(tempAct)
            }
            //binding.heartRate.text = "-"
            sendMessage("vibrator")
        }
        binding.finishDriveBtn.setOnClickListener {
            binding.driveState.setText("운행 종료")
            SocketAsyncTask().cancel(true)
            navController.navigate(R.id.action_driveFragment_to_homeFragment)
        }

    }

//    class SocketCoroutine {
//        private var socket: Socket? = null
//        private var socketThread: Thread? = null
//        private var dataOutputStream: DataOutputStream? = null
//        private var dataInputStream: DataInputStream? = null
//
//        suspend fun connect(ip: String, port: Int) = withContext(Dispatchers.IO) {
//            try {
//                socket = Socket(ip, port)
//                val output = socket?.getOutputStream()
//                dataOutputStream = DataOutputStream(output)
//                val input = socket?.getInputStream()
//                dataInputStream = DataInputStream(input)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//        suspend fun sendData(newRate: String) = withContext(Dispatchers.IO) {
//            try {
//                while (isActive) {
//                    // DataOutputStream 객체를 이용하여 데이터를 서버로 전송합니다.
//                    val newRateInt = ByteBuffer.allocate(4)
//                        .putInt(newRate.toInt())
//                        .order(ByteOrder.BIG_ENDIAN)
//                        .array()
//                    Log.d("SocketCoroutine", "$newRateInt : newRateInt!!!@!!@!@!@!@!@!@!")
//
//                    dataOutputStream?.write(newRateInt)
//                    delay(1000) // 1초 대기
//                }
//
//                val response = dataInputStream?.readInt()
//                Log.d("SocketCoroutine", "$response : 리스폰리스폰!!!@!!@!@!@!@!@!@!")
//                Log.d("SocketCoroutine", "${newRate.toInt()} : 뉴레이트!!!@!!@!@!@!@!@!@!")
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//        fun disconnect() {
//            try {
//                dataOutputStream?.close()
//                //socket?.close()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
    inner class SocketAsyncTask : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String {
            try {
                Log.d("SocketAsyncTask().execute()", "소켓소켓소켓소켓소켓소켓소켓소켓소켓소켓")
                val socket = Socket("192.168.0.103", 9999)


                //Socket을 통해 데이터를 얻어오기 위한 코드
                val input = socket.getInputStream()
                val dataInputStream = DataInputStream(input)
                //데이터를 보내기 위한 코드
                val output = socket.getOutputStream()// Socket의 출력 스트림을 가져옵니다.
                val dataOutputStream = DataOutputStream(output)// 데이터를 출력하기 위한 DataOutputStream 객체를 생성합니다.

                while (true) {
                    // DataOutputStream 객체를 이용하여 데이터를 서버로 전송합니다.
                    val newRateInt = ByteBuffer.allocate(4).putInt(newRate.toInt()).order(ByteOrder.BIG_ENDIAN).array()
                    Log.d("SocketAsyncTask", "$newRateInt : newRateInt!!!@!!@!@!@!@!@!@!")

                    dataOutputStream.write(newRateInt)
                    Thread.sleep(1000) // 1초 대기
                }


                val response = dataInputStream.readInt()
                Log.d("SocketAsyncTask", "$response : 리스폰리스폰!!!@!!@!@!@!@!@!@!")

                Log.d("SocketAsyncTask", "${newRate.toInt()} : 뉴레이트!!!@!!@!@!@!@!@!@!")
                // 출력 스트림 닫기
                dataOutputStream.close()
                // 소켓 닫기
                socket.close()

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            return ""
        }
        override fun onCancelled() {
            super.onCancelled()
            Log.d("SocketAsyncTask", "Task cancelled!")
        }
    }
    override fun onDataChanged(p0: DataEventBuffer) {
        TODO("Not yet implemented")
    }

    private fun sendMessage(message: String) {
        //toast.makeToast(requireContext(), "send message")
        println(wearableDeviceConnected)
        val wearableDeviceConnected1 = DevicePairing.getwearableDeviceConnected()
        Log.d("wearableDeviceConneced from DevicePairing",wearableDeviceConnected1.toString())
        if (wearableDeviceConnected!!) {
            if (binding.heartRate.text!!.isNotEmpty()) {
                //toast.makeToast(requireContext(), "send message")
                val nodeId: String = messageEvent?.sourceNodeId!!
                val nodeId1 = DevicePairing.getNodeId()
                Log.d("nodeid from DevicePairing",nodeId1.toString())
                // Set the data of the message to be the bytes of the Uri.
                val payload: ByteArray =
                    message.toByteArray()

                // Send the rpc
                // Instantiates clients without member variables, as clients are inexpensive to
                // create. (They are cached and shared between GoogleApi instances.)
                val sendMessageTask =
                    nodeId?.let {
                        Wearable.getMessageClient(activityContext!!)
                            .sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)
                    }

                if (sendMessageTask != null) {
                    sendMessageTask.addOnCompleteListener {
                        //toast.makeToast(requireContext(), "addOnCompleteListener")
                        if (it.isSuccessful) {
                            //toast.makeToast(requireContext(), "연결 성공")


                        } else {
                            //toast.makeToast(requireContext(), "다시 연결")
                        }
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

    //DB에 heartrate를 넣는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun insertDB(rate: Int): Boolean {
//val token = System.getenv()["INFLUX_TOKEN"]
        val user = "user"
        //사용자 uid
        val Uid = auth.currentUser?.uid
        //val Uid = "T"
        val org = "intern"
        val bucket = "HeartRate"
        //influxDB token
        val token =
            "yZmCmFFTYYoetepTiOpXDRK8oyL1f_orD6oZH8SXsvlf213z-_iRmXtaf-AjyLe2HS-NhfxcNeY-0K6qR0k6Sw=="
        print(System.getenv())
        val client =
            InfluxDBClientKotlinFactory.create("https://europe-west1-1.gcp.cloud2.influxdata.com",
                token!!.toCharArray(),
                org,
                bucket)
        client.use {
            val writeApi = client.getWriteKotlinApi()
            try {
                //todo : issleep?
                var issleep = false
                if (rate < 65) {
                    issleep = true
                }
                val map1 = mutableMapOf<String, Any>("heart" to rate, "isntsleep" to issleep)
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
                //binding.heartRate.text="-"
                if (getNodesResBool!![0]) {
                    //if message Acknowlegement Received
                    if (getNodesResBool[1]) {
                        wearableDeviceConnected = true
                        Log.d("heartrate text","wearable true")
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
        resBool[1] = false //wearableReturnAck Received : true이면 워치에 앱이 열려있다
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
                    Log.d("sendMessageToWearable", "send message")
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
//            val s =
//                String(p0.data, StandardCharsets.UTF_8)
            val rateAndBattery =String(p0.data, StandardCharsets.UTF_8).split("/")
            val s = rateAndBattery[0]
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
                    val battery = rateAndBattery[1]
                    //Log.d("battery",battery)
                    if(battery.toInt()>BatteryLow){
                        last_battery=true
                    }
                    if(battery.toInt()<BatteryLow && last_battery){
                        Log.d("battery","low!!!!!!")
                        sendMessage("vibrator")
                        last_battery=false
                    }
                    val dateAndTime: LocalDateTime = LocalDateTime.now()
                    val sbTemp = StringBuilder()
                    sbTemp.append(s)//심박수
                    newRate = s
                    var textColor = ContextCompat.getColor(requireContext(), R.color.black)
                    if (s.toInt() > 90) {
                        textColor = ContextCompat.getColor(requireContext(), R.color.line_warning)
                        binding.heartRate.setTextColor(textColor)
                    } else if (s.toInt() < 60) {
                        textColor = ContextCompat.getColor(requireContext(), R.color.teal_200)
                        binding.heartRate.setTextColor(textColor)
                        playSoundAndVibration(requireContext())
                        sendMessage("vibrator")
                    } else {
                        textColor = ContextCompat.getColor(requireContext(), R.color.white)
                        binding.heartRate.setTextColor(textColor)
                    }
                    binding.heartRate.text = sbTemp.toString()
                    //Log.d ("onMessageReceived_mainActivity", "feedMultiple()")
                    //chart에 표시
                    //DB로 데이터 전송
                    sum1minHeartRate+=newRate.toInt()
                    count1minHeartRate+=1
                    if (last_time != dateAndTime.toString().substring(14, 16)) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            //val isInserted = async { insertDB(newRate.toInt()) }
                            val Uid = auth.currentUser?.uid
                            val Rate = sum1minHeartRate/count1minHeartRate
                            Log.d("sum",sum1minHeartRate.toString())
                            Log.d("count",count1minHeartRate.toString())
                            Log.d("average ",Rate.toString())
                            Log.d("now",newRate.toString())
                            var Sleep = false
                            if(Rate.toInt()<75){
                                Sleep = true
                            }

                            var newData= HeartRate(dateAndTime.toString().substring(11,19),dateAndTime.toString().substring(0,10),Rate.toInt(),Sleep,Uid)
                            Log.d("NewData ",newData.toString())
                            Log.d("Time",dateAndTime.toString().substring(11,19)+dateAndTime.toString().substring(0,10))
                            var db =
                                Room.databaseBuilder(
                                    requireContext().applicationContext,
                                    AppDatabase::class.java,
                                    "heartRateDB"
                                ).build()

                            db!!.heartRateDao().insertHeartRate(newData)
                            Log.d("fromDB",db!!.heartRateDao().getAll().toString())
                            var heartRateAll = db!!.heartRateDao().getAll()
                            for(data in heartRateAll){
                                Log.d(data.Uid.toString() , data.HeartRate.toString())
                            }
                            Log.d("isInserted", Rate.toString())
                            Log.d("time", dateAndTime.toString().substring(14, 16))
                            last_time = dateAndTime.toString().substring(14, 16)
                            sum1minHeartRate=0
                            count1minHeartRate=0
                        }
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
        TODO("Not yet implemented")
    }

    override fun onPause() {
        super.onPause()
        //overridePendingTransition(0, 0)
        /* Bottom Menu */
        val bottomMenu = mainActivity.findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE
        /* FaceMesh */
        if (isCameraOn) {
//            timer2.cancel()
            glSurfaceView.visibility = View.GONE
            cameraInput.close()
        }
        timer2.cancel()
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
        isCameraOn =
            ActivityCompat.checkSelfPermission(activityContext!!, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        /* Bottom Menu */
        val bottomMenu = mainActivity.findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE
        /* FaceMesh */
        if (isCameraOn) {
            binding.faceFitting.visibility = View.VISIBLE
            postGlSurfaceView()
        } else {
            binding.faceFitting.visibility = View.GONE
        }
        try {
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getMessageClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // AsyncTask 취소
        socketAsyncTask?.cancel(true)

        //socketCoroutine.disconnect()
    }
}