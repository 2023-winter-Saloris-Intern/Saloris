package com.example.saloris.Setting

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.R
import com.example.saloris.databinding.FragmentSettingBinding
import com.example.saloris.util.MakeToast
import com.example.saloris.util.OpenDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingFragment : Fragment() {

    /* View */
    private lateinit var binding: FragmentSettingBinding
    private lateinit var navController: NavController

    /* Dialog */
    private val dialog = OpenDialog()

    /* Toast */
    private val toast = MakeToast()

    /* User Authentication */
    private lateinit var auth: FirebaseAuth

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
            navController.navigate(R.id.action_settingFragment_to_ImageSlideFragment)
        }
    }
}