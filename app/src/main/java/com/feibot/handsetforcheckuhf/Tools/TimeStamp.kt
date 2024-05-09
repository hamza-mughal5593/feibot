package com.feibot.handsetforcheckuhf.Tools

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.feibot.handsetforcheckuhf.base.BaseApplication
import com.feibot.handsetforcheckuhf.utils.LogUtils
import kotlinx.coroutines.GlobalScope
import java.text.SimpleDateFormat
import java.util.*

/**
 *@Author: Nick
 *@Description:时间戳
 *@Date 2021-06-19: 15:06
 */
@SuppressLint("SimpleDateFormat")
object TimeStamp {
    var offTime  = 0L
    var lastDestroyMillis  = 0L
    var runningTimeMillis  = 0L
    var changeMillis = 0L
    var runningChangeMillis = -28800000L
    var runningOffTimeMillis = 0L

    init {
        val mShp = BaseApplication.context().getSharedPreferences("deviceInfo",0)
        //设置本地时间的时间差
        offTime =  mShp.getLong("off_time",0L)
        lastDestroyMillis =  mShp.getLong("destroy_time_millis",0L)
        runningTimeMillis =  mShp.getLong("running_off_time",0L)
        //设置赛事用时的时间差
        runningOffTimeMillis = getCurrentMillisTime() - lastDestroyMillis + runningTimeMillis
    }

    //时间戳  上传Epc时间标签默认类型是4
    fun getMillis(offset: Long, type: Int): String? {
        val timeNow = System.currentTimeMillis()
        val offNow = timeNow + offTime
        val date = Date()
        date.time = offNow
        var strForm = ""
        when (type) {
            1 -> strForm = "yyyy-MM-dd HH:mm:ss"
            2 -> strForm = "yyyy-MM-dd HH:mm:ss:SSS"
            3 -> strForm = "HH:mm:ss:SSS"
            4 -> strForm = "yy-MM-dd~HH:mm:ss:SS"
            5 -> strForm = "HH:mm:ss"
            6 -> strForm = "yyyy-MM-dd"
            7 -> strForm = "yy-MM-dd HH:mm:ss:SSS"
            else -> {
            }
        }
        val simpleDateFormat = SimpleDateFormat(strForm)
        return simpleDateFormat.format(date)
    }
    //已经修改后的时间
    fun getChangeTimeData():String{
        if(changeMillis == 0L){
            return getMillis(0L,5)!!
        }
        return SimpleDateFormat("HH:mm:ss").format(changeMillis).apply {
            changeMillis += 1000
        }
    }

    //响枪按钮按下后赛事用时开始计时
    fun getFirstRunningTime():String{
        runningChangeMillis += 1000
        return SimpleDateFormat("HH:mm:ss").format(runningChangeMillis)
    }
    //启动后应用后 如果赛事正在进行 赛事时间持续进行
    fun getSavedRunningTime():String{
        return SimpleDateFormat("HH:mm:ss").format(runningOffTimeMillis).apply {
            runningOffTimeMillis += 1000
        }
    }

    //设置本地时间(获取当前millis格式的当前时间)
    fun getCurrentMillisTime():Long{
        return System.currentTimeMillis() + offTime
    }
}