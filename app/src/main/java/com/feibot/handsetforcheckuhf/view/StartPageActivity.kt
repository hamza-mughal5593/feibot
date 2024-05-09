package com.feibot.handsetforcheckuhf.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.feibot.handsetforcheckuhf.MainActivity
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.Tools.PermissionManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *@Author: Nick
 *@Description:
 *@Date 2021-07-20: 15:58
 */
class StartPageActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_page)
        //动态获取权限
        PermissionManager.askPermissionManager(this)
        initView()
    }

    private fun initView() {
        GlobalScope.launch {
            delay(1000)
            startActivity(Intent(this@StartPageActivity, MainActivity::class.java))
            finish()
        }
    }
/*
* @Author: nick
* @Description: 隐藏导航栏
* @DateTime: 2021-06-10 08:36
* @Params:
* @Return
*/
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus){
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

}