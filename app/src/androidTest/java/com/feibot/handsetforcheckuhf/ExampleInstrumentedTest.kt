package com.feibot.handsetforcheckuhf

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.feibot.handsetforcheckuhf.utils.LogUtils

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.feibot.handsetforcheckuhf", appContext.packageName)
    }

    @Test
    fun test1(){
        Log.d("this", "test1: 11111")
    }

    @Test
    fun test2(){
        val tempTime = System.currentTimeMillis()
        Log.d("this", "test2: $tempTime")
    }

}