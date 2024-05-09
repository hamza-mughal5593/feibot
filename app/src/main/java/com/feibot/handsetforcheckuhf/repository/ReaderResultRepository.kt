package com.feibot.handsetforcheckuhf.repository

import com.feibot.handsetforcheckuhf.Tools.ReadTagToolPlus

/**
 *@Author: Nick
 *@Description:
 *@Date 2021-06-10: 11:30
 */
class ReaderResultRepository {

    /*
    * @Author: nick
    * @Description: 提取读卡Tool的读取数据·
    * @DateTime: 2021-06-15 10:30
    * @Params:
    * @Return
    */
    fun getReaderResult():String{
        return ReadTagToolPlus.getInstance().onReaderResultLoaded()
    }
}