package com.example.saloris

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import com.example.saloris.LocalDB.AppDatabase
import com.example.saloris.databinding.FragmentAccountBinding
import com.example.saloris.util.MakeToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.exceptions.InfluxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

class AccountFragment : Fragment() {
    /* View */
    private lateinit var binding: FragmentAccountBinding
    private lateinit var navController: NavController

    /* Toast */
    private val toast = MakeToast()

    /* User Authentication */
    private lateinit var auth: FirebaseAuth

    val user="user"
    val org = "intern"
    val bucket = "HeartRate"
    val token = "yZmCmFFTYYoetepTiOpXDRK8oyL1f_orD6oZH8SXsvlf213z-_iRmXtaf-AjyLe2HS-NhfxcNeY-0K6qR0k6Sw=="

    private fun deleteAutoLoginInfo() {
        val autoLoginPref =
            requireContext().getSharedPreferences("autoLogin", Activity.MODE_PRIVATE)
        val autoLoginEdit = autoLoginPref.edit()
        autoLoginEdit.clear()
        autoLoginEdit.apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        // userName, userId 받아오기
        binding.userName.text = auth.currentUser!!.displayName
        binding.userId.text = auth.currentUser!!.email

//        // 로그아웃 -> 로그인 화면
//        binding.btnLogout.setOnClickListener {
//            auth.signOut()
//            deleteAutoLoginInfo()
//            navController.navigate(R.id.action_accountFragment_to_loginStartFragment)
//        }

        // 회원탈퇴 -> 로그인 화면
        binding.btnAccountRemoval.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("계정 탈퇴")
            builder.setMessage("정말 계정을 삭제 하시겠습니까?")
            builder.setPositiveButton("네") { _: DialogInterface, _: Int ->
                val uid = auth.currentUser!!.uid
                val user = FirebaseAuth.getInstance().currentUser
                Log.d("user","네~!!!!!!!!!!!!!!!!!!!!!!")
                Log.d("user",uid)
                //val dao = HeartRateDao()

                lifecycleScope.launch(Dispatchers.IO) {
//                    val isDeleted = async { deleteAllByUser(uid) }
                    var db =
                        Room.databaseBuilder(
                            requireContext().applicationContext,
                            AppDatabase::class.java,
                            "heartRateDB"
                        ).build()
                    db!!.heartRateDao().deleteAll(uid)
                    if (true) {
                        user?.delete()
                            ?.addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    Log.d("isSuccessful", "isSuccessful@@@@@@@@@@@@@")
                                    context?.let { context ->
                                        toast.makeToast(context, "계정이 삭제되었습니다")
                                    }
                                    navController.navigate(R.id.action_accountFragment_to_loginStartFragment)
                                } else if(deleteTask.isCanceled) {
                                    Log.d("isCanceled", "isCanceled@@@@@@@@@@@@@")

                                } else if(deleteTask.isComplete) {
                                    Log.d("isComplete", "isComplete@@@@@@@@@@@@@")

                                }
                                else {
                                    Log.d("else", deleteTask.result.toString()+"!!!!!!!!!!!!!!!")
                                    Log.d("else", deleteTask.exception.toString()+"@@@@@@@@@@@@@")
                                    context?.let { context ->
                                        toast.makeToast(
                                            context, "계정이 삭제되지 않았습니다!!. 다시 시도해 주세요."
                                        )
                                    }
                                }
                            }
                        // ?: Backup?
                    } else {
                        context?.let { context ->
                            toast.makeToast(context, "계정이 삭제되지 않았습니다. 다시 시도해 주세요.")
                        }
                    }
                }
            }
            builder.setNegativeButton("아니요") { _: DialogInterface, _: Int -> }
            builder.show()
        }
    }
    fun deleteAllByUser(uid: String): Boolean {
        val client = InfluxDBClientFactory.create()

        val start = OffsetDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneId.of("UTC"))
        val stop = OffsetDateTime.now()
        val measurement = "user"
        client.use {
            try {
                val deleteApi = client.deleteApi
                deleteApi.delete(
                    start, stop, "_measurement=\"$measurement\" AND Uid=\"$uid\"", bucket, org
                )
            } catch (ie: InfluxException) {
                Log.e("InfluxException", "Delete: ${ie.cause}")
                return false
            }
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}