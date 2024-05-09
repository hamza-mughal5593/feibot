package com.feibot.handsetforcheckuhf.view.setting

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Environment
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.feibot.handsetforcheckuhf.Tools.DataToMMKV
import com.feibot.handsetforcheckuhf.Tools.TimeStamp
import com.feibot.handsetforcheckuhf.api.RetrofitClient
import com.feibot.handsetforcheckuhf.api.retrofit
import com.feibot.handsetforcheckuhf.base.BaseApplication
import com.feibot.handsetforcheckuhf.bean.RegisterDevice
import com.feibot.handsetforcheckuhf.repository.PlayerInfoListRepository
import com.feibot.handsetforcheckuhf.services.UploadDataService
import com.feibot.handsetforcheckuhf.utils.*
import com.feibot.handsetforcheckuhf.view.detail.DetailFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager

/**":
 *
 *@Author: Nick
 *@Description:设置界面的ViewModel
 *@Date 2021-06-23: 11:42
 */
@SuppressLint("CommitPrefEdits")
class SettingFragmentViewModel:ViewModel() {
    private var mRaceID: String = ""
    val viewModelCaseState = MutableLiveData<Pair<CaseState,String>>()
    val viewModelReaderPowerValue = MutableLiveData<Int>()
    private var mDeviceID = ""
    private var mCSVFileName = ""
    private var mConfigFilePath:String? = null
    private lateinit var mShp:SharedPreferences
    private lateinit var mDetailFragmentViewModel:DetailFragmentViewModel

    fun initViewModel(requireActivity: FragmentActivity) {
        mDetailFragmentViewModel  =  ViewModelProvider(requireActivity).get(DetailFragmentViewModel::class.java)
    }
    private val mPlayerInfoListRepository by lazy { 
        PlayerInfoListRepository()
    }
/*
* @Author: nick
* @Description: 检查设备MAC地址是否与Xml相同
* @DateTime: 2021-06-23 11:15
* @Params:
* @Return
*/
    private fun checkDeviceMAC():Boolean{
        //加载Loading状态
        viewModelCaseState.postValue(Pair(CaseState.LOADING,""))
        //获取本机器的MAC地址
        val mac = Settings.Global.getString(BaseApplication.context().contentResolver,"WiFiMac")
        //val mac = "6C:15:24:81:41:04"
        //写入Shp里面
        mShp = BaseApplication.context().getSharedPreferences("deviceInfo",0)
        //获取配置文件的MAC地址
//        val macFromXml = ParserByPull
//            .getDevices(BaseApplication.context().assets.open("devices_mac_address_id.xml"))
        //循环判断是否一致
        if(mShp.getString("mac_address","") != mac){
            //加载Loading状态
            viewModelCaseState.postValue(Pair(CaseState.NEED_REGISTER,""))
            return false
        }
        return true
//        for(data in macFromXml){
//            if (data.mac == mac){
//                mDeviceID = data.id
//                //写入Shp里面
//                mShp = BaseApplication.context().getSharedPreferences("deviceInfo",0)
//                val editor = mShp.edit()
//                editor.putString("device_id",mDeviceID)
//                editor.apply()
//                //detailViewModel设置数据
//                mDetailFragmentViewModel.detailViewModelDeviceID.postValue(mDeviceID)
//                break
//            }
//        }
    }
/*
* @Author: nick
* @Description: 下载配置文件路径
* @DateTime: 2021-06-23 14:07
* @Params: 
* @Return 
*/
    fun downloadConfigPath(){
        if (!checkDeviceMAC()) return
        mDeviceID = mShp.getString("device_id","")!!
        val task = RetrofitClient.retrofitApi.downloadConfigFilePath(mDeviceID)
        task.enqueue(object :Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == HttpURLConnection.HTTP_OK){
                    mConfigFilePath = response.body()?.string()
                    if(mConfigFilePath != null){
                        LogUtils.d(this@SettingFragmentViewModel,"downloadConfigPath --->${call.request().url}--->$mConfigFilePath")
                        if(!mConfigFilePath!!.contains("http")){
                            viewModelCaseState.postValue(Pair(CaseState.NO_CHECK,"0"))
                        }else{
                            downloadConfigFile(mConfigFilePath!!)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                viewModelCaseState.postValue(Pair(CaseState.ERROR,"0"))
                t.printStackTrace()
            }
        })
    }
/*
* @Author: nick
* @Description: 下载配置文件
* @DateTime: 2021-06-24 16:49
* @Params:
* @Return
*/
    private fun downloadConfigFile(path:String){
        GlobalScope.launch(Dispatchers.IO) {


                val client =  OkHttpClient.Builder()
                    .sslSocketFactory(RetrofitClient.getSSLSocketFactory()!!,(RetrofitClient.trustAllCerts[0] as X509TrustManager))
                    .connectTimeout(10,TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder().url(path).build()
                val call = client.newCall(request)
            call.enqueue(object :okhttp3.Callback{
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    viewModelCaseState.postValue(Pair(CaseState.ERROR,"0"))
                    e.printStackTrace()
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.code == HttpURLConnection.HTTP_OK){
                        val inputStream = response.body?.byteStream()
                        val fileName = path.substring(path.lastIndexOf("/") + 1,path.length)
                        val filePath = BaseApplication.context().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        LogUtils.d(this@SettingFragmentViewModel,"写入文件中...目录:-->$filePath")
                        val file = File(filePath,fileName)
                        //执行存储文件操作
                        writeFile(inputStream,file,fileName)
                    }
                }
            })
        }
    }
