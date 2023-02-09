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
import com.example.saloris.databinding.FragmentRequiredInfo4Binding
import com.example.saloris.databinding.FragmentRequiredInfo5Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RequiredInfo5Fragment : Fragment() {

    /* View */
    private lateinit var binding: FragmentRequiredInfo5Binding
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
        binding = FragmentRequiredInfo5Binding.inflate(layoutInflater, container, false)

        //Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        var userInfo = RequiredInfo()

        var textColor = ContextCompat.getColor(requireContext(),R.color.white)
        var originalTextColor = ContextCompat.getColor(requireContext(),R.color.grey)

        // clickevent 구현 필요
        binding.neverDrink.setOnClickListener{
            userInfo.userDrink = "neverDrink"
            firestore?.collection("users")?.document(auth?.uid.toString())?.update("userDrink",userInfo.userDrink)
            binding.neverDrink.setSelected(true)
            binding.neverDrink.setTextColor(textColor)
            binding.sometimeDrink.setSelected(false)
            binding.sometimeDrink.setTextColor(originalTextColor)
            binding.usuallyDrink.setSelected(false)
            binding.usuallyDrink.setTextColor(originalTextColor)
        }

        binding.sometimeDrink.setOnClickListener{
            userInfo.userDrink = "sometimeDrink"
            firestore?.collection("users")?.document(auth?.uid.toString())?.update("userDrink",userInfo.userDrink)
            binding.neverDrink.setSelected(false)
            binding.neverDrink.setTextColor(originalTextColor)
            binding.sometimeDrink.setSelected(true)
            binding.sometimeDrink.setTextColor(textColor)
            binding.usuallyDrink.setSelected(false)
            binding.usuallyDrink.setTextColor(originalTextColor)
        }

        binding.usuallyDrink.setOnClickListener{
            userInfo.userDrink = "usuallyDrink"
            firestore?.collection("users")?.document(auth?.uid.toString())?.update("userDrink",userInfo.userDrink)
            binding.neverDrink.setSelected(false)
            binding.neverDrink.setTextColor(originalTextColor)
            binding.sometimeDrink.setSelected(false)
            binding.sometimeDrink.setTextColor(originalTextColor)
            binding.usuallyDrink.setSelected(true)
            binding.usuallyDrink.setTextColor(textColor)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.goNextStepBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo4Fragment_to_requiredInfo5Fragment)
        }

        binding.goBackBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo4Fragment_to_requiredInfo3Fragment)
        }
    }
}