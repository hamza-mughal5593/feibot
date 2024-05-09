package com.feibot.handsetforcheckuhf.view.main

import android.annotation.SuppressLint
import android.content.*
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feibot.handsetforcheckuhf.Tools.ReadTagToolPlus
import com.feibot.handsetforcheckuhf.Tools.TimeStamp
import com.feibot.handsetforcheckuhf.base.BaseApplication
import com.feibot.handsetforcheckuhf.bean.Epc
import com.feibot.handsetforcheckuhf.repository.ReaderResultRepository
import com.feibot.handsetforcheckuhf.repository.data.AppDataBase
import com.feibot.handsetforcheckuhf.repository.data.entity.EpcEntity
import com.feibot.handsetforcheckuhf.services.ReaderResultToCSV
import com.feibot.handsetforcheckuhf.services.UploadDataService
import com.feibot.handsetforcheckuhf.utils.CaseState
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.feibot.handsetforcheckuhf.utils.MMKVUtils
import com.feibot.handsetforcheckuhf.utils.MachineInfoUtil
import com.xuexiang.xupdate.service.DownloadService
import com.xuexiang.xupdate.service.DownloadService.bindService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log


/**
 *@Author: Nick
 *@Description:主界面的ViewModel
 *@Date 2021-06-10: 09:48
 */
@SuppressLint("StaticFieldLeak")
class MainFragmentViewModel :ViewModel(){
    private  var job: Job? = null

