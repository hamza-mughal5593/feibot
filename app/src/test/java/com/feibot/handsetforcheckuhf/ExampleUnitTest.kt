package com.feibot.handsetforcheckuhf

import com.feibot.handsetforcheckuhf.utils.LogUtils
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.junit.Test

import org.junit.Assert.*
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import kotlin.math.log

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    private var json: String? = null

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test1(){
//        val mCommonApi = CommonApi()
//        // TODO Auto-generated method stub
//        mCommonApi.setGpioDir(86, 0)
//        mCommonApi.getGpioIn(86)
//        GlobalScope.launch {
//            delay(500)
//            mCommonApi.setGpioDir(86, 1)
//            mCommonApi.setGpioOut(86, 1)
//        }
    }

    @Test
    fun test2(){
        val str = "https://timedatas.oss-cn-beijing.aliyuncs.com/feibotTime/Config/ec_1024.ecg"
        val index = str.lastIndexOf("/")
        val fileName = str.substring(index + 1, str.length)
        println(fileName)
    }
    @Test
    fun test3(){
        GlobalScope.launch(Dispatchers.IO) {


            val path ="https://time.running8.com/api/send-bibs?marathon_id=1099&mode=print&idcard=610121199305026431"
            val client =  OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build()
            val request = Request.Builder().url(path).build()
            val call = client.newCall(request)
            call.enqueue(object :okhttp3.Callback{
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    e.printStackTrace()
                }
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.code == HttpURLConnection.HTTP_OK){
                        this@ExampleUnitTest.json = response.body?.string()!!
                    }
                }
            })
        }
    //println(json)

//        val json = "{\"code\":\"ok\",\"msg\":\"\\u5df2\\u7ecf\\u6253\\u5370\\u8fc7\\u4e86\",\"print\":\"no\",\"runner\":{\"id\":607803,\"race_id\":1099,\"item_id\":2822,\"name\":\"\\u5f20\\u4e09\",\"gender\":\"F\",\"id_card\":\"610121199305026431\",\"phone\":null,\"t_shirt\":\"M\",\"age\":0,\"window\":\"\\u963f\\u5f25\\u5973\\u8db3\",\"bibsended\":\"2021-06-23T07:54:02.000000Z\",\"epc\":null,\"type\":null,\"area\":null,\"bib\":\"A988\",\"team\":\"\\u7f8e\\u56e2\",\"signed\":null,\"item_title\":\"\\u534a\\u7a0b\\u9a6c\\u62c9\\u677e\",\"sex\":\"F\",\"item\":{\"id\":2822,\"race_id\":1099,\"title\":\"\\u534a\\u7a0b\\u9a6c\\u62c9\\u677e\",\"created_at\":\"2021-05-27T07:50:32.000000Z\",\"updated_at\":\"2021-05-27T07:50:32.000000Z\"}},\"sended_num\":1,\"all_num\":4}    "
//        val jsonStr = JSONObject(json)
//        println(jsonStr.getString("code"))
    }
    @Test
    fun testDiffSize(){
        val diff = 20
        val size = 0
        println(size % diff)
    }

    @Test
    fun testArrayFilter(){
        val array = arrayListOf(1,2,3,4,5,6,7,8,9,10)
        val list = array.filterIndexed { index, _ ->
            index in 0 until 5
        }
        println(list)
    }

}