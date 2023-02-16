package com.example.saloris

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.R
import com.example.saloris.RequiredInfo.RequiredInfo
import com.example.saloris.databinding.FragmentRegisterSuccessBinding
import com.example.saloris.util.MakeToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class RegisterSuccessFragment : Fragment() {

    private lateinit var binding: FragmentRegisterSuccessBinding
    private lateinit var navController: NavController

    /* User Authentication */
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    /* Toast */
    private val toast = MakeToast()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRegisterSuccessBinding.inflate(layoutInflater, container, false)

        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        //Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        var userInfo = RequiredInfo()
        userInfo?.uid = auth?.uid
        userInfo?.userName = auth?.currentUser?.displayName
        userInfo?.userId = auth?.currentUser?.email
        userInfo?.emailVerified = auth?.currentUser?.isEmailVerified
        firestore?.collection("users")?.document(auth?.uid.toString())?.set(userInfo)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        auth = Firebase.auth
        val currentUser = FirebaseAuth.getInstance().currentUser

        binding.btnRequiredInfo.isEnabled = false
        binding.checkEmailVerified.setOnClickListener {
            currentUser?.reload()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val isEmailVerified = currentUser.isEmailVerified
                    Log.d("checkEmailVerified", "${isEmailVerified}!!!!!!!!")

                    if (isEmailVerified) {
                        binding.btnRequiredInfo.isEnabled = true
                        toast.makeToast(requireContext(), "이메일 인증이 확인되었습니다.")
                    }
                } else {
                    Log.d("checkEmailVerified", "${auth.currentUser?.isEmailVerified!!}!!!!!!!!")
                    toast.makeToast(requireContext(), "메일함을 확인해주세요.")
                }
                // 작업 실패 처리
            }
        }
        Log.d("checkEmailVerified", "${auth.currentUser?.isEmailVerified!!}!!!!!!!!")
//            if (auth.currentUser?.isEmailVerified!!) {
//                binding.btnRequiredInfo.isEnabled = true
//            } else {
//                Log.d("checkEmailVerified", "${auth.currentUser?.isEmailVerified!!}!!!!!!!!")
//                Firebase.auth.currentUser!!.sendEmailVerification()
//                    .addOnCompleteListener { verifyTask ->
//                        context?.let { context ->
//                            toast.makeToast(
//                                context,
//                                if (verifyTask.isSuccessful)
//                                    "인증 이메일을 다시 전송했습니다. 메일함에서 인증해주세요"
//                                else
//                                    "메일함을 확인해주세요"
//                            )
//                        }
//                    }
//            }
//            val user = auth.currentUser
//            if (user != null && user.isEmailVerified) {
//                binding.btnRequiredInfo.isEnabled = true
//            } else {
//                context?.let { toast.makeToast(it, "메일함에서 인증해주세요") }
//                binding.btnRequiredInfo.isEnabled = false
//            }
//        }
        // 필수 정보 입력
        binding.btnRequiredInfo.setOnClickListener {
            navController.navigate(R.id.action_registerSuccessFragment_to_RequiredInfoFragment,
                arguments)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}