/*
* @Author: nick
* @Description:写入配置文件文件操作
* @DateTime: 2021-06-24 17:01
* @Params:
* @Return
*/
    private fun writeFile(inputStream: InputStream?, file: File,fileName:String) {
        try {
            GlobalScope.launch(Dispatchers.IO) {


                val fop = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var len:Int
                while (((inputStream?.read(buffer)).also { len = it!! }) != -1){
                    fop.write(buffer,0,len)
                }
                fop.close()
                readFileToSharedPreferences(fileName)
                //更新Dialog状态
                viewModelCaseState.postValue(Pair(CaseState.SUCCESS,mRaceID))
            }
        }catch (e:WriteAbortedException){
            //更新Dialog状态
            viewModelCaseState.postValue(Pair(CaseState.ERROR,""))
            e.printStackTrace()
        }
    }
/*
* @Author: nick
* @Description: 读取文件存储shp
* @DateTime: 2021-06-25 14:23
* @Params:
* @Return
*/
    private fun readFileToSharedPreferences(fileName:String){
        val file = File(BaseApplication.context().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),fileName)
        var inputStream:FileInputStream? = null
        var stringBuilder:StringBuilder? = null
        try {
            //判断文件大小
            if (file.length() != 0L) {
                inputStream = FileInputStream(file)
                val streamReader = InputStreamReader(inputStream)
                val reader = BufferedReader(streamReader)
                var line: String?
                stringBuilder = StringBuilder()
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                reader.close()
                inputStream.close()
            }
            //文件写入读取完成之后写入shp
            var raceName:String           
            var racePosition:String
            //为变量赋值
            stringBuilder.toString().apply {
                raceName = substring(this.indexOf("eventName=") + 10, this.indexOf("eventId=")).trim()
                mRaceID = substring(this.indexOf("eventId=") + 8, this.indexOf("# 设备信息")).trim()
                racePosition = substring(this.indexOf("machinePosition=") + 16, this.indexOf("prio=")).trim()
                mCSVFileName = substring(this.indexOf("outputFileName=") + 15, this.indexOf("readerPower=")).trim()
            }
            //写入到shp中 并且 更新detailFragmentViewModel里面的数据
            val editor = mShp.edit()
            raceName.apply {
                editor.putString("race_name",this)
                viewModelScope.launch {
                    mDetailFragmentViewModel.detailViewModelRaceName.postValue(this@apply)
                }
            }
            mRaceID.apply {
                editor.putString("race_id",this)
                viewModelScope.launch {
                    mDetailFragmentViewModel.detailViewModelRaceID.postValue(this@apply)
                }
            }
            racePosition.apply {
                editor.putString("race_position",this)
                viewModelScope.launch {
                    mDetailFragmentViewModel.detailViewModelRacePosition.postValue(this@apply)
                }
            }
            //上传Csv文件的名称
            editor.putString("csv_file_name",mCSVFileName)
            editor.apply()
            //为上传Service类设置信息
            UploadDataService.getInstance().setEventID(mRaceID)
            UploadDataService.getInstance().setMachineID(mDeviceID)

        } catch (e: Exception) {
            e.printStackTrace()
            //更新Dialog状态
            viewModelCaseState.postValue(Pair(CaseState.ERROR,""))
        }finally {
            stringBuilder?.clear()
        }
    }
