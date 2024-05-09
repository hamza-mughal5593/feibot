package com.feibot.handsetforcheckuhf.base

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Environment
import com.feibot.handsetforcheckuhf.BuildConfig
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.tencent.mmkv.MMKV
import com.xuexiang.xupdate.XUpdate
import java.io.File


/**
 *@Author: Nick
 *@Description:应用基类 返回Context
 *@Date 2021-06-10: 09:23
 */
class BaseApplication:Application(){
    companion object{
        private lateinit var appContext:Context
        fun context():Context{
            return appContext
        }
    }

    override fun onCreate() {
        appContext = baseContext
        //初始化一个MMKV
        val mmkvPath = this.filesDir.absolutePath + "/mmkv"
        LogUtils.d(this,"MMKV files DIR is --> $mmkvPath")
        MMKV.initialize(mmkvPath)
        //删除之前的升级包
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),"${BuildConfig.VERSION_NAME}.apk")
        if(file.exists()){
            file.delete()
        }
        super.onCreate()
    }
}


