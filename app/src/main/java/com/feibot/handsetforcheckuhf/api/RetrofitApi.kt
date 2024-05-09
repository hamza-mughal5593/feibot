package com.feibot.handsetforcheckuhf.api

import com.feibot.handsetforcheckuhf.Tools.TimeStamp
import com.feibot.handsetforcheckuhf.bean.PlayerInfo
import com.feibot.handsetforcheckuhf.bean.PlayerInfoForGlobal
import com.feibot.handsetforcheckuhf.bean.RegisterDevice
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitApi {

    //后台获取赛事配置文件
    //https://newtime.run8.cn/time/api/getEventConfigfile?machineId=" + machineId
    //EPC原始数据上传
    //https://newtime.run8.cn/time/api/cpvUpload2?machineId=H002&eventId=1024&epcNum=1&1=090202BD@21-06-17~13:55:49:53&t=74491411&sign=4282e3f1cbde2919f0353ef7ffdf8928
    //ZIP文件上传
    //https://newtime.run8.cn/time/api/fileUpload
    //获取该赛事全部人员信息
    //https://time.running8.com/api/send-bibs/?marathon_id=?&mode=get_all
    //测试网络连接
    //https://newtime.run8.cn/time/api/testConnection
    //网络请求签名
    //?t=74491411&sign=4282e3f1cbde2919f0353ef7ffdf8928

    //上传读取结果列表操作
    @GET("cpvUpload2")
    fun uploadReaderResult(@QueryMap params:Map<String,String>):Call<ResponseBody>

    //下载机器配置信息操作
    @GET("getEventConfigfile")
    fun downloadConfigFilePath(@Query("machineId") machineId:String):Call<ResponseBody>

    //获取赛事名单数据(国内服务)
    @GET("api/send-bibs")
    suspend fun getPlayerInfoList(@Query("marathon_id") raceId:String,@Query("mode") mode:String = "get_all"):PlayerInfo

    //获取赛事名单数据(国外服务)
    @GET("runners")
    suspend fun getPlayerInfoListForGlobal(@Query("race_id") raceId:String):PlayerInfoForGlobal

    //测试网络连接
    @GET("testConnection")
    fun testNetWorkConnection():Call<ResponseBody>

    //上传zip文件
    @Multipart
    @POST("fileUpload")
    fun uploadZipFile(@Part part: MultipartBody.Part):Call<ResponseBody>

    //注册设备
    @GET("device-info")
    fun registerDevice(@Query("macCode") macAddress:String,
                       @Query("t")t:String = "74491411",
                           @Query("sign") sign:String = "4282e3f1cbde2919f0353ef7ffdf8928"):Call<RegisterDevice>

    //验证版本
    @GET("softwareUpdatePDA3505.php")
    fun validateVersion(@Query("machineId") machineID:String,@Query("info") info:String = "version"):Call<ResponseBody>

    //下载安装包
    @Streaming
    @GET()
    fun downloadUpgradePackage(@Url url:String):Call<ResponseBody>

    //每隔15分钟上传一次设备信息
    @GET("testConnection")
    fun uploadMachineInfo(@Query("machineId") machineId:String,
                          @Query("batPercent") batPercent:String,
                          @Query("epcTotal") epcTotal:String,
                          @Query("epcDiff") epcDiff:String,
                          @Query("eventId") eventId:String,
                          @Query("reader1Working") reader1Working:String = "1",
                          @Query("reader2Working") reader2Working:String = "1",
                          @Query("machineTimeStamp") machineTimeStamp:String = TimeStamp.getCurrentMillisTime().toString(),
                          @Query("t")t:String = "74491411",
                          @Query("sign") sign:String = "4282e3f1cbde2919f0353ef7ffdf8928"):Call<ResponseBody>

}