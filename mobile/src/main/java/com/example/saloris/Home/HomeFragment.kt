package com.example.saloris.Home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.example.saloris.MainActivity
import com.example.saloris.R
import com.example.saloris.data.Networking
import com.example.saloris.data.showData
import com.example.saloris.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.saloris.util.MakeToast

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var navController: NavController

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    var btnBackPressedTime: Long = 0

    /* Toast */
    private val toast = MakeToast()

    /* User Authentication */
    private lateinit var auth: FirebaseAuth

    private fun isAutoLogined(): Boolean {
        val autoLoginPref =
            requireContext().getSharedPreferences("autoLogin", Activity.MODE_PRIVATE)
        return autoLoginPref.contains("username")
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
                    toast.makeToast(context, "한 번 더 누르면 종료됩니다.")
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* User Authentication */
        auth = Firebase.auth
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        /* Bottom Menu */
        val bottomMenu = (requireActivity() as MainActivity).binding.bottomNav
        bottomMenu.visibility = View.VISIBLE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        /* User Authentication */
        if (auth.currentUser == null) {
            if (isAutoLogined()) {
                context?.let { toast.makeToast(it, "로그인에 실패했습니다.") }
            }
            navController.navigate(R.id.action_homeFragment_to_loginStartFragment)
        } else {
            if (!auth.currentUser?.isEmailVerified!!) {
                context?.let { toast.makeToast(it, "메일함에서 인증해주세요") }
                navController.navigate(R.id.action_homeFragment_to_loginStartFragment)
            }
            binding.userName.text = auth.currentUser!!.displayName
        }

        binding.startBtn.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_driveFragment)
        }
        binding.buttonTest.setOnClickListener {
            val intent = Intent(getActivity(), Networking::class.java)
            startActivity(intent)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        /* Bottom Menu */
        val bottomMenu = (requireActivity() as MainActivity).binding.bottomNav
        bottomMenu.visibility = View.VISIBLE
    }

    override fun onDetach() {
        super.onDetach()
        onBackPressedCallback.remove()
    }

}