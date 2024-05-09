package com.feibot.handsetforcheckuhf.view.main


import android.content.*
import android.graphics.Rect
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.feibot.handsetforcheckuhf.MainActivity
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.Tools.TimeStamp
import com.feibot.handsetforcheckuhf.base.BaseFragment
import com.feibot.handsetforcheckuhf.services.UploadMachineInfoWorker
import com.feibot.handsetforcheckuhf.utils.CaseState
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.feibot.handsetforcheckuhf.view.adapters.MainReaderResultListAdapter
import com.feibot.handsetforcheckuhf.view.custom.AlertDialogView
import com.feibot.handsetforcheckuhf.view.detail.DetailFragmentViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

/**
 *@Author: Nick
 *@Description:
 *@Date 2021-06-09: 17:12
 */
class MainFragment: BaseFragment(){
    private var job: Job? = null
    private lateinit var mAdapter:MainReaderResultListAdapter
    //设置读卡器开关
    private var mReaderSwitchFlag = false
    //声明shp
    private lateinit var mShp: SharedPreferences
    //显示AlertDialog
    private var mAlertDialog:AlertDialogView? = null

/*
* @Author: nick
* @Description: 加载页面
* @DateTime: 2021-06-10 15:26
* @Params:
* @Return
*/
    override fun setViewID(): Int {
        return R.layout.fragment_main
    }
    
/*
* @Author: nick
* @Description: 加载主页ViewModel
* @DateTime: 2021-06-10 15:25
* @Params: 
* @Return 
*/
    private val mMainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainFragmentViewModel::class.java)
    }

/*
* @Author: nick
* @Description: 加载详情页的ViewModel
* @DateTime: 2021-07-02 10:46
* @Params:
* @Return
*/
    private val mDetailFragmentViewModel by lazy {
        ViewModelProvider(requireActivity()).get(DetailFragmentViewModel::class.java)
    }

/*
* @Author: nick
* @Description: 初始化视图
* @DateTime: 2021-07-03 09:59
* @Params: 
* @Return 
*/
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun initView() {
        mShp = context!!.getSharedPreferences("deviceInfo",0)
        mAlertDialog = AlertDialogView(requireContext())
        mAlertDialog?.create()
        mAdapter = MainReaderResultListAdapter()
        //设置适配器样式
        readerResultItemListRv.apply {
            layoutManager = LinearLayoutManager(this@MainFragment.context,LinearLayoutManager.VERTICAL,true).apply {
                //stackFromEnd = false
            }
            adapter = mAdapter
//            //增加边距
//            addItemDecoration(object : RecyclerView.ItemDecoration(){
//                override fun getItemOffsets(
//                    outRect: Rect,
//                    view: View,
//                    parent: RecyclerView,
//                    state: RecyclerView.State
//                ) {
//                    outRect.top = 10
//                    outRect.bottom = 10
//                }
//            })
        }
        //读取按钮 跑马灯效果
        readCardBt.isSelected = true
        //注册物理按键监听读卡按键
        (requireActivity() as MainActivity).registerKeyDown(object :MainActivity.OnKeyDownInterface{
            override fun onKeyDown() {
                if (mReaderSwitchFlag) {
                    mMainViewModel.stopReadCard()
                }else{
                    mMainViewModel.startReadCard()
                }
            }
            //单次读卡
            override fun singleRead() {

            }
        })
    }

    fun readTagAction(){
        if (mReaderSwitchFlag) {
            mMainViewModel.stopReadCard()
        }else{
            mMainViewModel.startReadCard()
        }
    }
    
/*
* @Author: nick
* @Description: 加载观察者 观察数据
* @DateTime: 2021-06-10 15:25
* @Params:
* @Return
*/
    override fun initObserve() {
        mMainViewModel.apply {
            //观察读卡器结果
            viewModelReaderResult.observe(this@MainFragment) {
                  mAdapter.setData(it)
            }
            //观察读卡的按钮的状态
            viewModelReaderSwitchFlag.observe(this@MainFragment){
                mReaderSwitchFlag = !it
                if(it){
                    //开始读卡文字
                    readCardBt.text = this@MainFragment.resources.getText(R.string.main_start_read_card)
                    readCardBt.setBackgroundColor(this@MainFragment.resources.getColor(R.color.primary))
                }else{
                    //停止读卡文字
                    readCardBt.text = this@MainFragment.resources.getText(R.string.main_stop_read_card)
                    readCardBt.setBackgroundColor(this@MainFragment.resources.getColor(R.color.red))
                }
            }
            //观察上传结果
            viewModelUploadFileCase.observe(this@MainFragment){
                mAlertDialog?.setState(Pair(it,""))
            }
        }
    }
