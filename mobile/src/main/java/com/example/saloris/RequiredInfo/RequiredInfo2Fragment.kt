package com.example.saloris.RequiredInfo

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.R
import com.example.saloris.databinding.FragmentRequiredInfo2Binding
import com.example.saloris.databinding.FragmentRequiredInfoBinding
import com.example.saloris.databinding.FragmentSettingBinding
import com.example.saloris.util.MakeToast
import com.example.saloris.util.OpenDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RequiredInfo2Fragment : Fragment() {

    /* View */
    private lateinit var binding: FragmentRequiredInfo2Binding
    private lateinit var navController: NavController

    lateinit var numberPicker: NumberPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRequiredInfo2Binding.inflate(layoutInflater, container, false)

//        numberPicker!!.setMinValue(40);
//        numberPicker!!.setValue(50);

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.goNextStepBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo2Fragment_to_requiredInfo3Fragment)
        }

        binding.goBackBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo2Fragment_to_requiredInfo1Fragment)
        }

        val numberPicker: NumberPicker = requireView().findViewById(com.example.saloris.R.id.number_picker)

        numberPicker.maxValue = 150 //최대값

        numberPicker.minValue = 40 //최소값

        numberPicker.value = 50 // 초기값
    }
}