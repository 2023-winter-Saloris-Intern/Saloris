package com.example.saloris

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.R
import com.example.saloris.RequiredInfo.RequiredInfo
import com.example.saloris.databinding.FragmentRegisterSuccessBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RegisterSuccessFragment : Fragment() {

    private lateinit var binding: FragmentRegisterSuccessBinding
    private lateinit var navController: NavController
    /* User Authentication */
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterSuccessBinding.inflate(layoutInflater, container, false)

        /* Bottom Menu */
        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomMenu.visibility = View.GONE

        //Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        var userInfo = RequiredInfo()

        userInfo.uid = auth?.uid
        userInfo.userId = auth?.currentUser?.email
        userInfo.userName = auth?.currentUser?.displayName
        firestore?.collection("users")?.document(auth?.uid.toString())?.set(userInfo)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        // 필수 정보 입력
        binding.btnRequiredInfo.setOnClickListener {

            navController.navigate(R.id.action_registerSuccessFragment_to_RequiredInfoFragment, arguments)
        }
    }
}