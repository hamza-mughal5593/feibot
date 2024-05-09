package com.feibot.handsetforcheckuhf.view.custom

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.feibot.handsetforcheckuhf.R
import kotlinx.android.synthetic.main.confirm_dialog_view.*

/**
 *@Author: Nick
 *@Description:一般弹出提示框
 *@Date 2021-07-03: 12:06
 */
class ConfirmDialogView(context:Context):Dialog(context) {
    private var mCallBack:OnConfirmButtonClick? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_dialog_view)
        initEvent()
    }

    private fun initEvent() {
        this.enterBt.setOnClickListener {
            mCallBack?.onEnter()
            this.dismiss()
        }
        this.cancelBt.setOnClickListener {
            this.dismiss()
        }
    }



    fun setOnConfirmButtonClick(callback:OnConfirmButtonClick){
        mCallBack = callback
    }

    interface OnConfirmButtonClick{
        fun onEnter()
    }
}