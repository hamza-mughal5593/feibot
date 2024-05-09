package com.feibot.handsetforcheckuhf.services

import android.os.Environment
import com.feibot.handsetforcheckuhf.base.BaseApplication
import com.feibot.handsetforcheckuhf.bean.Epc
import com.feibot.handsetforcheckuhf.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

/**
 *@Author: Nick
 *@Description:处理Csv数据
 *@Date 2021-07-15: 10:58
 */
class ReaderResultToCSV {
    private var mFileName = ""
/*
* @Author: nick
* @Description: 单例模式
* @DateTime: 2021-07-15 11:09
* @Params: 
* @Return 
*/
    companion object{
        private var mInstance: ReaderResultToCSV? = null
            get() {
                if (field == null){
                    field = ReaderResultToCSV()
                }
                return field
            }
        @Synchronized
        fun getInstance():ReaderResultToCSV{
            return mInstance!!
        }
    }
    init {
        mFileName = getFileName()
    }
/*
* @Author: nick
* @Description: 获取文件名称
* @DateTime: 2021-07-15 11:24
* @Params: 
* @Return 
*/
    private fun getFileName():String{
        val shp = BaseApplication.context().getSharedPreferences("deviceInfo",0)
        return shp.getString("csv_file_name","H002_1024_record.csv ")!!
    }
/*
* @Author: nick
* @Description: 设置EpC写文件流操作
* @DateTime: 2021-07-15 11:10
* @Params:
* @Return
*/
    fun setDataToFile(epc:String,timeStamp:String){
        val epcInfo = "$timeStamp:$epc\n"
        val file = File(BaseApplication.context().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), mFileName)
        var fos: FileOutputStream? = null
        var osw: OutputStreamWriter? = null
        //耗时操作处理 开启IO线程
        GlobalScope.launch(Dispatchers.IO){


            try {
                fos = if (!file.exists()) {
                    file.createNewFile()
                    FileOutputStream(file)
                } else {
                    FileOutputStream(file, true)
                }
                osw = OutputStreamWriter(fos, "utf-8")
                //写入内容
                osw?.write(epcInfo)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {   //关闭流
                try {
                    osw?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    fos?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}