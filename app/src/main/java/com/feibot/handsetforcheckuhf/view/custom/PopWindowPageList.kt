package com.feibot.handsetforcheckuhf.view.custom

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.base.BaseApplication
import com.feibot.handsetforcheckuhf.utils.LogUtils
import kotlinx.android.synthetic.main.popwindow_page_list_view.view.*
import kotlinx.android.synthetic.main.popwindow_page_list_view.view.checkEpcTv
import kotlinx.android.synthetic.main.popwindow_page_list_view.view.downLoadRaceListOnMainTv
import kotlinx.android.synthetic.main.popwindow_page_list_view.view.downloadConfigForPopTv
import kotlinx.android.synthetic.main.popwindow_page_list_view.view.quitSystemTv
import kotlinx.android.synthetic.main.popwindow_page_list_view.view.settingPageTv
import kotlinx.android.synthetic.main.popwindow_page_list_view.view.uploadZipFileTv
import kotlinx.android.synthetic.main.popwindow_page_view.view.*

/**
 *@Author: Nick
 *@Description:
 *@Date 2021-07-12: 16:26
 */
@SuppressLint("InflateParams")
class PopWindowPageList(context:Context,layoutWidth:Int,layoutHeight:Int):PopupWindow(context){
    private var mCallback: OnItemClick? = null
    private var mPopWindow:View
    init {
        width = (layoutWidth * 0.5).toInt()
        height = (layoutHeight * 0.6).toInt()
        //先设置背景透明 否则无法点击外部隐藏
        setBackgroundDrawable(object : ColorDrawable(Color.TRANSPARENT){})
        mPopWindow = LayoutInflater.from(context).inflate(R.layout.popwindow_page_view,null)
        contentView = mPopWindow
        isOutsideTouchable = true
        animationStyle = R.style.AnimTools
        getClick()
    }
    fun setOnItemClick(callback:OnItemClick){
        mCallback =  callback
    }

    interface OnItemClick{
        fun clickItem(id:Int)
    }
/*
* @Author: nick
* @Description: 设置点击后返回该点击组件的ID
* @DateTime: 2021-07-13 09:03
* @Params: 
* @Return 
*/
    private fun getClick(){
        mPopWindow.apply {
            this.downloadConfigForPopTv.setOnClickListener {
                mCallback?.clickItem(it.id)
                this@PopWindowPageList.dismiss()
            }
            this.uploadZipFileTv.setOnClickListener {
                mCallback?.clickItem(it.id)
                this@PopWindowPageList.dismiss()
            }
            this.uploadEpcListTv.setOnClickListener {
                mCallback?.clickItem(it.id)
                this@PopWindowPageList.dismiss()
            }
            this.checkEpcTv.setOnClickListener {
                mCallback?.clickItem(it.id)
                this@PopWindowPageList.dismiss()
            }
            this.settingPageTv.setOnClickListener {
                mCallback?.clickItem(it.id)
                this@PopWindowPageList.dismiss()
            }
            this.quitSystemTv.setOnClickListener {
                mCallback?.clickItem(it.id)
                this@PopWindowPageList.dismiss()
            }
            this.downLoadRaceListOnMainTv.setOnClickListener {
                mCallback?.clickItem(it.id)
                this@PopWindowPageList.dismiss()
            }
        }
    }
}