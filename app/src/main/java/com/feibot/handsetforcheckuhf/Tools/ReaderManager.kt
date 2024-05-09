package com.feibot.handsetforcheckuhf.Tools

import android.annotation.SuppressLint
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.rscja.deviceapi.RFIDWithUHFUART
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object ReaderManager {
    @SuppressLint("StaticFieldLeak")
    private var mReader: RFIDWithUHFUART? = null
    init {
        mReader = RFIDWithUHFUART.getInstance()
    }
    fun getReader():RFIDWithUHFUART?{
        return mReader
    }
    fun initReader(){
        try {
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
    }
}