package com.feibot.handsetforcheckuhf.bean

data class RegisterDevice(
    val code: String,
    val machine: Machine,
    val msg: String
){
    data class Machine(
        val close_at: String,
        val created_at: String,
        val id: Int,
        val latitude: String,
        val longitude: String,
        val mac: String,
        val macCode: String,
        val name_no: String,
        val open_at: String,
        val race_id: Int,
        val reader_1: Int,
        val reader_2: Int,
        val reader_control: Int,
        val timer_id: Int,
        val type: String,
        val updated_at: String
    )
}

