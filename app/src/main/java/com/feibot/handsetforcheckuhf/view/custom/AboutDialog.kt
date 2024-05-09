package com.feibot.handsetforcheckuhf.view.custom

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.utils.GetMACTool
import kotlinx.android.synthetic.main.dialog_aboout.*

/**
 *@Author: Nick
 *@Description:关于界面的Dialog
 *@Date 2021-07-15: 09:22
 */
class AboutDialog(context:Context):Dialog(context,R.style.Dialog_Fullscreen) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_aboout)
        initView()
        initEvent()
    }

    private fun initView() {
        macAddressTv.text = Settings.Global.getString(context.contentResolver,"WiFiMac")
        //macAddressTv.text = "6C:15:24:81:41:04"
    }
    private fun initEvent() {
        aboutBackIv.setOnClickListener {
            this.dismiss()
        }
    }

}