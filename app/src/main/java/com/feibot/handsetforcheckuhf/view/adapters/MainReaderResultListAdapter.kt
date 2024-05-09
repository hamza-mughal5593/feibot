package com.feibot.handsetforcheckuhf.view.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.feibot.handsetforcheckuhf.R
import com.feibot.handsetforcheckuhf.bean.Epc
import com.feibot.handsetforcheckuhf.utils.LogUtils
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.reader_result_item.view.*

/**
 *@Author: Nick
 *@Description:扫描结果的列表适配器
 *@Date 2021-06-10: 10:56
 */
class MainReaderResultListAdapter:RecyclerView.Adapter<MainReaderResultListAdapter.InnerHolder>() {
    private val mList = arrayListOf<Epc>()
    private var mParentView:RecyclerView? = null

    class InnerHolder(view: View):RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerHolder {
        val view = View.inflate(parent.context,R.layout.reader_result_item,null)
        mParentView = parent as RecyclerView
        return InnerHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: InnerHolder, position: Int) {
        holder.itemView.apply {
            //设置序号
            orderNumberTv.text = (position + 1).toString()
            //设置时间
            timeTv.text = mList[position].timeStamp
            //设置Epc
            epcTv.apply {
                isSelected = true
                text = mList[position].epc
            }
            //设置参赛号
            bibNumberTv.text = mList[position].bibNumber
            //设置次数
            countTv.text = mList[position].count.toString()
        }
    }
    override fun getItemCount(): Int {
        return mList.size
    }
/*
* @Author: nick
* @Description: 设置读取后的列表
* @DateTime: 2021-06-19 15:03
* @Params:
* @Return
*/
    fun setData(epc:List<Epc>){
        mList.clear()
        mList.addAll(epc)
        notifyDataSetChanged()
        mParentView?.scrollToPosition(this.itemCount -1)
    }
}