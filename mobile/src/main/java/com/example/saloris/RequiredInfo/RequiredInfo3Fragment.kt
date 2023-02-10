package com.example.saloris.RequiredInfo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.R
import com.example.saloris.databinding.FragmentRequiredInfo2Binding
import com.example.saloris.databinding.FragmentRequiredInfo3Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RequiredInfo3Fragment : Fragment() {
    // 몸무게
    /* View */
    private lateinit var binding: FragmentRequiredInfo3Binding
    private lateinit var navController: NavController

    /* User Authentication */
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    var userInfo = RequiredInfo()

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
        binding = FragmentRequiredInfo3Binding.inflate(layoutInflater, container, false)

        //Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var originalCardColor = ContextCompat.getDrawable(requireContext(),R.drawable.light_grey_btn)
        var originalTextColor = ContextCompat.getColor(requireContext(),R.color.grey)

        navController = Navigation.findNavController(view)
        binding.goNextStepBtn.isEnabled = false
        binding.goNextStepBtn.setBackgroundDrawable(originalCardColor)
        binding.goNextStepBtn.setTextColor(originalTextColor)
        binding.goNextStepBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo2Fragment_to_requiredInfo3Fragment)
//            if (binding.numberPicker != null) {
//                navController.navigate(R.id.action_requiredInfo2Fragment_to_requiredInfo3Fragment)
//            }
        }

        binding.goBackBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo2Fragment_to_requiredInfo1Fragment)
        }

        val numberPicker: NumberPicker = requireView().findViewById(com.example.saloris.R.id.number_picker)

        numberPicker.maxValue = 150 //최대값

        numberPicker.minValue = 0 //최소값

        numberPicker.value = 50 // 초기값
        // 값이 변경될 때 마다 Firestore에 값을 업데이트
        numberPicker.setOnValueChangedListener { _, _, newVal ->
            changeButton()
            userInfo.userWeight = newVal.toString()
            firestore?.collection("users")?.document(auth?.uid!!)?.update("userWeight", userInfo.userWeight)
        }
    }
    private fun changeButton(){
        var cardColor = ContextCompat.getDrawable(requireContext(),R.drawable.blue_round_button)
        var textColor = ContextCompat.getColor(requireContext(),R.color.white)
        binding.goNextStepBtn.isEnabled = true
        binding.goNextStepBtn.setBackgroundDrawable(cardColor)
        binding.goNextStepBtn.setTextColor(textColor)
    }
}