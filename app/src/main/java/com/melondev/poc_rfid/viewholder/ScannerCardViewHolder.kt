package com.melondev.poc_rfid.viewholder

import android.widget.RelativeLayout
import android.widget.TextView
import com.melondev.poc_rfid.databinding.ScannerCardBinding
import com.melondev.poc_rfid.model.BaseModel
import com.melondev.poc_rfid.model.TagModel

class ScannerCardViewHolder(binding: ScannerCardBinding) :
    BaseViewHolder<BaseModel>(binding.root) {
    val nameView: TextView = binding.scannerCardName
    val addressView: TextView = binding.scannerCardAddress
    val rssiView: TextView = binding.scannerCardRssi
    val layoutView: RelativeLayout = binding.scannerCardLayout

    fun bind(tag: TagModel) {
        nameView.text = tag.name ?: "ไม่ทราบชี่อ"
        addressView.text = "ไอดี: ${(tag.address ?: "ไม่ทราบ")}"

        rssiView.text = "ความแรงสัญญาณ: ${(tag.rssi ?: "ไม่ทราบ")}"

        

        layoutView.setOnClickListener {
            tag.callback?.onClick(tag)
        }

    }

}