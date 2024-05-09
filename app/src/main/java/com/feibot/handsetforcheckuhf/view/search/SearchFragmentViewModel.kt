package com.feibot.handsetforcheckuhf.view.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.feibot.handsetforcheckuhf.Tools.ReadTagToolPlus
import com.feibot.handsetforcheckuhf.base.IBaseViewModel
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.feibot.handsetforcheckuhf.utils.MMKVUtils

/**
 *@Author: Nick
 *@Description:查询页面ViewModel
 *@Date 2021-07-05: 10:06
 */
class SearchFragmentViewModel:ViewModel(),IBaseViewModel{
    private val mmkvUtils = MMKVUtils.getInstance()
    val viewModelPlayerInfo = MutableLiveData<HashMap<String,String>>()
    private val mTempHashMap = hashMapOf<String,String>()
    private var mRaceID = ""



/*
* @Author: nick
* @Description: 从MMKV中获取数据
* @DateTime: 2021-07-05 11:52
* @Params: 
* @Return 
*/
    fun getPlayerInfoList(raceID:String?){
        if (raceID.isNullOrEmpty()) return
        mRaceID = raceID
        ReadTagToolPlus.getInstance().registerCallback(this)
    }

    override fun onReaderResultLoaded(epc: String) {
        //TODO("Not yet implemented")
    }

    override fun onReaderSingleResultLoaded(epc: String) {
        mTempHashMap["bib"] = mmkvUtils.decodeString("${mRaceID}_${epc}_bib")
        mTempHashMap["name"] = mmkvUtils.decodeString("${mRaceID}_${epc}_name")
        mTempHashMap["sex"] = mmkvUtils.decodeString("${mRaceID}_${epc}_sex")
        mTempHashMap["item"] = mmkvUtils.decodeString("${mRaceID}_${epc}_item")
        mTempHashMap["epc"] = epc
        viewModelPlayerInfo.postValue(mTempHashMap)
        LogUtils.d(this@SearchFragmentViewModel,"epc---->$epc")
    }
}