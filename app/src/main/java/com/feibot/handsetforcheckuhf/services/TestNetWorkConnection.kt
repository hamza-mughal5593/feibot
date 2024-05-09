package com.feibot.handsetforcheckuhf.services

import com.feibot.handsetforcheckuhf.api.RetrofitClient
import com.feibot.handsetforcheckuhf.api.retrofit
import com.feibot.handsetforcheckuhf.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

/**
 *@Author: Nick
 *@Description:检测网络状态的工具类
 *@Date 2021-07-06: 11:50
 */
object TestNetWorkConnection {
    private var mIConnection = 0
    private fun connection(){
        GlobalScope.launch{
            retrofit<ResponseBody> {
                api = RetrofitClient.retrofitApi.testNetWorkConnection()
                onSuccess {_,_ ->
                    this@TestNetWorkConnection.mIConnection = 1
                    LogUtils.d(this@TestNetWorkConnection,"网络连接成功:$mIConnection")
                }
                onFail { _, _ ->
                    this@TestNetWorkConnection.mIConnection = 0
                    LogUtils.d(this@TestNetWorkConnection,"网络连接失败:$mIConnection")
                }
            }
        }
    }
    //异步获取网络连接状态
    fun getConnectionAsyncResult():Int{
        connection()
        LogUtils.d(this@TestNetWorkConnection,"网络连接状态:$mIConnection")
        return mIConnection
    }
    //同步获取网络连接状态
    fun getConnectSyncResult():Boolean{
        val task = RetrofitClient.retrofitApi.testNetWorkConnection()
        return task.execute().isSuccessful
    }
}