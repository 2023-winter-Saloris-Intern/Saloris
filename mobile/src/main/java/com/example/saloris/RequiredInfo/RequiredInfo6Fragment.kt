package com.example.saloris.RequiredInfo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.R
import com.example.saloris.databinding.FragmentRequiredInfo5Binding
import com.example.saloris.databinding.FragmentRequiredInfo6Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RequiredInfo6Fragment : Fragment() {

    /* View */
    private lateinit var binding: FragmentRequiredInfo6Binding
    private lateinit var navController: NavController

    /* User Authentication */
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRequiredInfo6Binding.inflate(layoutInflater, container, false)

        //Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        var userInfo = RequiredInfo()

        var textColor = ContextCompat.getColor(requireContext(),R.color.white)
        var originalTextColor = ContextCompat.getColor(requireContext(),R.color.grey)

        // clickevent 구현 필요
        binding.neverSmoke.setOnClickListener{
            userInfo.userSmoke = "neverSmoke"
            firestore?.collection("users")?.document(auth?.uid.toString())?.update("userSmoke",userInfo.userSmoke)
            binding.neverSmoke.setSelected(true)
            binding.neverSmoke.setTextColor(textColor)
            binding.sometimeSmoke.setSelected(false)
            binding.sometimeSmoke.setTextColor(originalTextColor)
            binding.usuallySmoke.setSelected(false)
            binding.usuallySmoke.setTextColor(originalTextColor)
        }

        binding.sometimeSmoke.setOnClickListener{
            userInfo.userSmoke = "sometimeSmoke"
            firestore?.collection("users")?.document(auth?.uid.toString())?.update("userSmoke",userInfo.userSmoke)
            binding.neverSmoke.setSelected(false)
            binding.neverSmoke.setTextColor(originalTextColor)
            binding.sometimeSmoke.setSelected(true)
            binding.sometimeSmoke.setTextColor(textColor)
            binding.usuallySmoke.setSelected(false)
            binding.usuallySmoke.setTextColor(originalTextColor)
        }

        binding.usuallySmoke.setOnClickListener{
            userInfo.userSmoke = "usuallySmoke"
            firestore?.collection("users")?.document(auth?.uid.toString())?.update("userSmoke",userInfo.userSmoke)
            binding.neverSmoke.setSelected(false)
            binding.neverSmoke.setTextColor(originalTextColor)
            binding.sometimeSmoke.setSelected(false)
            binding.sometimeSmoke.setTextColor(originalTextColor)
            binding.usuallySmoke.setSelected(true)
            binding.usuallySmoke.setTextColor(textColor)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.goNextStepBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo5Fragment_to_loginFragment)
        }

        binding.goBackBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo5Fragment_to_requiredInfo4Fragment)
        }
    }
}