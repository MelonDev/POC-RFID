package com.melondev.poc_rfid.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.melondev.poc_rfid.databinding.ScannerCardBinding
import com.melondev.poc_rfid.databinding.ScannerTitleBinding
import com.melondev.poc_rfid.databinding.UnknownItemBinding
import com.melondev.poc_rfid.model.BaseModel
import com.melondev.poc_rfid.model.TagModel
import com.melondev.poc_rfid.model.TitleCounterModel
import com.melondev.poc_rfid.typeholder.ScannerViewType
import com.melondev.poc_rfid.viewholder.BaseViewHolder
import com.melondev.poc_rfid.viewholder.ScannerCardViewHolder
import com.melondev.poc_rfid.viewholder.ScannerTitleCounterViewHolder
import com.melondev.poc_rfid.viewholder.UnknownViewHolder

class ScannerRecyclerViewAdapter : RecyclerView.Adapter<BaseViewHolder<BaseModel>>() {

    var data: MutableList<BaseModel>? = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<BaseModel> {

        return when (viewType) {
            ScannerViewType.TYPE_CARD -> ScannerCardViewHolder(
                ScannerCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            ScannerViewType.TYPE_TITLE -> ScannerTitleCounterViewHolder(
                ScannerTitleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> {
                UnknownViewHolder(
                    UnknownItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }

        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<BaseModel>, position: Int) {
        data?.let { data ->
            when(holder){
                is ScannerCardViewHolder -> holder.bind(data[position] as TagModel)
                is ScannerTitleCounterViewHolder -> holder.bind(data[position] as TitleCounterModel)
            }
        }

    }

    override fun getItemCount(): Int = data?.size ?: 0

    override fun getItemViewType(position: Int): Int {
        return when (data?.get(position)) {
            is TagModel -> ScannerViewType.TYPE_CARD
            is TitleCounterModel -> ScannerViewType.TYPE_TITLE
            else -> {
                ScannerViewType.TYPE_UNKNOWN
            }
        }
    }

    fun add(tag: BaseModel) {
        data?.let {
            if (!it.contains(tag)) {

                val position: Int = data?.size ?: 0
                it.add(tag)
                this.notifyItemInserted(position)

            }
        }
    }

    fun addAll(data: MutableList<BaseModel>) {
        this.data = data
        this.notifyDataSetChanged()
    }


    fun clear() {
        data?.clear()
        this.notifyDataSetChanged()
    }

}