package com.feibot.handsetforcheckuhf.repository.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EpcEntity(
    @PrimaryKey(autoGenerate = true)
    var id:Int,
    var epc:String,
    var count:String,
    var race_id:String,
    var bib_number:String,
    var time_stamp:String,
    var upload_flag:Int,
    var create_time:String,
    var update_time:String
)
