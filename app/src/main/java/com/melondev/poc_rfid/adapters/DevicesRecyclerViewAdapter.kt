package com.melondev.poc_rfid.adapters

import android.content.ClipData.Item
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.melondev.poc_rfid.databinding.FragmentDevicesBinding
import com.melondev.poc_rfid.model.DeviceModel
import com.melondev.poc_rfid.placeholder.PlaceholderContent.PlaceholderItem

class DevicesRecyclerViewAdapter : RecyclerView.Adapter<DevicesRecyclerViewAdapter.ViewHolder>() {

    var data: MutableList<DeviceModel>? = ArrayList()

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
        data?.let { data ->
            val device: DeviceModel = data[position]
            holder.nameView.text = device.name
            holder.addressView.text = device.address
            if (device.connected) {
                holder.deviceConnection.visibility = View.VISIBLE
            } else {
                holder.deviceConnection.visibility = View.GONE
            }
            holder.deviceLayout.apply {
                this.setOnClickListener {
                    device.callback?.let {
                        if(!device.connected) {
                            it.connect(device.address, position)
                        }
                    }
                }
                this.setOnLongClickListener {
                    device.callback?.let {
                        it.disconnect(position)
                    }
                    return@setOnLongClickListener true
                }
            }
        }
    }

    override fun getItemCount(): Int = data?.size ?: 0

    fun add(device: DeviceModel) {
        data?.let {
            if (!it.contains(device)) {

                val position: Int = data?.size ?: 0
                it.add(device)
                this.notifyItemInserted(position)

            }
        }
    }


    fun clear() {
        data?.clear()
        this.notifyDataSetChanged()
    }

    fun connected(position: Int) {
        data?.let { data ->
            val device: DeviceModel = data[position]
            device.connected = true
            data[position] = device
            this.notifyItemChanged(position)
        }
    }

    fun disconnected(position: Int) {
        data?.let { data ->
            val device: DeviceModel = data[position]
            device.connected = false
            data[position] = device
            this.notifyItemChanged(position)

        }
    }

    private fun addData(newData: ArrayList<DeviceModel>?) {
        newData?.let { new ->
            data?.let {
                data?.addAll(new)
            } ?: run {
                data = newData
            }
            this.notifyDataSetChanged()
        }
    }

    inner class ViewHolder(binding: FragmentDevicesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val nameView: TextView = binding.deviceName
        val addressView: TextView = binding.deviceAddress
        val deviceLayout: RelativeLayout = binding.deviceLayout
        val deviceConnection: CardView = binding.deviceConnection


    }

}