package com.feibot.handsetforcheckuhf.view.adapters

import android.telecom.Call
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.feibot.handsetforcheckuhf.view.detail.DetailFragment
import com.feibot.handsetforcheckuhf.view.main.MainFragment
import com.feibot.handsetforcheckuhf.view.search.SearchFragment
import com.feibot.handsetforcheckuhf.view.setting.SettingFragment

/**
 *@Author: Nick
 *@Description:
 *@Date 2021-06-09: 17:08
 */
class MainViewPagerAdapter(fragmentActivity:FragmentActivity):FragmentStateAdapter(fragmentActivity){

    private val fragments = hashMapOf<Int,Fragment>()

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        LogUtils.d(this,"FragmentPosition--->$position")
        if(fragments[position] == null){
            setFragments()
            LogUtils.d(this,"Fragment-size--->${fragments.size}")
        }
        return fragments[position]!!
    }

    private fun setFragments(){
        fragments[0] = MainFragment()
        fragments[1] = DetailFragment()
        fragments[2] = SearchFragment()
        fragments[3] = SettingFragment()
    }

}