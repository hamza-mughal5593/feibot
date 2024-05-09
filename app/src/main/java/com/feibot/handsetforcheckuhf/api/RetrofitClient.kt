package com.feibot.handsetforcheckuhf.api

import android.annotation.SuppressLint
import com.feibot.handsetforcheckuhf.contants.URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("TrustAllX509TrustManager")
object RetrofitClient {
    //客户端不对服务器证书做任何验证
    fun getSSLSocketFactory(): SSLSocketFactory? {
        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(
            null, trustAllCerts,
            SecureRandom()
        )
        // Create an ssl socket factory with our all-trusting manager
        return sslContext.socketFactory
    }
        @SuppressLint("TrustAllX509TrustManager")
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {

            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>,authType: String) {}

            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>,authType: String) {}

            override fun getAcceptedIssuers(): Array<X509Certificate?> {return arrayOfNulls(0)}
        })

//基本okHttpClient客户端
    private val okHttpClient = OkHttpClient
        .Builder()
        .sslSocketFactory(getSSLSocketFactory()!!,(trustAllCerts[0] as X509TrustManager))
        .callTimeout(3000, TimeUnit.SECONDS)
        .build()
//基本okHttpClient重试3次客户端
    private val okHttpRetryClient = OkHttpClient
        .Builder()
        .sslSocketFactory(getSSLSocketFactory()!!,(trustAllCerts[0] as X509TrustManager))
        //开启重试
        .retryOnConnectionFailure(true)
        //重试的拦截器
        .addInterceptor {
            OkHttpInterceptor().intercept(it)
        }
        .callTimeout(3000, TimeUnit.SECONDS)
        .build()

//基本retrofit客户端 判断是否为中国语言环境
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(if(URL.isChineseLanguage()) URL.BASE_URL else URL.GLOBAL_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

//基本上传EpcRetrofit客户端 判断是否为中国语言环境
    private val uploadEpcRetrofit:Retrofit = Retrofit.Builder()
        .baseUrl(if(URL.isChineseLanguage()) URL.BASE_URL else URL.GLOBAL_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpRetryClient)
        .build()

//下载赛事列表客户端(国内版)
    private val playerInfoListRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(URL.PLAYER_INFO_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

//下载赛事列表客户端(国外版)
    private val playerInfoListRetrofitForGlobal: Retrofit = Retrofit.Builder()
        .baseUrl(URL.PLAYER_INFO_GLOBAL_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
    //升级程序客户端
    private val upgradeVersionClient = Retrofit.Builder()
        .baseUrl(URL.UPGRADE_VERSION_URL)
        .client(okHttpClient)
        .build()
    //上传CSV文件客户端
    private val uploadCSVRetrofit:Retrofit = Retrofit.Builder()
        .baseUrl(if(URL.isChineseLanguage()) URL.CHINA_UPLOAD_URL else URL.GLOBAL_UPLOAD_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpRetryClient)
        .build()

    val retrofitApi: RetrofitApi = retrofit.create(RetrofitApi::class.java)
    val uploadRetrofitApi: RetrofitApi = uploadEpcRetrofit.create(RetrofitApi::class.java)
    val getPlayerInfoListRetrofitApi: RetrofitApi = playerInfoListRetrofit.create(RetrofitApi::class.java)
    val getPlayerInfoListRetrofitForGlobalApi: RetrofitApi = playerInfoListRetrofitForGlobal.create(RetrofitApi::class.java)
    val upgradeVersionApi: RetrofitApi = upgradeVersionClient.create(RetrofitApi::class.java)
    val uploadCSVRetrofitApi: RetrofitApi = uploadCSVRetrofit.create(RetrofitApi::class.java)
}