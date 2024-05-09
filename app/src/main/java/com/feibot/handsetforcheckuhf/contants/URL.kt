package com.feibot.handsetforcheckuhf.contants

import com.feibot.handsetforcheckuhf.base.BaseApplication

object URL {
    ////////////////////////////////↓国内//////////////////////////////////////////
        //国内地址
        const val BASE_URL = "https://newtime.run8.cn/time/api/"
        //国内下载赛事名单的地址
        const val PLAYER_INFO_BASE_URL = "https://time.running8.com/"
        //国内上传csv文件包
        const val CHINA_UPLOAD_URL = "https://newtime.run8.cn/api/time/"
    ////////////////////////////////↑国内//////////////////////////////////////////


    ////////////////////////////////↓国外//////////////////////////////////////////
        //国外赛事API地址
        const val GLOBAL_URL = "https://time.feibot.com/time/api/"
        //国外赛事系统下载名单的地址
        const val PLAYER_INFO_GLOBAL_URL = "https://time.feibot.com/api/"
        //国外上传csv压缩包
        const val GLOBAL_UPLOAD_URL = "https://time.feibot.com/api/time/"
    ////////////////////////////////↑国外//////////////////////////////////////////

    //升级系统下载包的地址
    const val UPGRADE_VERSION_URL = "http://racepower.feibot.com/timingPlatform/"





    //判断是否是中文语言环境
    fun isChineseLanguage():Boolean{
        return BaseApplication.context().resources.configuration.locale.language.endsWith("zh")
    }

}