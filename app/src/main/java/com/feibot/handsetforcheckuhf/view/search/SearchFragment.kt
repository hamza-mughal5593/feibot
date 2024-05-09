package com.feibot.handsetforcheckuhf.view.search

import android.content.SharedPreferences
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.feibot.handsetforcheckuhf.MainActivity
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.Tools.ReadTagToolPlus
import com.feibot.handsetforcheckuhf.base.BaseFragment
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.feibot.handsetforcheckuhf.view.main.MainFragmentViewModel
import kotlinx.android.synthetic.main.fragment_search.*

/**
 *@Author: Nick
 *@Description:搜索页面的界面
 *@Date 2021-06-09: 17:17
 */
class SearchFragment :BaseFragment(){
    private var mMainReaderRunningTv: TextView? = null
    private var mShp:SharedPreferences ? = null
    private var raceID:String? = null
    private var closeEvent:CloseEvents? = null
    //加载搜索页面的ViewModel
    private val mSearchFragmentViewModel by lazy {
        ViewModelProvider(requireActivity()).get(SearchFragmentViewModel::class.java)
    }
    //加载主页面的ViewModel
    private val mMainFragmentViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainFragmentViewModel::class.java)
    }

    override fun initView() {
        mShp = context!!.getSharedPreferences("deviceInfo",0)
        raceID = mShp?.getString("race_id","0")
        mSearchFragmentViewModel.getPlayerInfoList(raceID)
        mMainReaderRunningTv = mainReaderRunningTv
        epcNumberTv.text = ""
    }

    interface CloseEvents{
        fun onSearchViewClose()
    }

    override fun initViewModel() {

    }

    override fun initObserve() {
        //当数据回来时观察显示
        mSearchFragmentViewModel.viewModelPlayerInfo.observe(this){
            bibNumberTv.text = it["bib"]
            LogUtils.d(this,"bib_number is ${it["bib"]}")
            nameTv.text = it["name"]
            sexTv.text = it["sex"]
            teamTv.text = it["item"]
            epcNumberTv.text = it["epc"]
        }
        //观察主界面的读卡状态，主界面读卡中则搜索界面停止读卡
        mMainFragmentViewModel.viewModelReaderSwitchFlag.observe(this){
            if(it){
                mMainReaderRunningTv?.visibility = View.GONE
                alphaBackgroundView.visibility = View.GONE
                readEpcBt.isEnabled = true
            }else{
                mMainReaderRunningTv?.visibility = View.VISIBLE
                alphaBackgroundView.visibility = View.VISIBLE
                readEpcBt.isEnabled = false
            }
        }
        //注册物理按键监听读卡按键
        (requireActivity() as MainActivity).registerKeyDown(object : MainActivity.OnKeyDownInterface{
            override fun onKeyDown() {

            }
            //单次读卡
            override fun singleRead() {
                ReadTagToolPlus.getInstance().wakeSingleReader()
            }
        })
    }
/*
* @Author: nick
* @Description: 加载点击事件
* @DateTime: 2021-07-05 16:41
* @Params: 
* @Return 
*/
    override fun initEvent() {
        readEpcBt.setOnClickListener {
            ReadTagToolPlus.getInstance().wakeSingleReader()
            //it.isEnabled = false
        }
        //退出该页面按钮
        checkEpcBackIv.setOnClickListener {
            closeEvent?.onSearchViewClose()
            (requireActivity() as MainActivity).searchViewClose()
            requireActivity().supportFragmentManager.beginTransaction().hide(this).commit()
            ReadTagToolPlus.getInstance().pauseSingleReader()
            readEpcBt.isEnabled = true
        }
    }

    override fun setViewID(): Int {
        return R.layout.fragment_search
    }

    override fun onDestroy() {
        ReadTagToolPlus.getInstance().unRegisterCallback()
        super.onDestroy()
    }

    fun closeEvent(callback:CloseEvents){
        closeEvent = callback
    }

}