package com.melondev.poc_rfid

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

import com.melondev.poc_rfid.placeholder.PlaceholderContent.PlaceholderItem
import com.melondev.poc_rfid.databinding.FragmentDevicesBinding

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class DevicesRecyclerViewAdapter(
    private val values: List<PlaceholderItem>
) : RecyclerView.Adapter<DevicesRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentDevicesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.id
        holder.contentView.text = item.content
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentDevicesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.tvName
        val contentView: TextView = binding.tvAddr

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}