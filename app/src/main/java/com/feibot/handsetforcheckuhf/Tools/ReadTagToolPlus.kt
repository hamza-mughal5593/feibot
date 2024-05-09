package com.feibot.handsetforcheckuhf.Tools

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.AsyncTask
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.base.IBaseViewModel
import com.feibot.handsetforcheckuhf.bean.Epc
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*




/**
 *@Author: Nick
 *@Description:读卡UHF的工具类
 *@Date 2021-06-09: 17:04
 */
class ReadTagToolPlus{
    companion object{
        private var mInstance:ReadTagToolPlus? = null
        get() {
            if(field == null){
                field = ReadTagToolPlus()
            }
            return field
        }
        fun getInstance():ReadTagToolPlus{
            return mInstance!!
        }
    }


    private var mRes: UHFTAGInfo? = null
    private var mRunAction: Job? = null
    private var readSingleSwitch: Boolean = false
    private var player: MediaPlayer? = null
    var mReader: RFIDWithUHFUART? = null
    private var mCallback:OnReaderResultLoaded? = null
    private var mShp: SharedPreferences? = null
    private var mDefaultPower = 16
    private val mCallbacks = arrayListOf<IBaseViewModel>()
    private var longReadSwitch = false
    private var job: Job? = null

    //读卡器开关
    private var readSwitch = false
    //epc列表集合
    private val mEpcList = arrayListOf<Epc>()
    //读取Epc的结果
    private var epcStr = ""

/*
* @Author: nick
* @Description: 加载读卡器初始化组件
* @DateTime: 2021-06-10 14:16
* @Params:
* @Return
*/
    fun initModule(context:Context) {
        if(RFIDWithUHFUART.getInstance() != null){
            RFIDWithUHFUART.getInstance().free()
        }
        //从Shp获取功率值
        mShp = context.getSharedPreferences("deviceInfo",0)
        mDefaultPower = mShp!!.getInt("reader_power",16)
        try {
            mReader = RFIDWithUHFUART.getInstance()
            if(mReader != null){
                runBlocking(Dispatchers.IO) {
                    mReader?.init().let {
                        LogUtils.d("","读卡器启动" + if(it!!) "成功!" else "失败!")
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.e("","读卡器启动失败!")
            e.printStackTrace()
        }

        //获取读写器设备示例，若返回null，则设备电源打开失败
        //readerDevice = UhfReaderDevice.getInstance()
        //设置读取频率
        //reader?.setOutputPower(mDefaultPower)
        //初始化读取声音
        player = MediaPlayer.create(context, R.raw.msg)
        //开始读卡操作
        //readTag()
    }

/*
* @Author: nick
* @Description:执行读卡
* @DateTime: 2021-06-10 13:48
* @Params:
* @Return
*/
    private fun readTag(start:Boolean){
        mReader?.apply {
            if (start) startInventoryTag() else stopInventory()
        }
        readFromBuffer(start)
    }

    private fun readFromBuffer(start:Boolean){
            if(start){
                LogUtils.d("","读卡已经开始!")
            }else{
                LogUtils.d("","读卡已经结束!")
            }
            longReadSwitch = start
//            mRes = mReader?.readTagFromBuffer()
//            Observable.just(if(mRes == null) "" else mRes!!.epc)
//                .repeatUntil {
//                    !longReadSwitch
//                }.subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe {
//                    //播放器播放声音
//                    //player?.start()
//                    this@ReadTagToolPlus.epcStr = it
//                    //供外部使用结果读取后
//                    mCallback?.onReaderResultLoaded()
//                    //LogUtils.d(this@ReadTagToolPlus,"epc str -->$epcStr")
//                }
            try {
                GlobalScope.launch(Dispatchers.IO){
                    while (longReadSwitch){
                        mRes = mReader?.readTagFromBuffer()
                        if (mRes != null) {
                            //播放器播放声音
                            player?.start()
                            this@ReadTagToolPlus.epcStr = mRes!!.epc
                            //供外部使用结果读取后
                            mCallback?.onReaderResultLoaded()
                            //LogUtils.d(this@ReadTagToolPlus,"epc str -->$epcStr")
                        }
                    }
                }
            }catch (e:java.lang.Exception){
                e.printStackTrace()
            }
//            if(start){
//                if(mRunAction?.start()!!){
//                    LogUtils.d(this@ReadTagToolPlus,"开始读取")
//                }else{
//                    LogUtils.d(this@ReadTagToolPlus,"读取队列未开始!")
//                }
//            }else{
//                mRunAction?.cancel()
//            }
    }

/*
* @Author: nick
* @Description: 执行单次读卡 循环读卡
* @DateTime: 2021-07-05 09:56
* @Params:
* @Return
*/
    private fun readSingleTag(){
        try {
            //开始读卡
            val strUII: UHFTAGInfo? = mReader?.inventorySingleTag()
            if(strUII != null){
                //播放器播放声音
                player?.start()
                epcStr = strUII.epc
                //供外部使用结果读取后
                for (mCallback in mCallbacks) {
                    mCallback.onReaderSingleResultLoaded(epcStr)
                }
            }
        }catch (e:InterruptedException){
            e.printStackTrace()
        }
    }
/*
* @Author: nick
* @Description: 读卡器结果转列表
* @DateTime: 2021-06-10 14:17
* @Params: 
* @Return 
*/
    private fun setReaderResultToList(readerResult: String) {
        val epcTag = Epc(1,"1",1,"1")
        if (mEpcList.isEmpty()){
            epcTag.epc = readerResult
            epcTag.count = 1
            mEpcList.add(epcTag)
        }else{
            for ((index, _) in mEpcList.withIndex()){
                val mEpc = mEpcList[index]
                //判断List中是否存在该Epc
                if(readerResult == mEpc.epc){
                    mEpc.count += 1
                    mEpcList.add(index,mEpc)
                    break
                }else if(index == mEpcList.size - 1){
                    epcTag.epc = readerResult
                    epcTag.count = 1
                    mEpcList.add(epcTag)
                }
            }
        }
        LogUtils.d(this,"EpcList size -->${mEpcList.size}")
    }
/*
* @Author: nick
* @Description: 返回读卡器结果列表给外部使用
* @DateTime: 2021-06-10 15:12
* @Params: 
* @Return 
*/
    fun onReaderResultListLoaded():ArrayList<Epc>{
        return mEpcList
    }


    /*
* @Author: nick
* @Description: 返回读卡器结果给外部使用
* @DateTime: 2021-06-10 15:12
* @Params:
* @Return
*/
    fun onReaderResultLoaded():String{
        return epcStr
    }
    
/*
* @Author: nick
* @Description: 关闭读卡器
* @DateTime: 2021-06-10 13:58
* @Params: 
* @Return 
*/
    fun closeReader(){
        //读卡器开关
            readSwitch = false
        //单次读卡开关
            readSingleSwitch = false
        //关闭读卡器
            mReader?.free()
        //释放播放器
            player?.release()
    }
/*
* @Author: nick
* @Description: 暂停读取
* @DateTime: 2021-06-22 14:47
* @Params:
* @Return
*/
    fun pauseReader(){
        //mReader?.stopInventory()
        readSwitch = false
        readTag(false)
    }
/*
* @Author: nick
* @Description: 暂停循环读取
* @DateTime: 2021-07-14 15:15
* @Params:
* @Return
*/
    fun pauseSingleReader(){
        //mReader?.stopInventory()
        readSingleSwitch = false
    }

/**
* @Author: nick
* @Description: 暂停之后开启读取 唤醒读取
* @DateTime: 2021-06-22 14:47
* @Params:
* @Return
*/
    fun wakeReader(){
        readSwitch = true
        readTag(true)
    }
/**
* @Author: nick
* @Description: 暂停之后开启读取 查询页面的读取
* @DateTime: 2021-07-14 15:16
* @Params:
* @Return
*/
    fun wakeSingleReader(){
        //readSingleSwitch = true
        readSingleTag()
    }

/**
* @Author: nick
* @Description: uhf通电
* @DateTime: 2021-06-10 13:47
* @Params:
* @Return
*/
    fun openGPIO() {
        mReader?.startInventoryTag()
    }
/**
* @Author: nick
* @Description:断开供电
* @DateTime: 2021-06-10 14:01
* @Params:
* @Return
*/
    fun closeGPIO() {
       mReader?.stopInventory()
    }


/*
* @Author: nick
* @Description: 暴露接口工外部实现使用
* @DateTime: 2021-06-15 09:56
* @Params: 
* @Return 
*/
    fun registerCallback(callback:IBaseViewModel){
        mCallbacks.add(callback)
    }
    fun unRegisterCallback(){
        mCallbacks.clear()
    }

    fun setOnReaderResultLoaded(callBack:OnReaderResultLoaded){
        mCallback = callBack
    }

    interface OnReaderResultLoaded{
        fun onReaderResultLoaded()
        fun onReaderSingleResultLoaded(epc:String)
    }
}