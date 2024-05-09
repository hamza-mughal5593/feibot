package com.feibot.handsetforcheckuhf

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.feibot.handsetforcheckuhf.Tools.ReadTagToolPlus
import com.feibot.handsetforcheckuhf.base.BaseFragment
import com.feibot.handsetforcheckuhf.receiver.Receiver
import com.feibot.handsetforcheckuhf.view.custom.PopWindowPageList
import com.feibot.handsetforcheckuhf.view.detail.DetailFragment
import com.feibot.handsetforcheckuhf.view.main.MainFragment
import com.feibot.handsetforcheckuhf.view.search.SearchFragment
import com.feibot.handsetforcheckuhf.view.setting.SettingFragment
import com.lidroid.xutils.util.LogUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.lang.Exception
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(),CoroutineScope{
    private lateinit var mTransaction: FragmentTransaction
    private lateinit var job: Job
    //声明读卡器工具类plus
    private lateinit var mReadTagToolPlus: ReadTagToolPlus
    //页面导航PopWindow
    private lateinit var mPopWindow: PopWindowPageList
    //声明一个布局管理器
    private lateinit var mFm:FragmentManager
    //各种Fragment
    private lateinit var mMainFragment: MainFragment
    private lateinit var mDetailFragment: DetailFragment
    private lateinit var mSettingFragment:SettingFragment
    private lateinit var mSearchFragment: SearchFragment
    private lateinit var mOtherContainer: View
    private var mOnKeyDownListener: OnKeyDownInterface? = null

    //连续读卡或者是单独读卡
    private var singleRead: Boolean = false

    interface OnKeyDownInterface{
        fun onKeyDown()
        fun singleRead()
    }

    //缓存的一个Fragment
    private  var lastFragment:BaseFragment? = null

    override fun onResume() {
        //kai读卡器加载模块
        mReadTagToolPlus.initModule(this)
        super.onResume()
    }

    override fun onPause() {
        mReadTagToolPlus.mReader?.free()
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //协程赋值
        job = Job()
        initView()
        initEvent()
        initReaderWorking()
    }
/*
* @Author: nick
* @Description: 加载各种点击事件
* @DateTime: 2021-07-13 09:17
* @Params: 
* @Return 
*/
    private fun initEvent() {
        //popWindow的显示
        pageListIv.setOnClickListener {
            mPopWindow.showAsDropDown(it,0,-10)
        }
        //popWindow的item的点击事件
        mPopWindow.setOnItemClick(object : PopWindowPageList.OnItemClick{
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun clickItem(id: Int) {
                when(id){
                    //下载配置文件界面
                    R.id.downloadConfigForPopTv -> {
                        mSettingFragment.downloadConfigFile()
                    }
                    //下载赛事名单
                    R.id.downLoadRaceListOnMainTv -> {
                        mSettingFragment.downloadRaceList()
                    }
                    //切换上传Zip界面
                    R.id.uploadZipFileTv -> {
                        mMainFragment.uploadCSVFile()
                    }
                    //手动上传Epc列表的动作
                    R.id.uploadEpcListTv -> {
                        mMainFragment.manualUploadList()
                    }
                    //切换搜索界面
                    R.id.checkEpcTv -> {
                        //this@MainActivity.supportFragmentManager.beginTransaction()
                        singleRead = true
                        startOtherPage(mSearchFragment)
                    }
                    //切换设置页面
                    R.id.settingPageTv -> {
                        //this@MainActivity.supportFragmentManager.beginTransaction().show(mSettingFragment).commit()
                        startOtherPage(mSettingFragment)
                    }
                    //退出应用
                    R.id.quitSystemTv ->{
                        mMainFragment.onDestroy()
                        this@MainActivity.finish()
                        exitProcess(0)
                    }
                }
            }
        })

    }
/*
* @Author: nick
* @Description: 读卡器开始工作 暂停读卡器的工作交给MainFragment destroyView时进行
* @DateTime: 2021-07-05 14:38
* @Params:
* @Return
*/
    private fun initReaderWorking() {
        //kai读卡器加载模块
        //mReadTagTool.initModule(this)
        //加载超远距离手持机读卡模块
        mReadTagToolPlus.initModule(this)
    }
/*
* @Author: nick
* @Description: 隐藏导航栏
* @DateTime: 2021-06-10 08:36
* @Params:
* @Return
*/
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus){
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }
/**
*
* 当机器有物理键按下后
* @date 2022/9/23 17:49
* @params  * @param null
* @return
* @author nick
*/
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 293) {
            if (event!!.repeatCount == 0){
                if(!singleRead){
                    //主页面读卡联系读卡
                    mMainFragment.readTagAction()
                    //mOnKeyDownListener?.onKeyDown()
                }else{
                    //芯片检测单独检测
                    mOnKeyDownListener?.singleRead()
                }
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

/*
* @Author: nick
* @Description: 加载页面布局
* @DateTime: 2021-06-15 10:27
* @Params: 
* @Return 
*/
    private fun initView() {
        //拿到读卡器的实例
        mReadTagToolPlus = ReadTagToolPlus.getInstance()
        //fragment实例化
        mMainFragment = MainFragment()
        mDetailFragment = DetailFragment()
        mSearchFragment= SearchFragment()
        mSettingFragment = SettingFragment()
        mOtherContainer = otherContainer
        //设置Fragment管理器
        mFm = supportFragmentManager
        mTransaction = mFm.beginTransaction()
        //加载各Fragment的页面
        initFragment()
        //加载PopWindow
        mPopWindow = PopWindowPageList(this,this.windowManager.defaultDisplay.width,this.windowManager.defaultDisplay.height)
        //注册观察电池容量的广播
        val receiver = Receiver(this)
        //注册屏幕关闭以及电量的广播
        val receiverFilter = IntentFilter()
        receiverFilter.run {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            }
        registerReceiver(receiver,receiverFilter)
        mSearchFragment.closeEvent(object : SearchFragment.CloseEvents {
            override fun onSearchViewClose() {
                singleRead = false
            }
        })

    }

/*
* @Author: nick
* @Description: 把各种Fragment加载到FragmentManager
* @DateTime: 2021-07-13 11:01
* @Params: 
* @Return 
*/
    private fun initFragment(){
        mTransaction.add(R.id.detailContainer,mDetailFragment)
        mTransaction.add(R.id.mainContainer,mMainFragment)
        mTransaction.add(R.id.otherContainer,mSettingFragment)
        mTransaction.commit()
    }
/*
* @Author: nick
* @Description: 切换各种Fragment
* @DateTime: 2021-07-13 11:01
* @Params:
* @Return
*/
    fun startOtherPage(fragment:BaseFragment){
        val transaction = mFm.beginTransaction()
        //判断Fragment并且隐藏上一个Fragment
        if(!fragment.isAdded){
            transaction.hide(mSettingFragment)
            transaction.add(R.id.otherContainer,fragment)
        }else{
            transaction.show(fragment)
        }
        transaction.commit()
        mOtherContainer.visibility = View.VISIBLE
    }

/*
* @Author: nick
* @Description: 设置页面资源释放后
* @DateTime: 2021-06-15 10:27
* @Params: 
* @Return 
*/
    override fun onDestroy() {
        //关闭页面结束所有携程
        try {
            job.cancel()
            mReadTagToolPlus.mReader?.free()
        }catch (e:Exception){
            e.printStackTrace()
        }
        super.onDestroy()
    }

/*
* @Author: nick
* @Description: 设置协程的上下文
* @DateTime: 2021-06-15 10:27
* @Params:
* @Return
*/
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    fun registerKeyDown(callback:OnKeyDownInterface){
        mOnKeyDownListener = callback
    }

    fun searchViewClose(){
        singleRead = false
    }

}




