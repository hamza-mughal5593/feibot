package com.feibot.handsetforcheckuhf.view.custom

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.NumberPicker
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.Tools.TimeStamp
import com.feibot.handsetforcheckuhf.utils.LogUtils
import kotlinx.android.synthetic.main.number_picker_confirm_dialog.*
import java.sql.Timestamp

/**
 *@Author: Nick
 *@Description:
 *@Date 2021-06-30: 10:53
 */
class ChangeTimeNumberPickerConfirmDiaLog(context: Context):Dialog(context,R.style.Dialog_Fullscreen) {
    private var mCallback: OnFinishSetTime? = null
    private var mMillisecondPicker: NumberPicker? = null
    private var mSecondPicker: NumberPicker? = null
    private var mMinutePicker: NumberPicker? = null
    private var mHourNumberPicker: NumberPicker? = null
    private var mYear = 2000
    private var mMonth = 1
    private var mDay = 1
    private var mHour = 1
    private var mMinute = 1
    private var mSecond = 1
    private var mMillisecond = "100"
    //创建一个map用来保存修改后的时间
    private val timeMap = hashMapOf<String,Int>()
    private lateinit var mShp: SharedPreferences

/*
* @Author: nick
* @Description: 创建页面
* @DateTime: 2021-06-30 14:23
* @Params: 
* @Return 
*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.number_picker_confirm_dialog)
        initView()
        initEvent()
    }
/*
* @Author: nick
* @Description: 初始化视图
* @DateTime: 2021-06-30 14:23
* @Params: 
* @Return 
*/
    @SuppressLint("ResourceAsColor")
    private fun initView() {
        mHourNumberPicker = hourPicker
        mMinutePicker = minutePicker
        mSecondPicker = secondPicker
        mMillisecondPicker = millisecondPicker
        this.setCanceledOnTouchOutside(false)
        //shp
        mShp = context.getSharedPreferences("deviceInfo",0)
        val useNetTime = mShp.getBoolean("useNetTime",false)
        updateNumberPickerUI(useNetTime)
        networkTimeSwitch.isChecked = useNetTime
}
/*
* @Author: nick
* @Description: 初始化各种事件
* @DateTime: 2021-06-30 14:22
* @Params: 
* @Return 
*/
    @SuppressLint("CommitPrefEdits")
    private fun initEvent() {
        //当显示的时候获取当前时间
        this.setOnShowListener {
            //获取时间戳：时间格式是："HH:mm:ss:SSS"
            val timeStamp = TimeStamp.getMillis(0L,3)
            mHour = timeStamp?.substring(0,2)!!.toInt()
            mMinute = timeStamp.substring(3,5).toInt()
            mSecond = timeStamp.substring(6,8).toInt()
            mMillisecond = timeStamp.substring(9,11)
            //获取日期时间戳：日期格式是："yyyy-mm-dd"
            val dataStamp = TimeStamp.getMillis(0L,6)
            mYear = dataStamp?.substring(0,4)!!.toInt()
            mMonth = dataStamp.substring(5,7).toInt()
            mDay = dataStamp.substring(8,10).toInt()
            initTime()
            initDate()
        }
        //完成按钮监听
        finishBt.setOnClickListener {
            getTimeData()
            this.dismiss()
        }
        //退回按钮的返回
        setTimeBackIv.setOnClickListener {
            this.dismiss()
        }
        //使用网络时间Switch
        networkTimeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            //更新numberPicker状态
            updateNumberPickerUI(isChecked)
            //保存使用网络时间的shp
            mShp.edit().run {
                putBoolean("useNetTime",isChecked)
            }.apply()
        }
    }
/*
* @Author: nick
* @Description: 为numberPicker设置当前时间
* @DateTime: 2021-06-30 14:22
* @Params: 
* @Return 
*/
    private fun initTime() {
        timeMap["year"] = mYear
        timeMap["month"] = mMonth
        timeMap["day"] = mDay
        timeMap["hour"] = mHour
        timeMap["minute"] = mMinute
        timeMap["second"] = mSecond
        timeMap["millisecond"] = mMillisecond.toInt()
        //时针设置
        mHourNumberPicker?.apply {
            maxValue = 24
            minValue = 0
            wrapSelectorWheel = true
            value = mHour
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener { numberPicker, i, i2 ->
                timeMap["hour"] = i2
            }
        }
    //分针设置
        mMinutePicker?.apply {
            maxValue = 59
            minValue = 0
            wrapSelectorWheel = true
            value = mMinute
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener { numberPicker, i, i2 ->
                timeMap["minute"] = i2
            }
        }
    //秒针设置
        mSecondPicker?.apply {
            maxValue = 59
            minValue = 0
            wrapSelectorWheel = true
            value = mSecond
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener { numberPicker, i, i2 ->
                timeMap["second"] = i2
            }
        }
    //毫秒针设置
        mMillisecondPicker?.apply {
            maxValue = 9
            minValue = 0
            wrapSelectorWheel = true
            setFormatter {
                it.toString() + "00"
            }
            value = mMillisecond.substring(0,2).toInt()
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener { numberPicker, i, i2 ->
                timeMap["millisecond"] = (i2.toString() + "00").toInt()
            }
        }
    }

    private fun initDate(){
        //年份设置
        yearPicker?.apply {
            maxValue = 2050
            minValue = 2000
            value = mYear
            wrapSelectorWheel = true
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener { numberPicker, i, i2 ->
                timeMap["year"] = i2
            }
        }
        //月份设置
        monthPicker?.apply {
            maxValue = 12
            minValue = 1
            value = mMonth
            wrapSelectorWheel = true
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener { numberPicker, i, i2 ->
                timeMap["month"] = i2
            }
        }
        //日设置
        dayPicker?.apply {
            maxValue = 31
            minValue = 1
            value = mDay
            wrapSelectorWheel = true
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener { numberPicker, i, i2 ->
                timeMap["day"] = i2
            }
        }
    }

/*
* @Author: nick
* @Description: 更新日期时间显示UI状态
* @DateTime: 2021-07-14 10:12
* @Params:
* @Return
*/
    private fun updateNumberPickerUI(isUseNetTime:Boolean){
        //改变时间选择器状态
        datePickerGroup.isEnabled = !isUseNetTime
        numberTimePickerGroup.isEnabled = !isUseNetTime
        //按钮置灰
        finishBt.isEnabled = !isUseNetTime
        //修改时间差值为0
        mShp.edit().apply {
          putLong("off_time",0L)
        }.apply()
        //时间戳时间隔为0
        TimeStamp.run {
            offTime = 0L
            changeMillis = 0L
        }
    }

/*
* @Author: nick
* @Description: 为Map设置已经修改的数据 并 通知外部方法
* @DateTime: 2021-06-30 14:21
* @Params: 
* @Return 
*/
    private fun getTimeData(){
        mCallback?.onSetFinished(timeMap)
    }
/*
* @Author: nick
* @Description: 外部暴露接口的方法
* @DateTime: 2021-06-30 14:21
* @Params: 
* @Return 
*/
    fun setOnFinishSetTime(callback:OnFinishSetTime){
        mCallback = callback
    }

/*
* @Author: nick
* @Description: 暴露一个通知接口
* @DateTime: 2021-06-30 14:22
* @Params: 
* @Return 
*/
    interface OnFinishSetTime{
        fun onSetFinished(timeMap: HashMap<String, Int>)
    }
}