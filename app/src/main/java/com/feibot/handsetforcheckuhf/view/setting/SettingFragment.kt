package com.feibot.handsetforcheckuhf.view.setting

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.base.BaseFragment
import com.feibot.handsetforcheckuhf.contants.URL
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.feibot.handsetforcheckuhf.view.custom.*
import com.feibot.handsetforcheckuhf.view.detail.DetailFragmentViewModel
import com.feibot.handsetforcheckuhf.view.main.MainFragmentViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.fragment_setting.setTimeTv
import kotlinx.android.synthetic.main.fragment_setting.view.*
import kotlinx.android.synthetic.main.number_picker_confirm_dialog.*
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*

/**
 *@Author: Nick
 *@Description:
 *@Date 2021-06-09: 17:19
 */
class SettingFragment:BaseFragment(){
    private var mAlertDialogView:AlertDialogView? = null
    private var mChangeTimeNumberPickerConfirmDiaLog:ChangeTimeNumberPickerConfirmDiaLog ? = null
    private var mReaderPowerValueDialog:ReaderPowerValueDialog ? = null
    private var mConfirmDiaLog:ConfirmDialogView? = null
    private var mUpgradePageDialog:UpgradePageDialog? = null
    private var mAboutPageDialog:AboutDialog? = null
    private lateinit var mShp:SharedPreferences
    private lateinit var mRaceID:String

//设置界面的ViewModel
    private val mSettingViewModel by lazy {
        ViewModelProvider(requireActivity()).get(SettingFragmentViewModel::class.java)
    }
    //主页的ViewModel
    private val mMainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainFragmentViewModel::class.java)
    }
    //详情页的ViewModel
    private val mDetailViewModel by lazy {
        ViewModelProvider(requireActivity()).get(DetailFragmentViewModel::class.java)
    }

/*
* @Author: nick
* @Description: 设置主页ViewID
* @DateTime: 2021-06-23 11:40
* @Params:
* @Return
*/
    override fun setViewID(): Int {
        handleSSLHandshake()
        return R.layout.fragment_setting
    }
/*
* @Author: nick
* @Description: 初始化界面
* @DateTime: 2021-07-14 10:09
* @Params: 
* @Return 
*/
    override fun initView() {
        //shp
        mShp = context!!.getSharedPreferences("deviceInfo",0)
    }
/*
* @Author: nick
* @Description: 加载viewModel
* @DateTime: 2021-06-28 14:05
* @Params: 
* @Return 
*/
    override fun initViewModel() {
        mSettingViewModel.initViewModel(requireActivity())
    }


/*
* @Author: nick
* @Description: 加载工具类
* @DateTime: 2021-06-25 11:05
* @Params: 
* @Return 
*/
    @SuppressLint("InflateParams")
    override fun initTools() {
        //创建AlertDialog
        mAlertDialogView = AlertDialogView(requireActivity())
        mAlertDialogView?.create()
        //创建NumberPickerDialog
        mChangeTimeNumberPickerConfirmDiaLog = ChangeTimeNumberPickerConfirmDiaLog(context!!)
        mChangeTimeNumberPickerConfirmDiaLog?.create()
        //创建读卡器功率设置ReaderNumberPickerDialog
        mReaderPowerValueDialog = ReaderPowerValueDialog(context!!)
        mReaderPowerValueDialog?.create()
        //取该赛事的ID
        mRaceID = mShp.getString("race_id","0")!!
        //一般的ConfirmDialog
        mConfirmDiaLog = ConfirmDialogView(requireActivity())
        mConfirmDiaLog?.create()
        //升级页面的Dialog
        mUpgradePageDialog = UpgradePageDialog(requireActivity())
        mUpgradePageDialog?.create()
        //关于界面的Dialog
        mAboutPageDialog = AboutDialog(requireActivity())
        mAboutPageDialog?.create()
    }

/*
* @Author: nick
* @Description: 设置监听事件
* @DateTime: 2021-06-23 11:41
* @Params:
* @Return
*/
    override fun initObserve() {
        mSettingViewModel.apply {
            viewModelCaseState.observe(this@SettingFragment){
                mAlertDialogView?.setState(it)
            }
        }
    }
/*
* @Author: nick
* @Description: 下载配置文件
* @DateTime: 2021-07-13 11:48
* @Params: 
* @Return 
*/
    fun downloadConfigFile(){
        mSettingViewModel.downloadConfigPath()
    }
/*
* @Author: nick
* @Description: 下载赛事名单
* @DateTime: 2021-07-14 11:00
* @Params: 
* @Return 
*/
    fun downloadRaceList(){
        if (URL.isChineseLanguage()){
            mSettingViewModel.downloadPlayerInfoList(mRaceID)
        }else{
            mSettingViewModel.downloadGlobalPlayerInfoList(mRaceID)
        }
    }

/*
* @Author: nick
* @Description: 停止响枪
* @DateTime: 2021-07-13 11:54
* @Params: 
* @Return 
*/
    fun stopGunShot(){
        mConfirmDiaLog?.show()
    }

/*
* @Author: nick
* @Description: 设置各种事件
* @DateTime: 2021-06-23 11:41
* @Params: 
* @Return 
*/
@SuppressLint("CommitPrefEdits")
@RequiresApi(Build.VERSION_CODES.N)
    override fun initEvent() {
        //设置读卡器功率
        setReaderPowerValueBt.setOnClickListener {
            mReaderPowerValueDialog
            mReaderPowerValueDialog?.show()
        }

        //设置时间按钮
        setTimeTv.setOnClickListener {
            mChangeTimeNumberPickerConfirmDiaLog?.show()
        }
        //当时间设置完成之后 通知 ViewModel 设置时间
        mChangeTimeNumberPickerConfirmDiaLog?.setOnFinishSetTime(object :ChangeTimeNumberPickerConfirmDiaLog.OnFinishSetTime{
            override fun onSetFinished(timeMap: HashMap<String, Int>) {
                LogUtils.d(this@SettingFragment,
                    "${timeMap["year"]}-${timeMap["month"]}-${timeMap["day"]}-${timeMap["hour"]}-${timeMap["minute"]}-${timeMap["second"]}-${timeMap["millisecond"]}")
                mSettingViewModel.setTime(timeMap)
            }
        })
        //当读卡器功率设置完成之后
        mReaderPowerValueDialog?.setOnFinishSetPowerValue(object : ReaderPowerValueDialog.OnFinishSetPowerValue {
            override fun onPowerValueSet(value: Int) {
                mSettingViewModel.setReaderValue(value)
            }
        })
        //返回主页面
        settingBackIv.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().hide(this).commit()
        }
        //升级界面
        upgradeTv.setOnClickListener {
            mUpgradePageDialog?.show()
        }
        //关于界面
        aboutTv.setOnClickListener {
            mAboutPageDialog?.show()
        }
        //注册设备
        registerMachineTv.setOnClickListener {
            mSettingViewModel.registerMachine()
        }
    }

/*
* @Author: nick
* @Description: 忽略https的证书校验
* @DateTime: 2021-07-05 13:50
* @Params:
* @Return
*/
    @SuppressLint("TrustAllX509TrustManager")
    fun handleSSLHandshake() {
        try {
            val trustAllCerts: Array<TrustManager> =
                arrayOf(object : X509TrustManager{

                    override fun checkClientTrusted(p0: Array<out X509Certificate>, p1: String?) {}

                    override fun checkServerTrusted(p0: Array<out X509Certificate>, p1: String?) {}

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                })
            val sc: SSLContext = SSLContext.getInstance("TLS")
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier(HostnameVerifier { _, _ ->
                true
            })
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }
    }
}