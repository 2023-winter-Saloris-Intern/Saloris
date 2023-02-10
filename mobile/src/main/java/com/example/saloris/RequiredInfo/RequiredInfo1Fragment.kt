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

    /* User Authentication */
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRequiredInfo1Binding.inflate(layoutInflater, container, false)

        var cardColor = ContextCompat.getDrawable(requireContext(),R.drawable.blue_round_button)
        var textColor = ContextCompat.getColor(requireContext(),R.color.white)
        var originalCardColor = ContextCompat.getDrawable(requireContext(),R.drawable.light_grey_btn)
        var originalTextColor = ContextCompat.getColor(requireContext(),R.color.grey)

        //Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        var userInfo = RequiredInfo()

//        val numberPicker: NumberPicker = binding.datePicker
//        numberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
//            if (newVal != 0) {
//                // 값이 0이 아닐 때, 다음 페이지로 이동 가능
//                binding.goNextStepBtn.setOnClickListener {
//                    binding.goNextStepBtn.setBackgroundDrawable(cardColor)
//                    binding.goNextStepBtn.setTextColor(textColor)
//                    navController.navigate(R.id.action_requiredInfoFragment_to_requiredInfo1Fragment)
//                }
//            } else {
//                // 값이 0일 때, 다음 페이지로 이동 불가
//                binding.goNextStepBtn.isEnabled = false
//                binding.goNextStepBtn.setBackgroundDrawable(originalCardColor)
//                binding.goNextStepBtn.setTextColor(originalTextColor)
//            }
//        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.goNextStepBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfoFragment_to_requiredInfo1Fragment)
        }
    }
}