/*
* @Author: nick
* @Description: 加载各种事件
* @DateTime: 2021-06-10 15:26
* @Params: 
* @Return 
*/
    override fun initEvent() {
        //点击读卡的按钮
        readCardBt.setOnClickListener {
            if (mReaderSwitchFlag) {
                mMainViewModel.stopReadCard()
            }else{
                mMainViewModel.startReadCard()
            }
        }

        //点击响枪的按钮
        clickGunBt.run {
            setOnClickListener {
                //保存响枪状态shp
                mShp.edit().apply {
                    putBoolean("gunShot",true)
                    putString("gun_shot_time",TimeStamp.getChangeTimeData())
                }.apply()
                mDetailFragmentViewModel.detailViewModelGunShotTime.postValue(TimeStamp.getChangeTimeData())
            }
        }
        //清空按钮
        clearBt.setOnClickListener {
            //当前缓存mEpcList清空
            mMainViewModel.clearReaderList(false)
        }
        //加载历史记录中
        mAlertDialog?.setState(Pair(CaseState.QUERY_HISTORY,"0"))
        GlobalScope.launch(Dispatchers.IO) {
            delay(3000)
            //从数据库获取提取为上传的数据
            mMainViewModel.getDataBaseUnUploadList()
        }
        //开启一个后台上传设备信息的任务
        startUploadMachineInfo()
    }


/*
* @Author: nick
* @Description: 页面销毁
* @DateTime: 2021-06-15 10:43
* @Params: 
* @Return 
*/
    override fun onDestroy() {
        mMainViewModel.stopUploadService()
        mMainViewModel.destroyReader()
        super.onDestroy()
    }

/*
* @Author: nick
* @Description: 加载服务类
* @DateTime: 2021-06-17 17:00
* @Params: 
* @Return 
*/
    override fun initService() {
        mMainViewModel.startUploadService()
    }
/*
* @Author: nick
* @Description: 加载工具类
* @DateTime: 2021-06-21 14:50
* @Params: 
* @Return 
*/
    override fun initTools() {
        mMainViewModel.initTools()
    }
/*
* @Author: nick
* @Description: 上传CSV文件
* @DateTime: 2021-07-15 15:11
* @Params: 
* @Return 
*/
@RequiresApi(Build.VERSION_CODES.KITKAT)
fun uploadCSVFile() {
       mMainViewModel.uploadCSVFile()
    }
/*
* @Author: nick
* @Description: 手动上传余数的EpcList
* @DateTime: 2021-10-08 11:19
* @Params:
* @Return
*/
    fun manualUploadList(){
        mMainViewModel.manualUploadEpcList()
    }
/**
*开启一个上传设备信息的任务15分钟执行一次
*
* @date 2022/9/20 9:37
* @params  * @param null
* @return
* @author nick
*/
    private fun startUploadMachineInfo(){
        //生成一个约束条件
        val constraints =
            Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)  // 网络状态
//                .setRequiresBatteryNotLow(true)                 // 不在电量不足时执行
//                .setRequiresCharging(false)                      // 在充电时执行
//                .setRequiresStorageNotLow(true)                 // 不在存储容量不足时执行
//                .setRequiresDeviceIdle(true)                    // 在待机状态下执行，需要 API 23以上
            .build()
        //先清空上次作业
        WorkManager.getInstance(this.context!!).cancelAllWorkByTag("uploadMachineInfo")
        //任务计划
        val request =
            PeriodicWorkRequest.Builder(UploadMachineInfoWorker::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag("uploadMachineInfo")
                .build()
        //执行新任务
        WorkManager.getInstance(this.context!!).enqueue(request)
    }
}