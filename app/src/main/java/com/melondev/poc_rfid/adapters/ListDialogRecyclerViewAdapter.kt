package com.melondev.poc_rfid.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.melondev.poc_rfid.R
import com.melondev.poc_rfid.callback.ItemCallback

import com.melondev.poc_rfid.placeholder.PlaceholderContent.PlaceholderItem
import com.melondev.poc_rfid.databinding.FragmentListDialogBinding
import com.melondev.poc_rfid.model.DeviceModel
import com.melondev.poc_rfid.model.ItemModel

class ListDialogRecyclerViewAdapter : RecyclerView.Adapter<ListDialogRecyclerViewAdapter.ViewHolder>() {

    var data: MutableList<ItemModel>? = ArrayList()
    var callback: ItemCallback? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentListDialogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        data?.let { data ->
            val item = data[position]

            val backgroundColor = if (item.enable) ContextCompat.getColor(
                holder.cardView.context,
                R.color.itemEnableColor
            ) else ContextCompat.getColor(
                holder.cardView.context,
                R.color.itemBackgroundColor
            )

            val textColor = if (item.enable) ContextCompat.getColor(
                holder.cardView.context,
                R.color.itemOnEnableColor
            ) else ContextCompat.getColor(
                holder.cardView.context,
                R.color.itemOnBackgroundColor
            )

            holder.apply {
                textView.text = item.name
                textView.setTextColor(textColor)

                cardView.setCardBackgroundColor(backgroundColor)
                cardView.setOnClickListener {
                    callback?.let {
                        it.onClick(item)
                    }
                }
            }
        }
        //holder.idView.text = item.id
        //holder.contentView.text = item.content
    }

    override fun getItemCount(): Int = data?.size ?: 0

    inner class ViewHolder(binding: FragmentListDialogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val textView: TextView = binding.listDialogItemText
        val cardView: CardView = binding.listDialogItemCard

    }

    fun addAll(data :MutableList<ItemModel>){
        this.data = data
        this.notifyDataSetChanged()
    }

}