    //声明读卡器工具类
    private lateinit var mReadTagTool:ReadTagToolPlus
    //声明列表上传服务类
    private  var mUploadDataService:UploadDataService? = null
    //读卡器读出后的结果
    private val mEpcList = arrayListOf<Epc>()
    //网络已经上传的标签数量
    private var mUploadEpcListSize = 0
    //读卡器读出的标签数量
    private var mLoadedEpcSize = 0
    //懒加载Repository
    private val readerResultRepository by lazy {
        ReaderResultRepository()
    }
    //上传服务类Binder
    private var mServiceBinder:UploadDataService.Bind? = null
    //上传服务类开启标识
    private var mUploadServiceBindState = false
    //上传服务类连接对象
    private lateinit var mConnection:ServiceConnection
    //声明一个LiveData读卡器读取结果
    val viewModelReaderResult = MutableLiveData<ArrayList<Epc>>()
    //声明一个LiveData网络网络已经上传的列表
    val viewModelLoadedList = MutableLiveData<ArrayList<Epc>>()
    //声明一个LiveData网络网络已经上传的列表数量
    val viewModelLoadedListSize = MutableLiveData<Int>()
    //声明一个LiveData网络网络还未上传的列表
    val viewModelUnLoadedList = MutableLiveData<Int>()
    //声明一个LiveData读卡器开关的标志
    val viewModelReaderSwitchFlag = MutableLiveData<Boolean>()
    //声明一个LiveData观察上传状态
    val viewModelUploadFileCase = MutableLiveData<CaseState>()
    //声明一个msp
    private var mShp:SharedPreferences? = null
    //声明一个mmkv工具类
    private var mMMKVUtils:MMKVUtils? = null
    //声明一个赛事ID
    private var mRaceID = ""
    //设置读卡器开关
    private var mReaderPowerOn = false
    //声明数据库一个实例
    private lateinit var mDataBase:AppDataBase
    //定时上传CSV的Timer
    private var mUploadCSVTimerTask: TimerTask? = null
    //定时一个上传遗留的时间任务
    private var mUnUploadTimerTask: TimerTask? = null
    private var mTimer: Timer?= null
    //声明一个读取Epc计数器
    private var mReaderResultSize = 0
/*
* @Author: nick
* @Description: 加载工具类
* @DateTime: 2021-06-21 14:49
* @Params:
* @Return
*/
    fun initTools(){
        //拿到读卡器的实例
        mReadTagTool = ReadTagToolPlus.getInstance()
        //上传服务类
        mUploadDataService = UploadDataService()
        //读卡器读取结果 接口实现
        mReadTagTool.setOnReaderResultLoaded(object :ReadTagToolPlus.OnReaderResultLoaded{
            //多次读取结果
            override fun onReaderResultLoaded() {
                this@MainFragmentViewModel.onReaderResultLoaded()
            }
            //单次读取结果
            override fun onReaderSingleResultLoaded(epc: String) {
                //TODO("Not yet implemented")
            }
        })
        //服务类上传结果 接口实现
        mUploadDataService?.setOnResultUploaded(object :UploadDataService.OnResultUploaded{
            //当上传的结果回来后
            override fun onResultLoaded(epc: List<Epc>) {
                setUploadedList(epc)
            }
            //当上传CSV文件的结果回来后
            override fun onUploadFileCase(caseState: CaseState) {
                viewModelUploadFileCase.postValue(caseState)
            }
        })
        //mmkv工具类赋值
        mMMKVUtils = MMKVUtils.getInstance()
        //shp赋值
        mShp = BaseApplication.context().getSharedPreferences("deviceInfo",0)
        mRaceID = mShp?.getString("race_id","")!!
        //数据库读取未上传的标签
        mDataBase = AppDataBase.getInstance()
        //开始上传CSV任务计划
        startTimerTask()
    }
/*
* @Author: nick
* @Description: 数据库获取未上传的列表
* @DateTime: 2021-07-06 15:14
* @Params:
* @Return
*/
    fun getDataBaseUnUploadList(){
        //超过1=000条删除前5000条数据
        val unUploadSize = mDataBase.epcDao()?.countByRecords()
        if(unUploadSize != null && unUploadSize > 10000){
            mDataBase.epcDao()?.deleteMaxSizeRecords()
        }
        //从数据库获取未上传数据(单项)
        val list = mDataBase.epcDao()?.getUnUploadListByFlag(mRaceID,0)
        //从数据库获取未上传数据(累加count列表)
        val unUploadByUnRepeatList = mDataBase.epcDao()?.getUnUploadListByFlagAndGroupBy(mRaceID)
        //判断列表的数量并且上传
        if (!list.isNullOrEmpty() && !unUploadByUnRepeatList.isNullOrEmpty()) {
            //遍历列表逐条上传
            for(data in list){
                mServiceBinder!!.uploadReaderResult(Epc(data!!.id,data.epc,data.count.toInt(), data.create_time,data.bib_number),true)
            }
            //前台显示列表数
//            for(data in unUploadByUnRepeatList){
//                mEpcList.add(Epc(data!!.id,data.epc,data.count.toInt(),data.create_time,data.bib_number))
//            }
            //待传列表数量
            mLoadedEpcSize = list.size
            //不同标签数列表
            mReaderResultSize = unUploadByUnRepeatList.size
            //更新UI显示
            onReaderResultLoaded()
        }else{
            //没有未上传的数据 即可删除数据库历史数据
            mDataBase.epcDao()?.deleteAll()
        }
    }

/*
* @Author: nick
* @Description:开始读卡
* @DateTime: 2021-06-21 10:24
* @Params: 
* @Return 
*/
    fun startReadCard() {
        mReadTagTool.wakeReader()
        viewModelReaderSwitchFlag.postValue(false)
    }
/*
* @Author: nick
* @Description:停止读卡
* @DateTime: 2021-06-21 10:24
* @Params:
* @Return
*/
    fun stopReadCard() {
        viewModelReaderSwitchFlag.postValue(true)
        mReadTagTool.pauseReader()
    }

/*
* @Author: nick
* @Description: 删除读卡器彻底停止读卡器活动 停止上传的服务计时器
* @DateTime: 2021-06-22 15:04
* @Params: 
* @Return 
*/
    fun destroyReader(){
        mReadTagTool.closeReader()
        UploadDataService.getInstance().stopTimerTask()
    }

/*
* @Author: nick
* @Description: ReadTagTool接口实现类 为liveData设置数据 判断时间戳规则 判断已经上传的列表
* @DateTime: 2021-06-15 10:40
* @Params: 
* @Return 
*/
    fun onReaderResultLoaded() {
        //判断读取列表列表新增或者更新 并且上传
        handleIsUploadEpcList()
        viewModelUnLoadedList.postValue(mLoadedEpcSize - mUploadEpcListSize)
        viewModelReaderResult.postValue(mEpcList)
        //更新读取结果计数器
        viewModelLoadedListSize.postValue(mReaderResultSize)
        //设置上传的读取总数时间
        MachineInfoUtil.totalTagSize = mReaderResultSize.toString()
    }

/*
* @Author: nick
* @Description: 只上传新读取的数据即第一条数据
* @DateTime: 2021-06-18 11:48
* @Params:
* @Return
*/
    private fun handleIsUploadEpcList() {
        val epc = readerResultRepository.getReaderResult()
        //没有数据返回
        if (epc.isEmpty()) return
        val epcTimestamp =   TimeStamp.getMillis(0L,4).toString()
        val csvTimestamp =   TimeStamp.getMillis(0L,7).toString()
        //判断列表是否为空 添加第一条数据
        if(mEpcList.isEmpty()){
            //添加Epc元素
            val epcFlag = Epc(1,
                epc,
                1,
                epcTimestamp,
                mMMKVUtils?.decodeString("${mRaceID}_${epc}_bib")!!)
            //添加到已经读到的总列表
            mEpcList.add(epcFlag)
            //添加已经读到的总列表数量
            mLoadedEpcSize += 1
            //单次上传标签
            mServiceBinder?.uploadReaderResult(epcFlag,false)
            //写入文件流操作
            ReaderResultToCSV.getInstance().setDataToFile(epc,csvTimestamp)
            //不同标签数累加
            mReaderResultSize += 1
        }else{
        //相同标签更新Count,时间;
            for((index, _) in mEpcList.withIndex()){
                //相同Epc修改count和时间
                if(epc == mEpcList[index].epc){
                    //修改id
                    mEpcList[index].id = index + 1
                    //修改count数量
                    mEpcList[index].count += 1
                    //修改时间戳
                    mEpcList[index].timeStamp = epcTimestamp
                    //上传标签
                    mServiceBinder?.uploadReaderResult(mEpcList[index],false)
                    //写入文件流操作
                    ReaderResultToCSV.getInstance().setDataToFile(epc,csvTimestamp)
                    //添加已经读到的总列表数量
                    mLoadedEpcSize += 1
                    break
                    ////////////以下为添加新标签//////////////
                } else if(index == mEpcList.size - 1){
                    val epcFlag = Epc(index + 1,
                        epc,
                        1,
                        epcTimestamp,
                        mMMKVUtils?.decodeString("${mRaceID}_${epc}_bib")!!)
                    mEpcList.add(epcFlag)
                    //单次上传标签
                    mServiceBinder?.uploadReaderResult(epcFlag,false)
                    //写入文件流操作
                    ReaderResultToCSV.getInstance().setDataToFile(epc,csvTimestamp)
                    //添加已经读到的总列表数量
                    mLoadedEpcSize += 1
                    //不同标签数累加
                    mReaderResultSize += 1
                }
            }
        }
    }
/*
* @Author: nick
* @Description: 上传完毕的列表结果进行处理 数据库更新标识
* @DateTime: 2021-06-18 11:17
* @Params:
* @Return
*/
    private fun setUploadedList(list:List<Epc>){
        //空数组直接返回
        if(list.isNullOrEmpty()) return

        //因为多线程处理 引用问题 所以要重新赋值一个新列表
        val tempList = arrayListOf<Epc>()
            tempList.addAll(list)

        //声明一个数据库epcEntity列表对象
        val epcEntity = arrayListOf<EpcEntity>()
            //数据库更新状态 操作 已经上传的标签upload_flag = 1
            GlobalScope.launch(Dispatchers.IO){


                for(data in tempList){
                    val entity = EpcEntity(
                        data.id,
                        data.epc,
                        data.count.toString(),
                        mRaceID,
                        data.bibNumber,
                        data.timeStamp,
                        1,
                        data.timeStamp,
                        TimeStamp.getMillis(0L,1)!!
                    )
                    epcEntity.add(entity)
                }
                //执行数据库更新
                mDataBase.epcDao()?.insertUnUploadEpc(epcEntity)
                //清空数据库epcEntity缓存
                epcEntity.clear()
                //清空列表tempList缓存
                tempList.clear()
            }
        //添加到已经上传的列表数量
        mUploadEpcListSize += list.size
        LogUtils.d(this,"成功上传 ${list.size} 条数据!")
        viewModelUnLoadedList.postValue(mLoadedEpcSize - mUploadEpcListSize)
    }

/*
* @Author: nick
* @Description: 上传CSV文件
* @DateTime: 2021-07-15 15:12
* @Params: 
* @Return 
*/
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun uploadCSVFile(){
            viewModelUploadFileCase.postValue(CaseState.UPLOAD_LOADING)
            mServiceBinder?.uploadCSV()
        }
/*
* @Author: nick
* @Description: 手动上传列表的剩余Epc
* @DateTime: 2021-10-08 11:18
* @Params: 
* @Return 
*/
    fun manualUploadEpcList(){
        mServiceBinder?.manualUploadEpcList()
    }

/*
* @Author: nick
* @Description: 开始上传服务
* @DateTime: 2021-06-17 16:28
* @Params:
* @Return
*/
    fun startUploadService(){
        if(!mUploadServiceBindState){
            mConnection = Connection()
            BaseApplication.context().run {
                try {
                    val intent = Intent(this, UploadDataService::class.java)
                    startService(intent)
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        startForegroundService(intent)
//                    } else {
//                        startService(intent)
//                    }
                    //绑定服务
                    mUploadServiceBindState = bindService(
                        intent,
                        mConnection,
                        Context.BIND_AUTO_CREATE
                    )
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }
/*
* @Author: nick
* @Description: 停止上传服务
* @DateTime: 2021-06-17 16:28
* @Params:
* @Return
*/
    fun stopUploadService(){
    //停止上传CSV文件
        stopTimerTask()
        BaseApplication.context().run {
            //停止绑定
            if(mUploadServiceBindState){
                unbindService(this@MainFragmentViewModel.mConnection)
                //停止服务
                stopService(Intent(this, UploadDataService::class.java))
                mUploadServiceBindState = false
            }
        }
    }
/*
* @Author: nick
* @Description: 清空已经读到的列表
* @DateTime: 2021-07-15 11:54
* @Params:
* @Return
*/
    fun clearReaderList(isAutoClear:Boolean){
        if (!isAutoClear){
            mReaderResultSize = 0
            viewModelLoadedListSize.postValue(mReaderResultSize)
        }
        mEpcList.clear()
        viewModelReaderResult.postValue(mEpcList)
    }

/*
* @Author: nick
* @Description: 服务类连接对象
* @DateTime: 2021-06-17 16:33
* @Params:
* @Return
*/
    inner class Connection: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mServiceBinder = service as UploadDataService.Bind
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mServiceBinder = null
        }
    }

/*
* @Author: nick
* @Description: 开启一个时间定时器
* @DateTime: 2021-07-19 14:21
* @Params:
* @Return
*/
    private fun startTimerTask(){
        if(mTimer == null){
            mTimer = Timer()
        }
        //上传CSV的轮播器
        if(mUploadCSVTimerTask == null){
            mUploadCSVTimerTask = object : TimerTask(){
                @RequiresApi(Build.VERSION_CODES.KITKAT)
                override fun run() {
                    mServiceBinder?.uploadCSV()
                }
            }
        }
        //如果有未上传的数据 数据库查询一次 再上传
//        if(mUnUploadTimerTask == null){
//            mUnUploadTimerTask = object : TimerTask(){
//                override fun run() {
//                    //检查一下数据库里面有没有未传的
//                    this@MainFragmentViewModel.getDataBaseUnUploadList(false)
//                    //判断列表是否超过50条 清空
//                    this@MainFragmentViewModel.mEpcList.run {
//                        if(this.size >= 50){
//                            this@MainFragmentViewModel.clearReaderList(true)
//                        }
//                    }
//                }
//            }
//        }
            mTimer?.run {
                //上传CSV 5分钟进行
                schedule(mUploadCSVTimerTask,300000,300000)
                //定时查询未上传列表 1分钟进行
                //schedule(mUnUploadTimerTask,60000,60000)
            }
    }

/*
* @Author: nick
* @Description: 停止CSV上传时间轮播任务
* @DateTime: 2021-07-19 14:22
* @Params:
* @Return
*/
    private fun stopTimerTask(){
        if(mTimer != null){
            mTimer?.cancel()
            mTimer = null
        }
        if(mUploadCSVTimerTask != null){
           //mUploadCSVTimerTask?.cancel()
            mUploadCSVTimerTask = null
        }
//        if(mUnUploadTimerTask != null){
//            mUnUploadTimerTask?.cancel()
//            mUnUploadTimerTask = null
//        }
    }

}