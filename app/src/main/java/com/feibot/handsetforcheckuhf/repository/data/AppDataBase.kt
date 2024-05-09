package com.feibot.handsetforcheckuhf.repository.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.feibot.handsetforcheckuhf.base.BaseApplication
import com.feibot.handsetforcheckuhf.repository.data.dao.EpcDao
import com.feibot.handsetforcheckuhf.repository.data.entity.EpcEntity

/**
 *@Author: Nick
 *@Description:创建一个数据库实例
 *@Date 2021-07-06: 09:57
 */
@Database(entities = [EpcEntity::class],version = 1,exportSchema = false)
abstract class AppDataBase:RoomDatabase() {
    //单例模式
    companion object{
        private var mInstance:AppDataBase? = null
        get() {
            if (field == null){
                field = Room
                    .databaseBuilder(BaseApplication.context(),AppDataBase::class.java,"hander.db")
                    .allowMainThreadQueries()
                    .build()
            }
            return field
        }
        @Synchronized
        fun getInstance():AppDataBase{
            return mInstance!!
        }
    }
    abstract fun epcDao():EpcDao?
}