package com.feibot.handsetforcheckuhf.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.feibot.handsetforcheckuhf.api.RetrofitClient
import com.feibot.handsetforcheckuhf.api.retrofit
import com.feibot.handsetforcheckuhf.utils.CaseState
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.feibot.handsetforcheckuhf.utils.MachineInfoUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UploadMachineInfoWorker(context: Context, workerParams: WorkerParameters):Worker(context, workerParams){
    override fun doWork(): Result {
        //uploadTask()
        return Result.success()
    }
    //上传设备信息
    private fun uploadTask(){
        GlobalScope.launch (Dispatchers.IO){
            retrofit<ResponseBody> {
                api = RetrofitClient.retrofitApi.uploadMachineInfo(
                    machineId = MachineInfoUtil.machineId,
                    batPercent = MachineInfoUtil.battery,
                    epcTotal = MachineInfoUtil.totalTagSize,
                    epcDiff = MachineInfoUtil.diffTagSize,
                    eventId = MachineInfoUtil.eventId)
                onSuccess { body, call ->
                    if(body.string().equals("ok")){
                        LogUtils.d(this,"上传设备信息成功")
                    }
                }
                onFail { msg, errorCode ->
                    LogUtils.d(this,"上传设备信息失败")
                    LogUtils.d(this,msg)
                }
            }
        }
    }

}