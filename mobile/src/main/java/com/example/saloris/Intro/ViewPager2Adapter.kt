package com.example.saloris.Intro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

// class ViewPager2Adapter (fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
//    var fragments: ArrayList<Fragment> = ArrayList()
//
//    override fun getItemCount(): Int {
//        return fragments.size
//    }
//
//    override fun createFragment(position: Int): Fragment {
//        return fragments[position]
//    }
//
//    fun addFragment(fragment: Fragment) {
//        fragments.add(fragment)
//        notifyItemInserted(fragments.size - 1)
//        //TODO: notifyItemInserted!!
//    }
//
//    fun removeFragement() {
//        fragments.removeLast()
//        notifyItemRemoved(fragments.size)
//        //TODO: notifyItemRemoved!!
//    }
//
//}

class ViewPager2Adapter(
    list: ArrayList<Fragment>,
    fm: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fm, lifecycle) {

    private val fragmentList = list

    override fun getItemCount() = fragmentList.size

    override fun createFragment(position: Int) = fragmentList[position]
}

//    override fun getItemCount(): Int = 3
//
//    override fun createFragment(position: Int): Fragment {
//        return when(position) {
//            0 -> IntroSlide1Fragment()
//            1 -> IntroSlide2Fragment()
//            else -> IntroSlide3Fragment()
//        }
//    }

// }