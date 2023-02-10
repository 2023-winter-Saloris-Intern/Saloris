package com.example.saloris.RequiredInfo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.saloris.R
import com.example.saloris.databinding.FragmentRequiredInfo4Binding
import com.example.saloris.databinding.FragmentRequiredInfo5Binding

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

        var cardColor = ContextCompat.getDrawable(requireContext(),R.drawable.blue_round_button)
        var textColor = ContextCompat.getColor(requireContext(),R.color.white)
        var originalCardColor = ContextCompat.getDrawable(requireContext(),R.drawable.light_grey_btn)
        var originalTextColor = ContextCompat.getColor(requireContext(),R.color.grey)

        // clickevent 구현 필요
        binding.neverDrink.setOnClickListener{

            binding.neverDrink.setSelected(true)
            binding.neverDrink.setTextColor(textColor)
            binding.sometimeDrink.setSelected(false)
            binding.sometimeDrink.setTextColor(originalTextColor)
            binding.usuallyDrink.setSelected(false)
            binding.usuallyDrink.setTextColor(originalTextColor)
            binding.goNextStepBtn.setBackgroundDrawable(cardColor)
            binding.goNextStepBtn.setTextColor(textColor)
        }

        binding.sometimeDrink.setOnClickListener{

            binding.neverDrink.setSelected(false)
            binding.neverDrink.setTextColor(originalTextColor)
            binding.sometimeDrink.setSelected(true)
            binding.sometimeDrink.setTextColor(textColor)
            binding.usuallyDrink.setSelected(false)
            binding.usuallyDrink.setTextColor(originalTextColor)
            binding.goNextStepBtn.setBackgroundDrawable(cardColor)
            binding.goNextStepBtn.setTextColor(textColor)
        }

        binding.usuallyDrink.setOnClickListener{

            binding.neverDrink.setSelected(false)
            binding.neverDrink.setTextColor(originalTextColor)
            binding.sometimeDrink.setSelected(false)
            binding.sometimeDrink.setTextColor(originalTextColor)
            binding.usuallyDrink.setSelected(true)
            binding.usuallyDrink.setTextColor(textColor)
            binding.goNextStepBtn.setBackgroundDrawable(cardColor)
            binding.goNextStepBtn.setTextColor(textColor)
        }

        if(!(binding.neverDrink.isSelected) && !(binding.sometimeDrink.isSelected) && !(binding.usuallyDrink.isSelected)) {
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

            if((binding.neverDrink.isSelected) || (binding.sometimeDrink.isSelected) || (binding.usuallyDrink.isSelected)) {
                navController.navigate(R.id.action_requiredInfo4Fragment_to_requiredInfo5Fragment)
            }
           // navController.navigate(R.id.action_requiredInfo4Fragment_to_requiredInfo5Fragment)
        }

        binding.goBackBtn.setOnClickListener {
            navController.navigate(R.id.action_requiredInfo4Fragment_to_requiredInfo3Fragment)
        }
    }
}