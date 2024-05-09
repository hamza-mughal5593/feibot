package com.feibot.handsetforcheckuhf.view.detail

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.feibot.handsetforcheckuhf.Tools.TimeStamp
import com.feibot.handsetforcheckuhf.base.BaseApplication
import com.feibot.handsetforcheckuhf.services.TestNetWorkConnection
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 *@Author: Nick
 *@Description:
 *@Date 2021-06-25: 15:54
 */
class DetailFragmentViewModel:ViewModel() {
    private var timerJob = true
    //定义shp
    private lateinit var mShp: SharedPreferences
    //定义赛事信息的LiveData
    val detailViewModelRaceID = MutableLiveData<String>()
    val detailViewModelRaceName = MutableLiveData<String>()
    val detailViewModelRacePosition = MutableLiveData<String>()
    val detailViewModelDeviceID = MutableLiveData<String>()
    //本地时间
    val detailViewModelLocalTime = MutableLiveData<String>()
    //响枪时间
    val detailViewModelGunShotTime = MutableLiveData<String>()
    //监听网络状态
    val  detailViewModelNetworkState = MutableLiveData<Int>()
    //声明一个LiveData网络网络还未上传的列表
    val viewModelUnLoadedList = MutableLiveData<Int>()
    //声明一个LiveData电池电量
    val viewModelBatteryLevel = MutableLiveData<Int>()

/*
* @Author: nick
* @Description: 初始化一些方法
* @DateTime: 2021-06-25 15:56
* @Params: 
* @Return 
*/
    fun initViewModel() {
        //开始循环任务
        startTimerTask()
        //读取shp为界面配置赛事信息
        mShp = BaseApplication.context().getSharedPreferences("deviceInfo", Context.MODE_PRIVATE)
        //设备ID
        detailViewModelDeviceID.postValue(mShp.getString("device_id", ""))
        //赛事ID
        detailViewModelRaceID.postValue(mShp.getString("race_id", ""))
        //赛事名称
        detailViewModelRaceName.postValue(mShp.getString("race_name", ""))
        //赛事坐标位置
        detailViewModelRacePosition.postValue(mShp.getString("race_position", ""))
        //网络连接状态
        detailViewModelNetworkState.postValue(TestNetWorkConnection.getConnectionAsyncResult())
    }
/*
* @Author: nick
* @Description: 停止循环的任务
* @DateTime: 2021-07-15 16:41
* @Params: 
* @Return 
*/
    fun stopLooperTask(){
        stopTimerTask()
    }

/*
* @Author: nick
* @Description: 开启一个时间定时器
* @DateTime: 2021-07-19 14:21
* @Params:
* @Return
*/
    private fun startTimerTask(){
        Observable.interval(60, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .repeatUntil {
                !timerJob
            }.subscribe{
                detailViewModelNetworkState.postValue(TestNetWorkConnection.getConnectionAsyncResult())
            }
        Observable.interval(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .repeatUntil {
                !timerJob
            }.subscribe{
                detailViewModelLocalTime.postValue(TimeStamp.getChangeTimeData())
            }
    }

/*
* @Author: nick
* @Description: 停止时间轮播任务
* @DateTime: 2021-07-19 14:22
* @Params:
* @Return
*/
    private fun stopTimerTask(){
        timerJob = false
    }
}