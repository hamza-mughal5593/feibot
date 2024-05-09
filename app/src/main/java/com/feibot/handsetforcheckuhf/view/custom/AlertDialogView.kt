package com.feibot.handsetforcheckuhf.view.custom

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.utils.CaseState
import kotlinx.android.synthetic.main.alert_dialog_view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *@Author: Nick
 *@Description:一个DiaLog提示框
 *@Date 2021-06-25: 10:23
 */
class AlertDialogView(context:Context):Dialog(context){

    private var mInfoText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alert_dialog_view)
        initView()
    }

    private fun initView() {
        mInfoText = infoTv
    }

    @SuppressLint("SetTextI18n")
    fun setState(pair: Pair<CaseState, String>){
        when(pair.first){
            CaseState.LOADING -> {
                mInfoText?.text = context.resources.getString(R.string.setting_download_config_file_loading)
                this.show()
            }
            CaseState.ERROR -> {
                mInfoText?.text = context.resources.getString(R.string.setting_download_config_file_error)
                this.show()
            }
            CaseState.NO_CHECK-> {
                mInfoText?.text = context.resources.getString(R.string.setting_download_config_file_no_check)
                this.show()
            }
            CaseState.SUCCESS -> {
                mInfoText?.text = context.resources.getString(R.string.dialog_race_id_test) + pair.second + " "+ context.resources.getString(R.string.setting_download_config_file_done)
                this.show()
            }
            CaseState.DOWNLOAD_PLAYER_INFO_LOADING -> {
                mInfoText?.text = context.resources.getString(R.string.setting_download_player_list_loading)
                this.show()
            }
            CaseState.DOWNLOAD_PLAYER_INFO_SUCCESS -> {
                mInfoText?.text = context.resources.getString(R.string.setting_download_player_list_success) + context.resources.getString(R.string.dialog_race_list_size_test)+ pair.second +context.resources.getString(R.string.dialog_race_list_union_test)
                this.show()
            }
            CaseState.DOWNLOAD_PLAYER_INFO_ERROR -> {
                mInfoText?.text = context.resources.getString(R.string.setting_download_player_list_error)
                this.show()
            }
            CaseState.DOWNLOAD_PLAYER_INFO_EMPTY -> {
                mInfoText?.text = context.resources.getString(R.string.setting_download_player_list_empty)
                this.show()
            }
            CaseState.UPGRADE_LOADING -> {
                mInfoText?.text = context.resources.getString(R.string.setting_upgrading)
                this.show()
            }
            CaseState.UPGRADE_SUCCESS -> {
                mInfoText?.text = context.resources.getString(R.string.setting_upgrade_success)
                this.show()
            }
            CaseState.UPLOAD_LOADING-> {
                mInfoText?.text = context.resources.getString(R.string.main_upload_loading)
                this.show()
            }
            CaseState.UPLOAD_SUCCESS-> {
                mInfoText?.text = context.resources.getString(R.string.main_upload_success)
                this.show()
            }
            CaseState.UPLOAD_ERROR -> {
                mInfoText?.text = context.resources.getString(R.string.main_upload_error)
                this.show()
            }
            CaseState.UPLOAD_FAIL-> {
                mInfoText?.text = context.resources.getString(R.string.main_upload_fail)
                this.show()
            }
            CaseState.MAIN_READER_ON-> {
                mInfoText?.text = context.resources.getString(R.string.setting_chip_detection_cancel_info_text)
                this.show()
            }
            CaseState.REGISTERING->{
                mInfoText?.text = context.resources.getString(R.string.setting_registering)
                this.show()
            }
            CaseState.REGISTER_SUCCESS->{
                mInfoText?.text = context.resources.getString(R.string.setting_register_success)
                this.show()
            }
            CaseState.REGISTER_FAIL->{
                mInfoText?.text = context.resources.getString(R.string.setting_register_fail)
                this.show()
            }
            CaseState.QUERY_HISTORY->{
                mInfoText?.text = context.resources.getString(R.string.query_history)
                this.show()
            }
            CaseState.ON_QUERY_HISTORY_LOADED->{
                val text = context.resources.getString(R.string.query_history_loaded)
                mInfoText?.text = String.format(text,pair.second)
                this.show()
            }
            else -> {}
        }
        GlobalScope.launch {
            delay(5000)
            this@AlertDialogView.dismiss()
        }
    }

}