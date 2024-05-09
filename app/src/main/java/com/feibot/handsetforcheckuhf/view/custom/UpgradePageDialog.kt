package com.feibot.handsetforcheckuhf.view.custom

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.graphics.PathUtils
import com.feibot.handsetforcheckuhf.BuildConfig
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.api.RetrofitApi
import com.feibot.handsetforcheckuhf.api.RetrofitClient
import com.feibot.handsetforcheckuhf.api.retrofit
import com.feibot.handsetforcheckuhf.base.BaseApplication
import com.feibot.handsetforcheckuhf.utils.CaseState
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate._XUpdate
import com.xuexiang.xupdate.entity.DownloadEntity
import com.xuexiang.xupdate.service.OnFileDownloadListener
import com.xuexiang.xupdate.utils.FileUtils
import kotlinx.android.synthetic.main.dialog_upgrade.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Callback
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception

/**
 *@Author: Nick
 *@Description:升级页面的Dialog
 *@Date 2021-07-15: 08:31
 */
class UpgradePageDialog(context:Context):Dialog(context, R.style.Dialog_Fullscreen) {

    private lateinit var mDeviceVersion: String
    private lateinit var mDeviceID: String
    private var mShp:SharedPreferences? = null
    private var mAlertDialog:AlertDialogView? = null
    private var mUpgradeDialog:UpgradeDialogView? = null
    private lateinit var mDownloadPath:String
    private lateinit var mDownloadUrl:String
    private lateinit var mValidateVersionUrl:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_upgrade)
        initData()
        initView()
        initEvent()
    }
/*
* @Author: nick
* @Description: 加载一些设备的参数
* @DateTime: 2021-08-16 16:49
* @Params: 
* @Return 
*/
    private fun initData() {
        mShp = BaseApplication.context().getSharedPreferences("deviceInfo",0)
        mDeviceID = mShp?.getString("device_id","")!!
        mDeviceVersion = BuildConfig.VERSION_NAME
        mDownloadPath = BaseApplication.context().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()
        mValidateVersionUrl = "http://racepower.feibot.com/timingPlatform/softwareUpdatePDA3505.php?machineId=${mDeviceID}&info=version"
        mDownloadUrl =  "http://racepower.feibot.com/timingPlatform/download/PDA3505/upgrade_package/PDA3505_${mDeviceVersion}.apk"
    }

    private fun initView() {
        mAlertDialog = AlertDialogView(context)
        mAlertDialog?.create()
        mUpgradeDialog = UpgradeDialogView(context)
        machineVersionTv.text = mDeviceVersion
        deviceIdTv.text = mDeviceID
    }

    private fun initEvent() {
        upgradeBt.setOnClickListener {
            validateVersion()
        }
        upgradeBackIv.setOnClickListener {
            this.dismiss()
        }
    }

    private fun validateVersion(){
        GlobalScope.launch (Dispatchers.IO){
            retrofit<ResponseBody> {
                api = RetrofitClient.upgradeVersionApi.validateVersion(mDeviceID)
                onSuccess { body, call ->
                    val newVersionName = body.string()
                    if(newVersionName == mDeviceVersion){
                        GlobalScope.launch(Dispatchers.Main) {


                            mAlertDialog?.setState(Pair(CaseState.UPGRADE_SUCCESS,"0"))
                        }
                    }else{
                        LogUtils.d(this@UpgradePageDialog,"下载文件文件中...${newVersionName}.apk")
                        //执行下载任务
                        downloadApkFile(newVersionName)
                    }
                }
                onFail { msg, errorCode ->
                    LogUtils.d(this@UpgradePageDialog,"下载文件失败！")
                    GlobalScope.launch (Dispatchers.Main){
                        mUpgradeDialog?.dismiss()
                        mAlertDialog?.setState(Pair(CaseState.ERROR,"0"))
                    }
                }
            }
        }
    }

/*
* @Author: nick
* @Description: 下载升级包任务
* @DateTime: 2021-08-16 17:00
* @Params:
* @Return
*/
    private fun downloadApkFile(newVersionName: String){
        GlobalScope.launch (Dispatchers.IO){
            retrofit<ResponseBody> {
                api = RetrofitClient.upgradeVersionApi.downloadUpgradePackage("download/PDA3505/upgrade_package/PDA3505_${newVersionName}.apk")
                onSuccess { body, call ->
                    val inputStream = body.byteStream()
                    val fileSize = body.contentLength()
                    LogUtils.d(this@UpgradePageDialog,"filesize--->$fileSize")
                    //保存文件
                    saveUpgradeFile(inputStream,newVersionName,fileSize)
                }
                onFail { msg, errorCode ->
                    LogUtils.d(this@UpgradePageDialog,"下载失败。。$errorCode--->$msg")
                    GlobalScope.launch(Dispatchers.Main) {


                        mAlertDialog?.setState(Pair(CaseState.ERROR,"0"))
                    }
                }
            }
        }
    }

/*
* @Author: nick
* @Description: 保存下载下来的升级包
* @DateTime: 2021-08-17 11:50
* @Params: 
* @Return 
*/
    private fun saveUpgradeFile(fileInputStream: InputStream,newVersionName: String,fileSize:Long){
        //通知更新进度
        mUpgradeDialog?.setDownloadFileState(Pair(CaseState.UPGRADE_UPGRADING,""))

        LogUtils.d(this@UpgradePageDialog,"写入文件文件中...")
        val fileLength = fileSize

        GlobalScope.launch(Dispatchers.IO) {


            try {
                val fileName = "${newVersionName}.apk"
                val file = File(BaseApplication.context().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),fileName)
                //写入文件操作
                val fop = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var len:Int
                var count = 0
                while (((fileInputStream.read(buffer)).also { len = it}) != -1){
                    count += len
                    fop.write(buffer,0,len)
                    val progress = ((count * 1.0f / fileLength) * 100).toInt()
                    updateProgress(progress)
                }
                fop.close()
                //安装任务
                installPackage(file)
            }catch (e:IOException){
                e.printStackTrace()
            }finally {
                mUpgradeDialog?.dismiss()
            }
        }
    }

    private fun updateProgress(progress:Int){
        GlobalScope.launch(Dispatchers.Main) {


            mUpgradeDialog?.setProgress(progress)
        }
    }

/*
* @Author: nick
* @Description: 安装下载的升级包
* @DateTime: 2021-08-16 17:01
* @Params:
* @Return
*/
    private fun installPackage(apkFile:File){
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)//安装完成后打开新版本
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 给目标应用一个临时授权
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//判断版本大于等于7.0
                //如果SDK版本>=24，即：Build.VERSION.SDK_INT >= 24，使用FileProvider兼容安装apk
                val  packageName = BaseApplication().packageName
                val  authority = StringBuilder(packageName).append(".fileprovider").toString()
                val  apkUri = FileProvider.getUriForFile(context, authority, apkFile)
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            } else {
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
            }
            context.startActivity(intent)
            android.os.Process.killProcess(android.os.Process.myPid())//安装完之后会提示”完成” “打开”。
        }catch (e:Exception){
            e.printStackTrace()
            GlobalScope.launch(Dispatchers.Main){


                mAlertDialog?.setState(Pair(CaseState.ERROR,"0"))
            }
        }
    }
}