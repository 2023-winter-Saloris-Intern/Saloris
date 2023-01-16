package com.example.saloris.Record

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
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

//        val view = inflater.inflate(R.layout.fragment_record, container, false)
//        val button: Button = view.findViewById(R.id.date_confirm_btn)
//        val dateTextView: TextView = view.findViewById(R.id.date_text)
//        val datePicker: DatePicker = view.findViewById(R.id.date_picker)
//
//        val iYear: Int = datePicker.year
//        val iMonth: Int = datePicker.month+1
//        val iDay: Int = datePicker.dayOfMonth

//        dateTextView.text = "${iYear}년 ${iMonth}월 ${iDay}일"

//        datePicker.setOnDateChangedListener { datePicker, year, month, dayOfMonth ->
//            dateTextView.text = "${year}년 ${month+1}월 ${dayOfMonth}일"
//        }
//        button.setOnClickListener { // 버튼 click 시 선택 된 날짜 정보 불러옴
//            dateTextView.setText("Selected Date: "+ datePicker.getDayOfMonth()+"/"+ (datePicker.getMonth() + 1)+"/"+datePicker.getYear());
//        }

        // clickevent로 수정 필요!
        binding.dateChoiceBtn.setOnClickListener {

//          val datepickerHeaderid = binding.datePicker.getChildAt(0)
//              .resources.getIdentifier("date_picker_header", "id", "android")
//          binding.datePicker.findViewById<View>(datepickerHeaderid).visibility = View.GONE

            val cal = java.util.Calendar.getInstance()
            // 사용자가 OK버튼을 누르면 바인딩된 text가 바뀜
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, day ->
                binding.dateText.text = "${year}년 ${month+1}월 ${day}일"
            }
            // 다이얼로그 생성, 다이얼로그를 띄울때 오늘 날짜를 기본 데이터로 잡아준다.
            //DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            //스피너 모드로 바꿔주기 deprecated 됐기 때문에 themes 에서 추가해줘야함
            val dateDialog = DatePickerDialog(requireActivity(), dateSetListener, cal.get(java.util.Calendar.YEAR), cal.get(
                java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH))
                .apply {
                    datePicker.maxDate = System.currentTimeMillis()
                }

            dateDialog.show()
            //dateDialog.datePicker.spinnersShown = true
        }

//        binding.datePicker.setOnClickListener(){
//            val cal = java.util.Calendar.getInstance()
//            // 사용자가 OK버튼을 누르면 바인딩된 text가 바뀜
//            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, day ->
//                binding.dateText.text = "${year}년. ${month+1}월. ${day}일"
//            }
//            // 다이얼로그 생성, 다이얼로그를 띄울때 오늘 날짜를 기본 데이터로 잡아준다.
//            //DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
//            //스피너 모드로 바꿔주기 deprecated 됐기 때문에 themes 에서 추가해줘야함
//            val dateDialog = DatePickerDialog(requireActivity(), dateSetListener, cal.get(java.util.Calendar.YEAR), cal.get(
//                java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH))
//            dateDialog.show()
//            dateDialog.datePicker.spinnersShown = true
//
//        }
        return binding.root
    }

}