/*
* @Author: nick
* @Description: 设置时间 时:分:秒 并保存 Shp
* @DateTime: 2021-06-30 14:41
* @Params: 
* @Return 
*/
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @SuppressLint("SimpleDateFormat")
    fun setTime(timeMap: HashMap<String, Int>) {
        val year = timeMap["year"].toString()

        var month = timeMap["month"].toString()
        if(month.length < 2){
            month = "0$month"
        }

        var day = timeMap["day"].toString()
        if(day.length < 2){
            day = "0$day"
        }

        var hour = timeMap["hour"].toString()
        if(hour.length < 2){
            hour = "0$hour"
        }

        var minute =   timeMap["minute"].toString()
        if(minute.length < 2){
            minute = "0$minute"
        }

        var second =   timeMap["second"].toString()
        if(second.length < 2){
            second = "0$second"
        }

        var milliSecond =   timeMap["millisecond"].toString()
        if(milliSecond.length < 2){
            milliSecond = "00$hour"
        }else if (milliSecond.length  == 2){
            milliSecond = "0$hour"
        }
        //拿到修改后时间的Date格式的数据
        val result ="$year-$month-$day $hour:$minute:$second:$milliSecond"

        //声明一个时间格式转换器
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
        val simpleDateFormatYearOfDay = SimpleDateFormat("yyyy-MM-dd")

        //转换修改时间格式为毫秒级别数据 unix时间
        val changedTimeMillis = simpleDateFormat.parse(result).time

        //获取到一个本地的时间unix格式的 yyyy-mm-dd hh:mm:ss 的unix时间
        val currentTimeMillis = System.currentTimeMillis()
        val currentTimeDateYearOfDay = simpleDateFormat.format(currentTimeMillis)
        val currentTimeMillisYearOfDay = currentTimeMillis - simpleDateFormat.parse(currentTimeDateYearOfDay).time

        //本地时间与系统时间的差值 并保存到 shp
        val offTime =  currentTimeMillisYearOfDay + changedTimeMillis - currentTimeMillis

        //为时间戳工具设置变化差值
        TimeStamp.changeMillis = changedTimeMillis
        TimeStamp.offTime = offTime

        //保存差值到Shp中
        mShp = BaseApplication.context().getSharedPreferences("deviceInfo",0)
        val edit = mShp.edit()
        edit.putLong("off_time",offTime)
        edit.apply()
        LogUtils.d(this,"offTime ---> $offTime")
    }
/*
* @Author: nick
* @Description: 设置读卡器功率 保存至Shp中
* @DateTime: 2021-07-03 17:30
* @Params: 
* @Return 
*/
    fun setReaderValue(value: Int) {
        mShp = BaseApplication.context().getSharedPreferences("deviceInfo",0)
        val edit = mShp.edit()
        edit.putInt("reader_power",value)
        edit.apply()
    }
    
/*
* @Author: nick
* @Description: 下载国内版赛事名单
* @DateTime: 2021-07-01 15:42
* @Params: 
* @Return 
*/
    fun downloadPlayerInfoList(raceID:String){
        if(mRaceID == ""){
            mRaceID = raceID
        }

        viewModelCaseState.postValue(Pair(CaseState.DOWNLOAD_PLAYER_INFO_LOADING,""))
        viewModelScope.launch {
            try {
                val list = mPlayerInfoListRepository.getPlayerInfoList(mRaceID)
                LogUtils.d(this@SettingFragmentViewModel,"获取到了${list.runner.size}条数据!")
                //清除历史数据
                DataToMMKV().deleteHistoryData()
                DataToMMKV().setPlayerInfoToMMKV(raceID.toInt(),list)
                //设置返回的列表数量
                viewModelCaseState.postValue(Pair(CaseState.DOWNLOAD_PLAYER_INFO_SUCCESS,list.runner.size.toString()))
            }catch (e:java.lang.Exception){
                if (e is NullPointerException){
                    viewModelCaseState.postValue(Pair(CaseState.DOWNLOAD_PLAYER_INFO_EMPTY,""))
                    LogUtils.d(this@SettingFragmentViewModel,"下载名单为空.")
                }else{
                    viewModelCaseState.postValue(Pair(CaseState.DOWNLOAD_PLAYER_INFO_ERROR,""))
                    e.printStackTrace()
                }
            }
        }
    }
