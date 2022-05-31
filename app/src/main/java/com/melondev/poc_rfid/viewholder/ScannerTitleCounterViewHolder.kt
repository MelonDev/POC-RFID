package com.melondev.poc_rfid.viewholder

import android.widget.TextView
import com.melondev.poc_rfid.databinding.ScannerTitleBinding
import com.melondev.poc_rfid.model.BaseModel
import com.melondev.poc_rfid.model.TitleCounterModel

class ScannerTitleCounterViewHolder(binding: ScannerTitleBinding) :
    BaseViewHolder<BaseModel>(binding.root) {
    val title: TextView = binding.scannerTitleText
    val counter: TextView = binding.scannerCounterText


    fun bind(item: TitleCounterModel) {
        title.text = item.name
        counter.text = "${item.count} ต้น"
    }

}