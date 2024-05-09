package com.feibot.handsetforcheckuhf.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 *@Author: Nick
 *@Description:网络错误 重试 的拦截器
 *@Date 2021-07-26: 08:24
 */
class OkHttpInterceptor:Interceptor {
    private var retryNum = 2
    private var maxRetry = 3

    override fun intercept(chain: Interceptor.Chain): Response {
        val  request = chain.request()
        var  response = chain.proceed(chain.request())

        while (!response.isSuccessful && retryNum < maxRetry) {
            retryNum ++
            response = chain.proceed(request)
        }
        if (retryNum == 3){
            response.close()
        }
        return response
    }
}