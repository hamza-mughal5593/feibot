package com.feibot.handsetforcheckuhf.utils

import com.tencent.mmkv.MMKV

/**
 *@Author: Nick
 *@Description:MMKV工具类
 *@Date 2021-07-05: 10:25
 */
class MMKVUtils {
    companion object {
        private var mv: MMKV? = MMKV.defaultMMKV()
        private var mInstance: MMKVUtils? = null
            get() {
                if (field == null) {
                    field = MMKVUtils()
                }
                return field
            }
        @Synchronized
        fun getInstance(): MMKVUtils {
            return mInstance!!
        }
    }

    /*
    * @Author: nick
    * @Description: MMKV执行编码
    * @DateTime: 2021-05-17 11:39
    * @Params:
    * @Return
    */
    fun encode(key: String, `object`: Any) {
        if (`object` is String) {
            mv?.encode(key, `object`)
        } else if (`object` is Int) {
            mv?.encode(key, `object`)
        } else if (`object` is Boolean) {
            mv?.encode(key, `object`)
        } else if (`object` is Float) {
            mv?.encode(key, `object`)
        } else if (`object` is Long) {
            mv?.encode(key, `object`)
        } else if (`object` is Double) {
            mv?.encode(key, `object`)
        } else if (`object` is ByteArray) {
            mv?.encode(key, `object`)
        } else {
            mv?.encode(key, `object`.toString());
        }
    }

    /*
    * @Author: nick
    * @Description: mmkv执行读取操作
    * @DateTime: 2021-05-17 11:43
    * @Params:
    * @Return
    */
//-------------------------------以下↓是解码过程-------------------------------------------------
    fun decodeInt(key :String):Int{
        return mv!!.decodeInt(key, 0)
    }

    fun decodeDouble(key:String):Double {
        return mv!!.decodeDouble(key, 0.00)
    }

    fun  decodeLong(key:String):Long {
        return mv!!.decodeLong(key, 0L)
    }

    fun  decodeBoolean(key: String):Boolean{
        return mv!!.decodeBool(key, false)
    }

    fun  decodeFloat(key: String):Float {
        return mv!!.decodeFloat(key, 0F)
    }

    fun decodeBytes(key: String):ByteArray{
        return mv!!.decodeBytes(key)
    }

    fun decodeString(key: String):String {
        return mv!!.decodeString(key, "")
    }


}