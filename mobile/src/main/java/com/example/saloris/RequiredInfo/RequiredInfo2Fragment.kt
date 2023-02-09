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
import com.google.firebase.firestore.FirebaseFirestore

class RequiredInfo2Fragment : Fragment() {

    /* View */
    private lateinit var binding: FragmentRequiredInfo2Binding
    private lateinit var navController: NavController

    var cnt: Int = 0
    //true 남, false 여
    var sex: Boolean = true
    // Create a reference to the Firestore database
    val db = FirebaseFirestore.getInstance()

    // Create a new document with a unique ID
    val newDocument = db.collection("users").document()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRequiredInfo2Binding.inflate(layoutInflater, container, false)



        //var cardColor = ContextCompat.getDrawable(requireContext(),R.drawable.blue_round_button)
        var textColor = ContextCompat.getColor(requireContext(),R.color.white)
        //var originalCardColor = ContextCompat.getDrawable(requireContext(),R.drawable.light_grey_btn)
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
            sex = true
            binding.manBtn.setSelected(true)
            binding.manBtn.setTextColor(textColor)
            binding.womanBtn.setSelected(false)
            binding.womanBtn.setTextColor(originalTextColor)
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
            sex = false
            binding.manBtn.setSelected(false)
            binding.manBtn.setTextColor(originalTextColor)
            binding.womanBtn.setSelected(true)
            binding.womanBtn.setTextColor(textColor)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.goNextStepBtn.setOnClickListener {
            // Add data to the new document
            val data = hashMapOf(
                "userSex" to sex,
            )
            db.collection("users").document().set(data)
            newDocument.set(data)
                .addOnSuccessListener { Log.d("Firestore", "$sex Data added successfully!!!!!!!!!!!!") }
                .addOnFailureListener { e -> Log.w("Firestore", "Error adding data", e) }
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