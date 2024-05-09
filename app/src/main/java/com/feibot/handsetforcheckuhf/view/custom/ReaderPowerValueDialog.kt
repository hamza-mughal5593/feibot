package com.feibot.handsetforcheckuhf.view.custom

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.Tools.ReadTagToolPlus
import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.feibot.handsetforcheckuhf.utils.UIHelper
import kotlinx.android.synthetic.main.reader_value_dialog_view.*

/**
 *@Author: Nick
 *@Description:
 *@Date 2021-07-01: 15:50
 */
class ReaderPowerValueDialog(context: Context):Dialog(context, R.style.Dialog_Fullscreen) {
    private lateinit var adapter: ArrayAdapter<CharSequence>
    private var mCallback: OnFinishSetPowerValue? = null
    private var mDefaultReaderPowerValue = 16
    private lateinit var mShp: SharedPreferences
    private var mShpForReaderPower: Int? = null
    private val mReader = ReadTagToolPlus.getInstance().mReader


/**
* @Author: nick
* @Description: 创建时Dialog时
* @DateTime: 2021-07-01 16:35
* @Params: 
* @Return 
*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reader_value_dialog_view)
        initView()
        initData()
        initEvent()
    }

    private fun initView() {
        val ver: String? = mReader?.version
        var arrPow = R.array.arrayPower
        if (ver != null && ver.contains("RLM")) {
            arrPow = R.array.arrayPower2
        }
        val adapter: ArrayAdapter<*> =
            ArrayAdapter.createFromResource(context, arrPow, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spPower.adapter = adapter
    }


/*
* @Author: nick
* @Description: 从Shp里取相应的值
* @DateTime: 2021-07-01 16:35
* @Params: 
* @Return 
*/
    private fun initData() {
        mShp = context.getSharedPreferences("deviceInfo", 0)
        mShpForReaderPower = mShp.getInt("reader_power", 16)
        mDefaultReaderPowerValue = mShpForReaderPower!!
    }

    /*
* @Author: nick
* @Description: 设置相关的numberPicker监听数据
* @DateTime: 2021-07-01 16:39
* @Params: 
* @Return
*/
    @SuppressLint("CommitPrefEdits")
    private fun initEvent() {
        //取消按钮监听
        this.readerPowerValueBackIv.setOnClickListener{
            this.dismiss()
        }
        //设置频率点击事件
        this.BtSetFre.setOnClickListener {
            if (ReadTagToolPlus.getInstance().mReader!!.setFrequencyMode(this.spinnerMode.selectedItemPosition)) {
                LogUtils.d(this, "设置频率成功!")
                UIHelper.ToastMessage(context, "Set the power success")
            } else {
                LogUtils.d(this, "设置频率失败!")
                UIHelper.ToastMessage(context, "Set the power failure")
            }
        }
        //读取频率点击事件
        this.BtGetFre.setOnClickListener {
            val idx: Int = ReadTagToolPlus.getInstance().mReader!!.frequencyMode

            if (idx != -1) {
                val count: Int = this.spinnerMode.count
                this.spinnerMode.setSelection(if (idx > count - 1) count - 1 else idx)
            } else {
                UIHelper.ToastMessage(context, "Set the power failure")
            }
        }
        //工作模式下拉列表点击选中item监听
        spinnerMode.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 3) {
                    ll_freHop.visibility = View.VISIBLE
                    rb_America.isChecked = true //默认美国频点
                } else if (position != 3) {
                    ll_freHop.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        this.btnSetPower.setOnClickListener {
            val iPower = spPower.selectedItemPosition + 5
            if (mReader?.setPower(iPower)!!) {
                UIHelper.ToastMessage(context, "Set the power success")
            } else {
                UIHelper.ToastMessage(context, "Set the power failure")
            }
        }

        this.btnGetPower.setOnClickListener {
            val iPower: Int = mReader?.power!!
            if (iPower > -1) {
                val position = iPower - 5
                val count = spPower.count
                spPower.setSelection(if (position > count - 1) count - 1 else position)

                // UIHelper.ToastMessage(mContext,
                // R.string.uhf_msg_read_power_succ);
            } else {
                UIHelper.ToastMessage(context, "Read the power failure")
            }
        }

        rb_America.setOnClickListener {
            adapter = ArrayAdapter.createFromResource(
                context,
                R.array.arrayFreHop_us,
                android.R.layout.simple_spinner_item
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spFreHop.adapter = adapter
        }

        rb_Others.setOnClickListener {
            adapter = ArrayAdapter.createFromResource(
                context,
                R.array.arrayFreHop,
                android.R.layout.simple_spinner_item
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spFreHop.adapter = adapter
        }

        btnSetFreHop.setOnClickListener {
            val view = spFreHop.selectedView
            if (view is TextView) {
                val freHop = view.text.toString().trim { it <= ' ' }
                setFreHop(java.lang.Float.valueOf(freHop)) //设置频点
            }
        }
    }

/**
* 设置频点
*
* @param value 频点数值
* @return 是否设置成功
*/
    private fun setFreHop(value: Float): Boolean {
        val result: Boolean = mReader!!.setFreHop(value)
        if (result) {
            UIHelper.ToastMessage(context, "Set the frequency Hopping success")
        } else {
            UIHelper.ToastMessage(context, "Set the frequency Hopping failure")
        }
        return result
    }

/*
* @Author: nick
* @Description: 暴露方法给外部
* @DateTime: 2021-07-01 16:39
* @Params: 
* @Return 
*/
    fun setOnFinishSetPowerValue(callback: OnFinishSetPowerValue) {
        mCallback = callback
    }

/*
* @Author: nick
* @Description: 暴露接口
* @DateTime: 2021-07-01 16:39
* @Params: 
* @Return 
*/
    interface OnFinishSetPowerValue{
        fun onPowerValueSet(value: Int)
    }
}