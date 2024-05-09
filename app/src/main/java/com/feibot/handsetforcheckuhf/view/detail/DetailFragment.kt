package com.feibot.handsetforcheckuhf.view.detail

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.Tools.TimeStamp
import com.feibot.handsetforcheckuhf.base.BaseFragment
import com.feibot.handsetforcheckuhf.utils.MachineInfoUtil
import com.feibot.handsetforcheckuhf.view.main.MainFragmentViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlin.math.sign

/*
 *@Author: Nick
 *@Description:
 *@Date 2021-06-09: 17:16
 */
class DetailFragment:BaseFragment() {
    private lateinit var  mShp: SharedPreferences
    //主页MainViewModel
    private val mMainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainFragmentViewModel::class.java)
    }
    //详情页ViewModel
    private val mDetailFragmentViewModel by lazy {
        ViewModelProvider(requireActivity()).get(DetailFragmentViewModel::class.java)
    }
/*
* @Author: nick
* @Description: 加载界面
* @DateTime: 2021-07-02 10:08
* @Params: 
* @Return 
*/
    override fun setViewID(): Int {
        return R.layout.fragment_detail
    }

/*
* @Author: nick
* @Description: 初始化视图
* @DateTime: 2021-07-03 10:04
* @Params: 
* @Return 
*/
    override fun initView() {
        mShp = context!!.getSharedPreferences("deviceInfo",0)
        //设置 从 shp 取响枪时间
        gunShotTimeTv.text = mShp.getString("gun_shot_time","00:00:00")
        //坐标信息 跑马灯效果
        detailPositionTv.isSelected = true
    }

/*
* @Author: nick
* @Description: 加载ViewModel的方法
* @DateTime: 2021-06-25 16:08
* @Params: 
* @Return 
*/
    override fun initViewModel() {
        mDetailFragmentViewModel.initViewModel()
    }
    
/*
* @Author: nick
* @Description: 加载ViewModel观察
* @DateTime: 2021-06-23 08:34
* @Params: 
* @Return 
*/
    @SuppressLint("SetTextI18n")
    override fun initObserve() {
    //观察主页的数据动态
        mMainViewModel.apply {
            //主页读取数量观察
            viewModelLoadedListSize.observe(this@DetailFragment){
                epcListCountTv.text = it.toString()
                MachineInfoUtil.diffTagSize = it.toString()
            }
            //观察主页待传数据
            viewModelUnLoadedList.observe(this@DetailFragment){
                if(it <= 0){
                    epcListUnLoadCountTv.text = "0"
                }else{
                    epcListUnLoadCountTv.text = it.toString()
                }
            }
        }
            //观察详情页的数据变化
            mDetailFragmentViewModel.apply {
            //详情页的赛事标题 设备编号
            detailViewModelDeviceID.observe(this@DetailFragment){
                detailDeviceIdTv.text = it
                MachineInfoUtil.machineId = it
            }
            //详情页的赛事ID
            detailViewModelRaceID.observe(this@DetailFragment){
                detailRaceIdTv.text = it
                MachineInfoUtil.eventId = it
            }
            //详情页的赛事名称
            detailViewModelRaceName.observe(this@DetailFragment){
                detailRaceNameTv.text = it
            }
            //详情页的赛事坐标
            detailViewModelRacePosition.observe(this@DetailFragment){
                detailPositionTv.text = it
            }
            //机器的本地时间
            detailViewModelLocalTime.observe(this@DetailFragment){
                localTimeTv.text = it
            }
            //响枪时间
            detailViewModelGunShotTime.observe(this@DetailFragment){
                gunShotTimeTv.text = it
            }
            //网路状态的监听
            detailViewModelNetworkState.observe(this@DetailFragment){
                networkStateTv.apply {
                    text = if(it == 0){
                        MachineInfoUtil.networkState = "网络不佳"
                        requireActivity().resources.getString(R.string.detail_network_unlink)
                    }else{
                        MachineInfoUtil.networkState = "联网"
                        requireActivity().resources.getString(R.string.detail_network_link)
                    }
                }
            }
            //电池电量的监听
            viewModelBatteryLevel.observe(this@DetailFragment){
                batteryLevelTv.text = "$it%"
                MachineInfoUtil.battery = "$it%"
            }
        }
    }
/*
* @Author: nick
* @Description: 监听各种时事件
* @DateTime: 2021-07-12 17:47
* @Params:
* @Return
*/
    override fun initEvent() {}

    override fun onDestroy() {
        super.onDestroy()
        //saveRunningOffTime()
        mDetailFragmentViewModel.stopLooperTask()
    }
/*
* @Author: nick
* @Description: 当页面摧毁后保存赛事已经用时
* @DateTime: 2021-07-02 16:36
* @Params: 
* @Return 
*/
    @SuppressLint("CommitPrefEdits")
    private fun saveRunningOffTime(){
        val runningTimeOffTime = TimeStamp.runningChangeMillis
        val destroyTimeMillis = TimeStamp.getCurrentMillisTime()
        val editor = mShp.edit()
        editor.putLong("running_off_time",runningTimeOffTime)
        editor.putLong("destroy_time_millis",destroyTimeMillis)
        editor.apply()
    }
}