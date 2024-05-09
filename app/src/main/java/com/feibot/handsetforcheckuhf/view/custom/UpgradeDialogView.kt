package com.feibot.handsetforcheckuhf.view.custom

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.utils.CaseState
import com.feibot.handsetforcheckuhf.utils.LogUtils
import kotlinx.android.synthetic.main.alert_dialog_view.*
import kotlinx.android.synthetic.main.alert_dialog_view.infoTv
import kotlinx.android.synthetic.main.upgrade_alert_dialog_view.*

/**
 *@Author: Nick
 *@Description:
 *@Date 2021-08-17: 14:34
 */
class UpgradeDialogView(context:Context):Dialog(context, R.style.ShareDialog){

    private var mText: String = ""
    private var mInfoText: TextView? = null
    private var mUpgradePageDialog: UpgradePageDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upgrade_alert_dialog_view)
        setCanceledOnTouchOutside(false)
        initView()
    }

    private fun initView() {
        mInfoText = upgradeInfoTv
        mUpgradePageDialog = UpgradePageDialog(context)
    }


    //设置进度条
    fun setDownloadFileState(pair: Pair<CaseState, String>){
        mText = context.resources.getString(R.string.upgrade_progress)
        when(pair.first){
            CaseState.UPGRADE_UPGRADING->{
                mInfoText?.text = String.format(mText,0,"%")
                this.show()
            }
            else->{
                mInfoText?.text = context.resources.getString(R.string.upgrade_install)
                this.show()
            }
        }
    }
    fun setProgress(progress:Int){
        mInfoText?.text = String.format(mText,progress,"%")
    }
}