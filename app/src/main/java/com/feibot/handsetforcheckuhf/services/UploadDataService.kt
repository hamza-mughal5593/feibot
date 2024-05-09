package com.feibot.handsetforcheckuhf.services

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.feibot.handsetforcheckuhf.api.RetrofitClient
import com.feibot.handsetforcheckuhf.api.retrofit
import com.feibot.handsetforcheckuhf.base.BaseApplication
import com.feibot.handsetforcheckuhf.bean.Epc
import com.feibot.handsetforcheckuhf.contants.Config
import com.feibot.handsetforcheckuhf.repository.data.AppDataBase
import com.feibot.handsetforcheckuhf.repository.data.entity.EpcEntity
import com.feibot.handsetforcheckuhf.utils.CaseState
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.feibot.handsetforcheckuhf.utils.MMKVUtils
import com.feibot.handsetforcheckuhf.utils.MachineInfoUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import ir.mahdi.mzip.zip.ZipArchive
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.util.*
import kotlin.math.log

/**
 *@Author: Nick
 *@Description: 上传数据的服务类
 *@Date 2021-06-17: 16:10
 */
class UploadDataService:Service(){
    private var job: Job? = null
    private var timeTask: Job? = null
    private var mMachineID:String? = null
    private var mEventID:String? = null
    private var mShp:SharedPreferences? = null
    private lateinit var mDataBase:AppDataBase
    private lateinit var mMMKVUtils: MMKVUtils
    private var mRaceID = ""
    private val mUploadTempList = arrayListOf<Epc>()
    //声明一个临时获取的列表
    private val mTempList = arrayListOf<Epc>()
    //声明一个临时的时间标签
    private var mTempTime = 0L
    //声明一个余数上传标志
    private var mOtherEpcUploadFlag = false
    //声明一个上传的次数计数器
    private var mUploadTimes = 1
/*
* @Author: nick
* @Description: 单例模式
* @DateTime: 2021-06-28 15:28
* @Params: 
* @Return 
*/
    companion object{
        private var mCallback:OnResultUploaded? = null
        private var mInstance:UploadDataService? = null
        get() {
            if (field == null){
                field = UploadDataService()
            }
            return field
        }
        @Synchronized
        fun getInstance():UploadDataService{
            return mInstance!!
        }
    }
/*
* @Author: nick
* @Description: 返回绑定任务
* @DateTime: 2021-06-28 15:29
* @Params: 
* @Return 
*/
    override fun onBind(p0: Intent?): IBinder{
        mShp = BaseApplication.context().getSharedPreferences("deviceInfo",0)
        mDataBase = AppDataBase.getInstance()
        mMMKVUtils = MMKVUtils.getInstance()
        //开始计时任务判断有未传的数据
        startTimerTask()
        return Bind()
    }

/*
* @Author: nick
* @Description: 上传扫描结果List
* @DateTime: 2021-06-17 16:26
* @Params: 
* @Return 
*/
    fun uploadReadResult(data:Epc,fromDB:Boolean){
        //如果没有设备编号 直接返回
        val deviceID = mShp?.getString("device_id","")!!
        val raceID = mShp?.getString("race_id","")!!
        if(deviceID == "" || raceID == "") return
        mUploadTempList.add(data)
        //大于默认50条和上传次数乘机之后上传
        if(mUploadTempList.size >= (Config.UPLOAD_DIFF_SIZE * mUploadTimes)){
            LogUtils.d(this,"50条上传一次!")
            loadedUploadData(mUploadTimes,false,fromDB)
        }
    }
/*
* @Author: nick
* @Description: 准备上传EPC的数据
* @DateTime: 2021-07-19 14:00
* @Params: times：上传次数，remainder：是否余数
* @Return
*/
    private fun loadedUploadData(times:Int,remainder:Boolean,fromDB:Boolean){
        //空数组直接返回
        if (mUploadTempList.isNullOrEmpty()) return

        //过滤结果列表的范围 赋值一个新的缓存列表
        var array:List<Epc>?
        //组装列表的格式
        var map = hashMapOf<String,String>()
        //判断是否是余数上传
        if(remainder){
            //余数上传 下标大于上次的最后上传下标
            array = mUploadTempList.filterIndexed { index, _ ->
                index >= (times - 1) * Config.UPLOAD_DIFF_SIZE
            }
            //清空读取的总列表
            mUploadTempList.clear()
            // 上传次数计数器重置
            mUploadTimes = 1
        }else{
            //不是余数上传
            //上传次数计数器 + 1
            mUploadTimes += 1
            array = mUploadTempList.filterIndexed { index, _ ->
                index in (times - 1) * Config.UPLOAD_DIFF_SIZE  until (times * Config.UPLOAD_DIFF_SIZE)
            }
        }
        //取shp的信息
        if (mMachineID.isNullOrEmpty() && mEventID.isNullOrEmpty()){
            mRaceID = mShp?.getString("race_id","")!!
            map["machineId"] = mShp?.getString("device_id","")!!
            map["eventId"] = mRaceID
        }else{
            //取配置文件的信息
            mRaceID = mEventID!!
            map["machineId"] = mMachineID!!
            map["eventId"] = mRaceID
        }
        map["epcNum"] = array.size.toString()
        //循环收集的数据
        for((index,epc) in array.withIndex()){
            map[(index + 1).toString()] = "${epc.epc}@${epc.timeStamp}"
        }
        map["t"] = Config.REQUEST_NO
        map["sign"] = Config.REQUEST_SIGN

        //执行上传任务
        handleUploadEpcListTask(map,array,fromDB)

        //清空缓存的读取列表
        array = null
        map = hashMapOf()

        //记录时间标签
        mTempTime = System.currentTimeMillis()
    }
/*
* @Author: nick
* @Description: 处理网络上传Epc任务
* @DateTime: 2021-06-18 09:15
* @Params: 
* @Return 
*/
    private fun handleUploadEpcListTask(map:MutableMap<String,String>,epcList:List<Epc>,fromDB:Boolean){
        //开启协程扩展方法 网络操作耗时操作 已经处于IO线程
        GlobalScope.launch(Dispatchers.IO){


            //默认延迟秒数上传
          //  delay(Config.UPLOAD_LIST_TIME)
            //retrofit开始上传
             retrofit<ResponseBody> {
                 api = RetrofitClient.uploadRetrofitApi.uploadReaderResult(map)
                 onSuccess{ _ ,_ ->
                     //记录已经上传的列表通知到ViewModel
                     handleUploadListForViewModel(epcList)
                 }
                 //失败的时候把数据写入到数据库里
                 onUploadFail { _, _ ->
                     val epcEntity = arrayListOf<EpcEntity>()
                     //判断列表是否为空
                     if(epcList.isNotEmpty()){
                         for (data in epcList) {
                             val entity = EpcEntity(
                                 0,
                                 data.epc,
                                 "1",
                                 mRaceID,
                                 data.bibNumber,
                                 data.timeStamp,
                                 0,
                                 data.timeStamp,
                                 ""
                             )
                             epcEntity.add(entity)
                         }
                         //是否是从读取数据库后上传
                         if(!fromDB){
                             mDataBase.epcDao()?.updateEpcListUploadState(epcEntity)
                             //数据库执行写入操作
                             LogUtils.d(this@UploadDataService, "向数据库递交数据，${epcList.size}条!")
                         }
                     }
                     //清空缓存
                     epcEntity.clear()
                 }
             }
        }
    }
/*
* @Author: nick
* @Description: 处理已经上传的列表通知到ViewModel
* @DateTime: 2021-06-18 11:22
* @Params: 
* @Return 
*/
    private fun handleUploadListForViewModel(data: List<Epc>) {
        mCallback?.onResultLoaded(data)
    }

/*
* @Author: nick
* @Description: 上传CSV-Zip文件
* @DateTime: 2021-06-17 16:26
* @Params:
* @Return
*/
@RequiresApi(Build.VERSION_CODES.KITKAT)
fun uploadCSV(){
        mCallback?.onUploadFileCase(CaseState.UPLOAD_LOADING)
        //打包一下csv
        val file  = zipCSVFile()
        if(file.exists()){
            try {
                GlobalScope.launch {
                    retrofit<ResponseBody> {
                        val part = MultipartBody
                            .Part
                            .createFormData("myFile",
                                file.name,
                                file.asRequestBody("multipart/form-data".toMediaType()))
                        api = RetrofitClient.uploadCSVRetrofitApi.uploadZipFile(part)
                        onSuccess {it,call ->
                            if(it.string() == "upload successful"){
                                LogUtils.d(this@UploadDataService,"CVS上传成功！-->${it.string()}-->call-path(${call.request().url})")
                                mCallback?.onUploadFileCase(CaseState.UPLOAD_SUCCESS)
                            }
                        }
                        onFail { msg, code ->
                            LogUtils.d(this@UploadDataService,msg)
                            mCallback?.onUploadFileCase(CaseState.UPLOAD_FAIL)
                        }
                    }
                }
            }catch (e:IOException){
                mCallback?.onUploadFileCase(CaseState.UPLOAD_ERROR)
                e.printStackTrace()
            }
        }
    }
    
/*
* @Author: nick
* @Description: 打包zip的CSV文件
* @DateTime: 2021-07-15 13:53
* @Params: 
* @Return 
*/
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun zipCSVFile():File{
        val fileName = mShp?.getString("csv_file_name","")
        val zipFileName = fileName?.replace(".csv",".csv.zip").toString()
        val fileDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        //zip打包
        GlobalScope.launch(Dispatchers.IO) {


            ZipArchive.zip("$fileDir/$fileName","$fileDir/$zipFileName","")
        }
         return File("$fileDir/$zipFileName")
    }


/*
* @Author: nick
* @Description: 服务的执行任务
* @DateTime: 2021-06-17 16:25
* @Params: 
* @Return 
*/
    inner class Bind:Binder(){
        fun uploadReaderResult(epc:Epc,fromDB:Boolean){
            this@UploadDataService.uploadReadResult(epc,fromDB)
        }
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        fun uploadCSV(){
            this@UploadDataService.uploadCSV()
        }
        fun manualUploadEpcList(){
            this@UploadDataService.manualUploadEpcList()
        }

}
/*
* @Author: nick
* @Description: 手动上传Epclist
* @DateTime: 2021-10-08 11:16
* @Params: 
* @Return 
*/
    private fun manualUploadEpcList() {
        loadedUploadData(mUploadTimes,true,false)
    }

/*
* @Author: nick
* @Description: 暴露读取结果接口供外部使用
* @DateTime: 2021-06-21 10:27
* @Params:
* @Return
*/
    fun setOnResultUploaded(callback:OnResultUploaded){
        mCallback = callback
    }

