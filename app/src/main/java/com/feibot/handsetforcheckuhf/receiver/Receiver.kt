package com.feibot.handsetforcheckuhf.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.feibot.handsetforcheckuhf.view.detail.DetailFragmentViewModel

/**
 *@Author: Nick
 *@Description:屏幕关闭和电池电量的接受者
 *@Date 2021-07-14: 16:56
 */
class Receiver(viewModelStoreOwner: ViewModelStoreOwner?): BroadcastReceiver(){
/*
* @Author: nick
* @Description: 加载详情页的ViewModel
* @DateTime: 2021-07-02 10:46
* @Params:
* @Return
*/
    private val mDetailFragmentViewModel by lazy {
        ViewModelProvider(viewModelStoreOwner!!).get(DetailFragmentViewModel::class.java)
    }
/*
* @Author: nick
* @Description: 通知接受者
* @DateTime: 2021-07-14 17:01
* @Params: 
* @Return 
*/
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.extras != null){
             val current = intent.extras?.getInt(BatteryManager.EXTRA_LEVEL,-1) //获得当前电量
             val total = intent.extras?.getInt(BatteryManager.EXTRA_SCALE,-1) //获得总电量
            if(current != null && total != null){
                val percent = (current * 100) / total
                mDetailFragmentViewModel.viewModelBatteryLevel.postValue(percent)
            }else{
                mDetailFragmentViewModel.viewModelBatteryLevel.postValue(0)
            }
            //屏亮
            if (intent.action == Intent.ACTION_SCREEN_ON) {
                //读卡器开启电源
                //ReadTagToolPlus.getInstance().openGPIO()
            } //屏灭
            else if (intent.action == Intent.ACTION_SCREEN_OFF) {
                //读卡器关闭电源
                //ReadTagTool.getInstance().closeGPIO()
            }
        }
    }
}