package com.example.saloris.RequiredInfo

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        cnt= 0
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRequiredInfo1Binding.inflate(layoutInflater, container, false)

        // clickevent 구현 필요
        if(cnt<2){
            if(binding.manBtn.isSelected==true){
                binding.manBtn.isSelected=false
                cnt--
            }
            else{
                binding.womanBtn.isSelected=true
                cnt++
            }
        }
        else{
            if(binding.manBtn.isSelected==true){
                binding.manBtn.isSelected=false
                cnt--
            }
            else{
                binding.womanBtn.isSelected=true
                cnt--
            }
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