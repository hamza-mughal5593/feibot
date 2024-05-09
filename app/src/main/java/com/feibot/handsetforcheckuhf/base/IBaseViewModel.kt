package com.feibot.handsetforcheckuhf.base

interface IBaseViewModel {
    fun onReaderResultLoaded(epc:String)
    fun onReaderSingleResultLoaded(epc:String)
}