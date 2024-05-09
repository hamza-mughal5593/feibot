package com.feibot.handsetforcheckuhf.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.IOException
import java.lang.Exception
import java.net.ConnectException
import java.net.HttpURLConnection

/**
 *@Author: Nick
 *@Description:请求相关的支持DSL语法的接收者
 *@Date 2021-06-18: 09:46
 */
class RetrofitCoroutineDSL<T> {
    var api: (Call<T>)? = null
    internal var onSuccess: ((T,call:Call<T>) -> Unit)? = null
        private set
    internal var onFail: ((msg: String, errorCode: Int) -> Unit)? = null
        private set
    internal var onUploadFail: ((msg: String, errorCode: Int) -> Unit)? = null
        private set
    internal var onComplete: (() -> Unit)? = null
        private set

    /**
     * 获取数据成功
     * @param block (T) -> Unit
     */
    fun onSuccess(block: (T,call:Call<T>) -> Unit) {
        this.onSuccess = block
    }

    /**
     * 获取数据失败
     * @param block (msg: String, errorCode: Int) -> Unit
     */
    fun onFail(block: (msg: String, errorCode: Int) -> Unit) {
        this.onFail = block
    }

    /**
     * 上传数据失败
     * @param block (msg: String, errorCode: Int) -> Unit
     */
    fun onUploadFail(block: (msg: String, errorCode: Int) -> Unit) {
        this.onUploadFail = block
    }

    /**
     * 访问完成
     * @param block () -> Unit
     */
    fun onComplete(block: () -> Unit) {
        this.onComplete = block
    }

    internal fun clean() {
        onSuccess = null
        onComplete = null
        onFail = null
        onUploadFail = null
    }
}
fun <T> CoroutineScope.retrofit(dsl: RetrofitCoroutineDSL<T>.() -> Unit) {
    //在主线程中开启协程
    this.launch(Dispatchers.Main) {
        val coroutine = RetrofitCoroutineDSL<T>().apply(dsl)
        coroutine.api?.let { call ->
            //async 并发执行 在IO线程中
            val deferred = async(Dispatchers.IO) {
                try {
                    call.execute() //已经在io线程中了，所以调用Retrofit的同步方法
                } catch (e: ConnectException) {
                    coroutine.onFail?.invoke("网络断开", -1)
                    coroutine.onUploadFail?.invoke("上传失败!", -1)
                    null
                } catch (e: Exception) {
                    coroutine.onFail?.invoke("${call.request().url}网络延迟!", -1)
                    coroutine.onUploadFail?.invoke("上传失败!", -1)
                    null
                }
            }
            //当协程取消的时候，取消网络请求
            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                    coroutine.clean()
                }
            }
            //await 等待异步执行的结果
            val response = deferred.await()
            if (response == null) {
                coroutine.onFail?.invoke("返回为空", -1)
            } else {
                response.let {
                    if (response.isSuccessful) {
                        //访问接口成功
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            //判断status 为1 表示获取数据成功
                            coroutine.onSuccess?.invoke(response.body()!!,call)
                        } else {
                            coroutine.onFail?.invoke(response.message()?: "返回数据为空", response.code())
                        }
                    } else {
                        coroutine.onFail?.invoke(response.message().toString(), response.code())
                    }
                }
            }
            coroutine.onComplete?.invoke()
        }
    }
}