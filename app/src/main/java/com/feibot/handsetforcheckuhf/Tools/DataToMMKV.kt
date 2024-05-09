package com.feibot.handsetforcheckuhf.Tools

import com.feibot.handsetforcheckuhf.bean.PlayerInfo
import com.feibot.handsetforcheckuhf.bean.PlayerInfoForGlobal
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.feibot.handsetforcheckuhf.utils.MMKVUtils
import com.tencent.mmkv.MMKV

class DataToMMKV {
    private val mmkv = MMKVUtils.getInstance()

    //设置下载的数据到MMKV中 国内用户
    fun setPlayerInfoToMMKV(raceID: Int, data: PlayerInfo) {
        val networkImportFlag = data.all_num > 0
        if (networkImportFlag) {
            for (player in data.runner) {
                //参赛号
                mmkv.encode("${raceID}_${player.epc}_bib",player.bib)
                //姓名
                mmkv.encode("${raceID}_${player.epc}_name",player.name)
                //性别
                mmkv.encode("${raceID}_${player.epc}_sex",setSex(player.sex))
                //组别
                    if(player.item_title == null){
                    mmkv.encode("${raceID}_${player.epc}_item","")
                }else{
                    mmkv.encode("${raceID}_${player.epc}_item",player.item_title)
                }
                //EPC
                mmkv.encode("${raceID}_${player.epc}_epc",player.epc)
            }
        }
    }
    //设置下载的数据 国外用户版
    fun setGlobalPlayerInfoToMMKV(raceID: Int, data: PlayerInfoForGlobal){
       if(data.runners.isNotEmpty()){
           for (player in data.runners) {
               //参赛号
               mmkv.encode("${raceID}_${player.epc}_bib",player.bib)
               //姓名
               mmkv.encode("${raceID}_${player.epc}_name",player.name)
               //性别
               mmkv.encode("${raceID}_${player.epc}_sex",setGlobalSex(player.sex))
               //组别
               if(player.item.title == null){
                   mmkv.encode("${raceID}_${player.epc}_item","")
               }else{
                   mmkv.encode("${raceID}_${player.epc}_item",player.item.title)
               }
               //EPC
               mmkv.encode("${raceID}_${player.epc}_epc",player.epc)
           }
       }
    }
    //判断男女性别
    private fun setGlobalSex(sex:String):String{
        return if (sex == "M") "Male" else "Female"
    }


    //判断男女性别
    private fun setSex(sex:String):String{
        return if (sex == "M") "男" else "女"
    }
    //删除历史数据
    fun deleteHistoryData(){
        //先清除历史记录
        MMKV.defaultMMKV().clearAll()
    }

}