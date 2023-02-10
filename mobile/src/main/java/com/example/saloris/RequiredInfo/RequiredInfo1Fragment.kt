package com.example.saloris.RequiredInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.NumberPicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.R
import com.example.saloris.databinding.FragmentRequiredInfo1Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class RequiredInfo1Fragment : Fragment() {

    /* View */
    private lateinit var binding: FragmentRequiredInfo1Binding
    private lateinit var navController: NavController
    var currentStep: Int = 0

    /* User Authentication */
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            currentStep = savedInstanceState.getInt("currentStep")
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRequiredInfo1Binding.inflate(layoutInflater, container, false)

        //Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        var originalCardColor = ContextCompat.getDrawable(requireContext(),R.drawable.light_grey_btn)
        var originalTextColor = ContextCompat.getColor(requireContext(),R.color.grey)

        binding.goNextStepBtn.isEnabled = false
        binding.goNextStepBtn.setBackgroundDrawable(originalCardColor)
        binding.goNextStepBtn.setTextColor(originalTextColor)
        binding.goNextStepBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfoFragment_to_requiredInfo1Fragment)
//            if (binding.numberPicker != null) {
//                navController.navigate(R.id.action_requiredInfo2Fragment_to_requiredInfo3Fragment)
//            }
        }

        val datePicker: DatePicker = requireView().findViewById(R.id.datePicker)
        datePicker.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
            changeButton()
            val userInfo = RequiredInfo()
            var day: String = "${year}년 ${monthOfYear + 1}월 ${dayOfMonth}일"
            userInfo.userBirth = day
            firestore?.collection("users")?.document(auth?.uid!!)?.update("userBirth", userInfo.userBirth)
        }
    }

    private fun changeButton(){
        var cardColor = ContextCompat.getDrawable(requireContext(),R.drawable.blue_round_button)
        var textColor = ContextCompat.getColor(requireContext(),R.color.white)
        binding.goNextStepBtn.isEnabled = true
        binding.goNextStepBtn.setBackgroundDrawable(cardColor)
        binding.goNextStepBtn.setTextColor(textColor)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentStep", currentStep)
    }
}