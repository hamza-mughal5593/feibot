package com.feibot.handsetforcheckuhf.utils

import android.content.Context
import com.feibot.handsetforcheckuhf.base.BaseApplication


/**
* 电量 网络状态 不同标签数量
* 获取设备信息的工具类
* @date 2022/9/20 10:00
* @params  * @param null
* @return
* @author nick
*/
object MachineInfoUtil {
    val mShp = BaseApplication.context().getSharedPreferences("deviceInfo", Context.MODE_PRIVATE)
    var machineId:String = mShp.getString("device_id", "")!!
    var eventId:String = mShp.getString("race_id", "")!!
    var battery:String = ""
    var networkState:String = ""
    var diffTagSize:String = "0"
    var totalTagSize:String = "0"
}