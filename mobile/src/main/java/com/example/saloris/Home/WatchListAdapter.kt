package com.example.saloris.Home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saloris.R
import com.example.saloris.databinding.ConnectingWatchRecyclerviewBinding


class WatchListAdapter(val watchInfo:MutableList<WachInfo>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class MyViewHolder(val binding: ConnectingWatchRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WatchListAdapter.MyViewHolder(
            ConnectingWatchRecyclerviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("LOGTest",""+itemCount+"---"+position)
        val binding = (holder as MyViewHolder).binding

        binding.watchImage.setImageResource(R.drawable.watch)
//        binding.potLocationImg.setImageResource(R.drawable.study)
//        binding.petsitterImg.setImageResource(R.drawable.example1)
        binding.watchUserName.text=watchInfo[position].watchUserName
        //binding.watchKind.text=watchInfo[position].watchKind

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return watchInfo.size
    }
}