    interface OnResultUploaded{
        fun onResultLoaded(epc:List<Epc>)
        fun onUploadFileCase(caseState: CaseState)
    }
/*
* @Author: nick
* @Description: 外部设置设备ID
* @DateTime: 2021-06-28 15:30
* @Params: 
* @Return 
*/
    fun setMachineID(machineID:String){
         mMachineID = machineID
    }
/*
* @Author: nick
* @Description: 外部设置赛事ID
* @DateTime: 2021-06-28 15:31
* @Params: 
* @Return 
*/    
    fun setEventID(eventID:String){
        mEventID = eventID
    }

/*
* @Author: nick
* @Description: 开启一个时间定时器 用于进行余数上传
* @DateTime: 2021-07-19 14:21
* @Params: 
* @Return 
*/
    private fun startTimerTask(){
        if (timeTask != null){
            timeTask?.start()
            return
        }
        timeTask = GlobalScope.launch(Dispatchers.IO){
            try {
                do {
                    delay(60000)
                    //距离上次标签的时间超过30秒后
                    if(System.currentTimeMillis() - mTempTime > 30000){
                        LogUtils.d(this@UploadDataService,"余数上传 时间间隔是 ${System.currentTimeMillis() - mTempTime}")
                        LogUtils.d(this@UploadDataService,"上传次数是 $mUploadTimes")
                        LogUtils.d(this@UploadDataService,"读取列表的数量是 ${mUploadTempList.size} 条！")
                        //进行余数上传
                        loadedUploadData(mUploadTimes,true,false)
                        //上传一次设备信息
                        uploadMachineInfoTask()
                    }
                }while (true)
            }catch (e:IOException){
                e.printStackTrace()
            }
        }
    }
/**
*
* 上传设备信息
* @date 2022/9/23 14:45
* @params  * @param null
* @return
* @author nick
*/
    private fun uploadMachineInfoTask(){
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
/*
* @Author: nick
* @Description: 停止判断读卡器剩余结果计时器
* @DateTime: 2021-07-19 14:22
* @Params: 
* @Return 
*/
    fun stopTimerTask(){
        timeTask?.cancel()
    }
}