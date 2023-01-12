package com.example.saloris.Record

import android.icu.util.Calendar
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.saloris.R
import com.example.saloris.databinding.FragmentRecordBinding


class RecordFragment : Fragment() {
    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    // Item의 클릭 상태를 저장할 array 객체
    private val selectedItems = SparseBooleanArray()

    // 직전에 클릭됐던 Item의 position
    private val prePosition = -1

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

            if (selectedItems.get(prePosition))
            {
                selectedItems.delete(prePosition)
                binding.dateChoiceBtn.setImageResource(R.drawable.ic_outline_arrow_circle_up_24)
                binding.datePicker.setVisibility(View.VISIBLE)
                binding.dateConfirmBtn.setVisibility(View.VISIBLE)
            } else {
                selectedItems.delete(prePosition)
                selectedItems.put(prePosition,true)
                binding.dateChoiceBtn.setImageResource(R.drawable.ic_baseline_arrow_circle_down_24)
                binding.datePicker.setVisibility(View.INVISIBLE);
                binding.dateConfirmBtn.setVisibility(View.INVISIBLE);
            }
        }

//        binding.datePicker.setOnClickListener {
////            datepickerdialog에 표시할 달력
//            val datepickercalendar = Calendar.getInstance()
//            val year = datepickercalendar.get(Calendar.YEAR)
//            val month = datepickercalendar.get(Calendar.MONTH)
//            val day = datepickercalendar.get(Calendar.DAY_OF_MONTH)
//            var dateText = R.id.date_text
//
//            binding.datePicker.setOnDateChangedListener { datePicker, year, month, dayOfMonth ->
//                dateText.toString() = "${year}년 ${month+1}월 ${dayOfMonth}일"
//            }
//
//        }


        return binding.root
    }

}