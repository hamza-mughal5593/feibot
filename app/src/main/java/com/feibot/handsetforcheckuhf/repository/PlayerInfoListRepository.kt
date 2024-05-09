package com.feibot.handsetforcheckuhf.repository

import com.feibot.handsetforcheckuhf.api.RetrofitClient

/**
 *@Author: Nick
 *@Description:
 *@Date 2021-07-03: 17:14
 */
class PlayerInfoListRepository {
    //获取赛事信息列表的数据(国外赛事)
    suspend fun getPlayerInfoListForGlobal(raceId:String) = RetrofitClient.getPlayerInfoListRetrofitForGlobalApi.getPlayerInfoListForGlobal(raceId)

    //获取赛事信息列表的数据(国内赛事)
    suspend fun getPlayerInfoList(raceId:String) = RetrofitClient.getPlayerInfoListRetrofitApi.getPlayerInfoList(raceId)

}