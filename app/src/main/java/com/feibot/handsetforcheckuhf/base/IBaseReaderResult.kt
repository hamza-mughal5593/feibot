package com.feibot.handsetforcheckuhf.base

import com.feibot.handsetforcheckuhf.bean.Epc

interface IBaseReaderResult {
    //当扫描结果EPC出来以后
    fun onResultRead()
}