package com.feibot.handsetforcheckuhf.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 *@Author: Nick
 *@Description:一个基础BaseFragment的实现类
 *@Date 2021-06-09: 16:56
 */
abstract class BaseFragment:Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(setViewID(),container,false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
        initTools()
        initObserve()
        initService()
        initEvent()
    }

    protected open fun initTools(){}

    //子类实现设置页面布局ID
    protected abstract fun setViewID():Int

    abstract fun initObserve()

    abstract fun initEvent()

    protected open fun initService(){}
    protected open fun initViewModel(){}
    protected open fun initView(){}
}