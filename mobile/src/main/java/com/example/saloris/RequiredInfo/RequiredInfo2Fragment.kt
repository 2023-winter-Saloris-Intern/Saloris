package com.example.saloris.RequiredInfo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.R
import com.example.saloris.databinding.FragmentRequiredInfo1Binding
import com.example.saloris.databinding.FragmentRequiredInfo2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RequiredInfo2Fragment : Fragment() {

    /* View */
    private lateinit var binding: FragmentRequiredInfo2Binding
    private lateinit var navController: NavController

    /* User Authentication */
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    var currentStep: Int = 0

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
        binding = FragmentRequiredInfo2Binding.inflate(layoutInflater, container, false)

        //Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        var userInfo = RequiredInfo()



        var cardColor = ContextCompat.getDrawable(requireContext(),R.drawable.blue_round_button)
        var textColor = ContextCompat.getColor(requireContext(),R.color.white)
        var originalCardColor = ContextCompat.getDrawable(requireContext(),R.drawable.light_grey_btn)
        var originalTextColor = ContextCompat.getColor(requireContext(),R.color.grey)


        // clickevent 구현 필요
        binding.manBtn.setOnClickListener{
//            if(binding.manBtn.isPressed==true){
//                binding.manBtn.setSelected(true)
//                binding.manBtn.setTextColor(textColor)
//            }
//            else {
//                binding.womanBtn.setSelected(false)
//                binding.womanBtn.setTextColor(originalTextColor)
//            }
            //파이어 베이스 파이어 스토어에 성별 저장
            userInfo.userSex = true
            firestore?.collection("users")?.document(auth?.uid.toString())?.update("userSex",userInfo.userSex)

            binding.manBtn.setSelected(true)
            binding.manBtn.setTextColor(textColor)
            binding.womanBtn.setSelected(false)
            binding.womanBtn.setTextColor(originalTextColor)
            binding.goNextStepBtn.setBackgroundDrawable(cardColor)
            binding.goNextStepBtn.setTextColor(textColor)
        }

        binding.womanBtn.setOnClickListener{
//            if(binding.womanBtn.isPressed==true){
//                binding.womanBtn.setBackgroundDrawable(cardColor)
//                binding.womanBtn.setTextColor(textColor)
//            }
//            else {
//                binding.manBtn.setSelected(false)
//                binding.manBtn.setTextColor(originalTextColor)
//            }
            //파이어 베이스 파이어 스토어에 성별 저장
            userInfo.userSex = false
            firestore?.collection("users")?.document(auth?.uid.toString())?.update("userSex",userInfo.userSex)
            binding.manBtn.setSelected(false)
            binding.manBtn.setTextColor(originalTextColor)
            binding.womanBtn.setSelected(true)
            binding.womanBtn.setTextColor(textColor)
            binding.goNextStepBtn.setBackgroundDrawable(cardColor)
            binding.goNextStepBtn.setTextColor(textColor)
        }

        if(!(binding.manBtn.isSelected) && !(binding.womanBtn.isSelected)) {
            binding.goNextStepBtn.setSelected(false)
            binding.goNextStepBtn.setBackgroundDrawable(originalCardColor)
            binding.goNextStepBtn.setTextColor(originalTextColor)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.goNextStepBtn.setOnClickListener {

            if((binding.manBtn.isSelected) || (binding.womanBtn.isSelected)) {
                navController.navigate(R.id.action_requiredInfo1Fragment_to_requiredInfo2Fragment)
            }
//            navController.navigate(R.id.action_requiredInfo1Fragment_to_requiredInfo2Fragment)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentStep", currentStep)
    }
}