/*
* @Author: nick
* @Description: 获取国外版的列表数据
* @DateTime: 2021-07-22 18:35
* @Params: 
* @Return 
*/
    fun downloadGlobalPlayerInfoList(raceID:String) {
        if(mRaceID == ""){
            mRaceID = raceID
        }
        viewModelCaseState.postValue(Pair(CaseState.DOWNLOAD_PLAYER_INFO_LOADING,""))
        viewModelScope.launch {
            try {
                val mList = mPlayerInfoListRepository.getPlayerInfoListForGlobal(mRaceID)
                if(mList.runners.isEmpty()) {
                    viewModelCaseState.postValue(Pair(CaseState.DOWNLOAD_PLAYER_INFO_EMPTY,""))
                    LogUtils.d(this@SettingFragmentViewModel,"下载名单为空.")
                    return@launch
                }
                LogUtils.d(this@SettingFragmentViewModel,"获取到了${mList.runners.size}条数据!")
                DataToMMKV().setGlobalPlayerInfoToMMKV(mRaceID.toInt(),mList)
                //设置返回的列表数量
                viewModelCaseState.postValue(Pair(CaseState.DOWNLOAD_PLAYER_INFO_SUCCESS,mList.runners.size.toString()))
            }catch (e:java.lang.Exception){
                if (e is NullPointerException){
                    viewModelCaseState.postValue(Pair(CaseState.DOWNLOAD_PLAYER_INFO_EMPTY,""))
                    LogUtils.d(this@SettingFragmentViewModel,"下载名单为空.")
                }else{
                    viewModelCaseState.postValue(Pair(CaseState.DOWNLOAD_PLAYER_INFO_ERROR,""))
                    e.printStackTrace()
                }
            }
        }
    }
/*
* @Author: nick
* @Description: 注册设备
* @DateTime: 2021-08-12 09:53
* @Params:
* @Return
*/
    fun registerMachine() {
        //写入Shp里面
        mShp = BaseApplication.context().getSharedPreferences("deviceInfo", 0)
        //加载Loading状态 注册中...
        viewModelCaseState.postValue(Pair(CaseState.REGISTERING, ""))
        //获取本设备的MAC地址
        val mac = Settings.Global.getString(BaseApplication.context().contentResolver,"WiFiMac")

        //网络查询是否有MAC地址记录
        try {
            GlobalScope.launch(Dispatchers.IO) {


                retrofit<RegisterDevice> {
                    api = RetrofitClient.retrofitApi.registerDevice(mac)
                    onSuccess { registerDevice, call ->
                        if (registerDevice.code == "ok") {
                            LogUtils.d(this@SettingFragmentViewModel,"${call.request().url}")
                            mDeviceID = registerDevice.machine.name_no
                            mShp.edit().putString("device_id", mDeviceID).apply()
                            //detailViewModel设置数据
                            mDetailFragmentViewModel.detailViewModelDeviceID.postValue(mDeviceID)
                            mShp.edit().putString("mac_address", mac).apply()
                            viewModelCaseState.postValue(Pair(CaseState.REGISTER_SUCCESS, ""))
                        }
                    }
                    onFail { msg, errorCode ->
                        viewModelCaseState.postValue(Pair(CaseState.REGISTER_FAIL, ""))
                    }
                }
            }
        }catch (e:java.lang.Exception){
            e.printStackTrace()
            viewModelCaseState.postValue(Pair(CaseState.REGISTER_FAIL, ""))
        }
    }

}