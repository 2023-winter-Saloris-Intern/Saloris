package com.example.saloris.Home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saloris.R
import com.example.saloris.databinding.FragmentScanBinding
import com.example.saloris.databinding.FragmentSettingBinding
import com.example.saloris.databinding.FragmentWatchConnectListBinding

class ScanFragment : Fragment() {
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    val wachInfo= mutableListOf<WachInfo>()
    val WatchListAdapter=WatchListAdapter(wachInfo)



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater,container,false)

        wachInfo.add(
            WachInfo("R.drawable.watch","000","galaxy Watch 5"))
        wachInfo.add(
            WachInfo("R.drawable.watch","000","galaxy Watch Active"))
        wachInfo.add(
            WachInfo("R.drawable.watch","000","galaxy Watch 4"))

        binding.watchListRecyclerview.layoutManager= LinearLayoutManager(requireContext())
        binding.watchListRecyclerview.adapter=WatchListAdapter
        WatchListAdapter.setItemClickListener(object : WatchListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                //loadFragment(DetailInformationFragment())
            }

        })


        return binding.root
    }

}