package com.example.saloris.RequiredInfo

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.R
import com.example.saloris.databinding.FragmentRequiredInfo1Binding
import com.example.saloris.databinding.FragmentRequiredInfoBinding
import com.example.saloris.databinding.FragmentSettingBinding
import com.example.saloris.util.MakeToast
import com.example.saloris.util.OpenDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RequiredInfo1Fragment : Fragment() {

    /* View */
    private lateinit var binding: FragmentRequiredInfo1Binding
    private lateinit var navController: NavController

    var cnt:Int=0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRequiredInfo1Binding.inflate(layoutInflater, container, false)

        var cardColor = ContextCompat.getDrawable(requireContext(),R.drawable.blue_round_button)
        var textColor = ContextCompat.getColor(requireContext(),R.color.white)

        // clickevent 구현 필요
        binding.manBtn.setOnClickListener{
            if(binding.manBtn.isPressed==true){
                binding.manBtn.setBackgroundDrawable(cardColor)
                binding.manBtn.setTextColor(textColor)
            }
//            else if(binding.manBtn.isPressed==false){
//
//            }
        }

        binding.womanBtn.setOnClickListener{
            if(binding.womanBtn.isPressed==true){
                binding.womanBtn.setBackgroundDrawable(cardColor)
                binding.womanBtn.setTextColor(textColor)
            }
//            else if(binding.womanBtn.isPressed==false){
//
//            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.goNextStepBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo1Fragment_to_requiredInfo2Fragment)
        }

        binding.goBackBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo1Fragment_to_requiredInfoFragment)
        }
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int){

        }
    }
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
    private lateinit var itemClickListener : OnItemClickListener
}