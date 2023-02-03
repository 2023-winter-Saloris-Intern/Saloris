package com.example.saloris.RequiredInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.R
import com.example.saloris.databinding.FragmentRequiredInfo3Binding


class RequiredInfo3Fragment : Fragment() {

    /* View */
    private lateinit var binding: FragmentRequiredInfo3Binding
    private lateinit var navController: NavController

    //lateinit var numberPicker: NumberPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRequiredInfo3Binding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.goNextStepBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo3Fragment_to_requiredInfo4Fragment)
        }

        binding.goBackBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo3Fragment_to_requiredInfo2Fragment)
        }

        val numberPicker: NumberPicker = requireView().findViewById(com.example.saloris.R.id.number_picker)

        numberPicker.maxValue = 200 //최대값

        numberPicker.minValue = 150 //최소값

        numberPicker.value = 170 // 초기값
    }
}