package com.feibot.handsetforcheckuhf.repository.data.dao

import androidx.room.*
import com.feibot.handsetforcheckuhf.repository.data.entity.EpcEntity

@Dao
interface EpcDao {

    //查询数据库记录数
    @Query("SELECT COUNT('id') FROM EpcEntity WHERE upload_flag = 0")
    fun countByRecords():Int

    //删除已经上传的记录
    @Query("DELETE FROM EpcEntity WHERE upload_flag = 1")
    fun deleteAll()

    //删除未上传前5000条记录
    @Query("DELETE FROM EpcEntity WHERE upload_flag = 0 AND id IN (SELECT id FROM EpcEntity ORDER BY id LIMIT 5000)" )
    fun deleteMaxSizeRecords()

    //查询所有列表
    @get:Query("SELECT * FROM EpcEntity WHERE upload_flag = 0")
    val getAllByUnUpload:List<EpcEntity?>?

    //根据未上传标识查询该赛事列表
    @Query("SELECT * FROM EpcEntity WHERE upload_flag = (:uploadFlag) AND race_id = (:raceID)")
    fun getUnUploadListByFlag(raceID:String,uploadFlag:Int):List<EpcEntity?>?

    //插入未上传的列表 这个插入仅仅是替换
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUnUploadEpc(epcEntity:List<EpcEntity>?)

    //更新上传状态
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateEpcListUploadState(epcEntity:List<EpcEntity>?)

    //查询出未上传的列表的不重复显示
    @Query("SELECT id,epc,COUNT(count) AS count ,race_id,bib_number,time_stamp,upload_flag,create_time,update_time FROM EpcEntity WHERE race_id = (:raceID) AND upload_flag = 0 GROUP BY epc")
    fun getUnUploadListByFlagAndGroupBy(raceID: String):List<EpcEntity?>?

}