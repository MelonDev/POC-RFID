package com.melondev.poc_rfid.adapters

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

import com.melondev.poc_rfid.databinding.FragmentScannerBinding
import com.melondev.poc_rfid.model.TagModel

class ScannerRecyclerViewAdapter : RecyclerView.Adapter<ScannerRecyclerViewAdapter.ViewHolder>() {

    var data: MutableList<TagModel>? = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentScannerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        data?.let { data->
            val tag: TagModel = data[position]
            holder.nameView.text = tag.name ?: "ไม่ทราบชี่อ"
            holder.addressView.text = "ไอดี: " + (tag.address ?: "ไม่ทราบ")
            holder.rssiView.text = "ระยะห่าง: " + (tag.rssi ?: "ไม่ทราบ").toString()

            holder.layoutView.setOnClickListener {
                tag.callback?.let {
                    it.onClick(tag)
                }
            }
        }

    }

    override fun getItemCount(): Int = data?.size ?: 0

    inner class ViewHolder(binding: FragmentScannerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val nameView: TextView = binding.scannerName
        val addressView: TextView = binding.scannerAddress
        val rssiView: TextView = binding.scannerRssi
        val layoutView : RelativeLayout = binding.scannerLayout
    }

    fun add(tag: TagModel) {
        data?.let {
            if (!it.contains(tag)) {

                val position: Int = data?.size ?: 0
                it.add(tag)
                this.notifyItemInserted(position)

            }
        }
    }

    fun addAll(data: MutableList<TagModel>){
        this.data = data
        this.notifyDataSetChanged()
    }


    fun clear() {
        data?.clear()
        this.notifyDataSetChanged()
    }

}