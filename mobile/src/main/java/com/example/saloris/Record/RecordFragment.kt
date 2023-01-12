package com.example.saloris.Record

import android.os.Bundle
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.saloris.R
import com.example.saloris.databinding.FragmentRecordBinding


class RecordFragment : Fragment() {
    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRecordBinding.inflate(inflater, container, false)

        //val dateChoiceBtn: ImageButton = requireView().findViewById(com.example.saloris.R.id.date_choice_btn)

//        binding.deviceScanBtn.setOnClickListener {
//            loadFragment(ScanFragment())
//        }

        // clickevent로 수정 필요!
        binding.dateChoiceBtn.setOnClickListener {
            var datePicker = R.id.date_picker
            if (it.isClickable)
            {
                binding.dateChoiceBtn.setImageResource(R.drawable.ic_outline_arrow_circle_up_24)
                binding.datePicker.setVisibility(View.VISIBLE);
            }
            else if(it.isContextClickable)
            {
                binding.datePicker.setVisibility(View.INVISIBLE);
            }
        }

        return binding.root
    }

}