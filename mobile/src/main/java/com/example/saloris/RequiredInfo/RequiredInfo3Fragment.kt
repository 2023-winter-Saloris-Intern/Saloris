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
import com.example.saloris.databinding.FragmentRequiredInfo3Binding
import com.example.saloris.databinding.FragmentRequiredInfoBinding
import com.example.saloris.databinding.FragmentSettingBinding
import com.example.saloris.util.MakeToast
import com.example.saloris.util.OpenDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RequiredInfo3Fragment : Fragment() {

    /* View */
    private lateinit var binding: FragmentRequiredInfo3Binding
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
        binding = FragmentRequiredInfo3Binding.inflate(layoutInflater, container, false)

//        numberPicker!!.setMinValue(40);
//        numberPicker!!.setValue(50);

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

//        binding.goNextStepBtn.setOnClickListener {
//            navController.navigate(R.id.action_requiredInfoFragment_to_requiredInfoFragment1)
//        }

        binding.goBackBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo3Fragment_to_requiredInfo2Fragment)
        }
    }
}