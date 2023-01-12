package com.example.saloris.Home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saloris.R
import com.example.saloris.databinding.FragmentScanBinding
import com.example.saloris.databinding.FragmentWatchConnectListBinding


class WatchConnectListFragment : Fragment() {
    private var _binding: FragmentWatchConnectListBinding? = null
    private val binding get() = _binding!!
    val wachInfo= mutableListOf<WachInfo>()
    val WatchListAdapter=WatchListAdapter(wachInfo)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        val toolbar = (activity as AppCompatActivity?)!!.supportActionBar
//        toolbar?.setDisplayHomeAsUpEnabled(true) // 액션바 왼쪽에 버튼 만들기(defalut:뒤로가기버튼)
//        toolbar?.setHomeAsUpIndicator(R.drawable.ic_baseline_keyboard_backspace_24)
//        toolbar?.setTitle("문화생활팟")


        _binding = FragmentWatchConnectListBinding.inflate(inflater,container,false)
//        binding.petListRecyclerView.layoutManager=LinearLayoutManager(requireContext())
//        binding.petListRecyclerView.adapter= PetListAdapter(pets)

        wachInfo.add(
            WachInfo("R.drawable.watch","000","galaxy Watch 5"))
        wachInfo.add(
            WachInfo("R.drawable.watch","000","galaxy Watch Active"))
        wachInfo.add(
            WachInfo("R.drawable.watch","000","galaxy Watch 4"))


//        binding.petsitterImage.setImageResource(R.drawable.dog_service)
//        binding.myName.text=userProfile[position].userName
//        binding.myPhoneNum.editableText=userProfile[position].userPhoneNum
//        binding.myLocation.editableText=userProfile[position].userLoaction
//        binding.myGender.text=userProfile[position].userGender
//        binding.myAge.editableText=userProfile[position].userAge

        binding.watchListRecyclerview.layoutManager= LinearLayoutManager(requireContext())
        binding.watchListRecyclerview.adapter=WatchListAdapter
        WatchListAdapter.setItemClickListener(object : WatchListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                //loadFragment(DetailInformationFragment())
            }

        })

        binding.watchListRecyclerview.setOnClickListener {
            val bottomSheet = WatchConnectDialog1Fragment()
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

//        binding.fab.setOnClickListener {
//            val intent = Intent(getActivity(), PartyOpenActivity::class.java)
//            startActivity(intent)
//        }

        return binding.root
    }
//    private fun loadFragment(fragment: Fragment){
//        Log.d("clickTest","click!->"+fragment.tag)
//        val transaction = requireActivity().supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.frame_layout,fragment)
//        transaction.addToBackStack(null)
//        transaction.commit()
//    }
}