package com.feibot.handsetforcheckuhf.bean
/*
* @Author: nick
* @Description: 参赛者的信息 国外用户
* @DateTime: 2021-07-22 17:59
* @Params: 
* @Return 
*/
data class PlayerInfoForGlobal(
    val code: String,
    val msg: String,
    val runners: List<Runner>
)
data class Runner(
    val age: Any,
    val age_int: Any,
    val bib: String,
    val birthday: Any,
    val city: Any,
    val country: Any,
    val cp1: Any,
    val cp2: Any,
    val cp3: Any,
    val cp4: Any,
    val cp5: Any,
    val cp6: Any,
    val cp7: Any,
    val cp8: Any,
    val created_at: String,
    val email: Any,
    val epc: String,
    val id: Int,
    val id_card: Any,
    val invalid: String,
    val item: Item,
    val item_id: Int,
    val locking: Int,
    val name: String,
    val phone: Any,
    val race_id: Int,
    val score10k: Any,
    val score15k: Any,
    val score20k: Any,
    val score25k: Any,
    val score30k: Any,
    val score35k: Any,
    val score40k: Any,
    val score5k: Any,
    val sex: String,
    val smsed: Int,
    val team: Any,
    val type: Any,
    val updated_at: String,
    val window: Any
)
data class Item(
    val certificate: Int,
    val created_at: String,
    val id: Int,
    val race_id: Int,
    val result: Int,
    val title: String,
    val updated_at: String
)