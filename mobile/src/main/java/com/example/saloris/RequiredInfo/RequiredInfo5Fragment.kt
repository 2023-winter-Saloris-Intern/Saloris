package com.example.saloris.RequiredInfo

import android.app.Activity
import android.os.Bundle
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
import com.example.saloris.databinding.FragmentRequiredInfo4Binding
import com.example.saloris.databinding.FragmentRequiredInfo5Binding
import com.example.saloris.databinding.FragmentRequiredInfoBinding
import com.example.saloris.databinding.FragmentSettingBinding
import com.example.saloris.util.MakeToast
import com.example.saloris.util.OpenDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RequiredInfo5Fragment : Fragment() {

    /* View */
    private lateinit var binding: FragmentRequiredInfo5Binding
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRequiredInfo5Binding.inflate(layoutInflater, container, false)

        var textColor = ContextCompat.getColor(requireContext(),R.color.white)
        var originalTextColor = ContextCompat.getColor(requireContext(),R.color.grey)

        // clickevent 구현 필요
        binding.neverSmoke.setOnClickListener{

            binding.neverSmoke.setSelected(true)
            binding.neverSmoke.setTextColor(textColor)
            binding.sometimeSmoke.setSelected(false)
            binding.sometimeSmoke.setTextColor(originalTextColor)
            binding.usuallySmoke.setSelected(false)
            binding.usuallySmoke.setTextColor(originalTextColor)
        }

        binding.sometimeSmoke.setOnClickListener{

            binding.neverSmoke.setSelected(false)
            binding.neverSmoke.setTextColor(originalTextColor)
            binding.sometimeSmoke.setSelected(true)
            binding.sometimeSmoke.setTextColor(textColor)
            binding.usuallySmoke.setSelected(false)
            binding.usuallySmoke.setTextColor(originalTextColor)
        }

        binding.usuallySmoke.setOnClickListener{

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