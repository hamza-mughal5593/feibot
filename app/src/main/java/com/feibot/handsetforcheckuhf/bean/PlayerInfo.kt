package com.feibot.handsetforcheckuhf.bean
/*
* @Author: nick
* @Description: 参赛者的信息Bean
* @DateTime: 2021-07-03 16:50
* @Params: 
* @Return 
*/
data class PlayerInfo(
    val all_num: Int,
    val code: String,
    val msg: String,
    val print: String,
    val runner: List<Runner>,
    val sended_num: Int
){
    data class Runner(
        val age: Int,
        val area: Any,
        val bib: String,
        val bibsended: Any,
        val epc: String,
        val gender: String,
        val id: Int,
        val id_card: Any,
        val item: Item,
        val item_id: Int,
        val item_title: String,
        val name: String,
        val phone: Any,
        val race_id: Int,
        val sex: String,
        val signed: Any,
        val t_shirt: Any,
        val team: String = "",
        val type: Any,
        val window: Any
    )
    
    data class Item(
        val created_at: String,
        val id: Int,
        val race_id: Int,
        val title: String,
        val updated_at: String
    )
}