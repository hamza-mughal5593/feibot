package com.feibot.handsetforcheckuhf.bean

data class Epc(
    var id: Int,
    var epc: String,
    var count: Int,
    var timeStamp: String,
    var bibNumber:String = ""
)
