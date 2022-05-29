package com.melondev.poc_rfid.adapters

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.melondev.poc_rfid.R
import com.melondev.poc_rfid.databinding.FragmentDevicesBinding
import com.melondev.poc_rfid.model.DeviceDetail
import com.melondev.poc_rfid.model.DeviceModel


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
            holder.apply {
                nameView.text = device.name
                addressView.text = device.address
                if (device.status > -1) {
                    deviceConnection.visibility = View.VISIBLE
                    Log.e("DETAILL", (device.detail != null).toString())
                    deviceDetail.visibility = if (device.detail != null) View.VISIBLE else View.GONE
                    device.detail?.let { detail ->
                        detail.mode?.let {
                            deviceMode.visibility = View.VISIBLE
                            deviceMode.text = "โหมด: " + it.name
                        } ?: run {
                            deviceMode.visibility = View.GONE
                        }
                    } ?: run {

                    }
                    when (device.status) {
                        0 -> {
                            deviceConnection.setCardBackgroundColor(
                                ContextCompat.getColor(
                                    holder.deviceConnection.context,
                                    R.color.deviceConnectingColor
                                )
                            )
                            deviceStatus.text = "กำลังเชี่อมต่อ"
                            deviceStatus.setTextColor(
                                ContextCompat.getColor(
                                    holder.deviceConnection.context,
                                    R.color.onDeviceConnectingColor
                                )
                            )
                        }
                        1 -> {

                            deviceConnection.setCardBackgroundColor(
                                ContextCompat.getColor(
                                    holder.deviceConnection.context,
                                    R.color.deviceConnectedColor
                                )
                            )
                            deviceStatus.text = "เชื่อมต่อแล้ว"
                            deviceStatus.setTextColor(
                                ContextCompat.getColor(
                                    holder.deviceConnection.context,
                                    R.color.white
                                )
                            )

                        }
                    }
                } else {
                    deviceDetail.visibility = View.GONE
                    deviceConnection.visibility = View.GONE
                }
                deviceLayout.apply {
                    this.setOnClickListener {
                        device.callback?.let {
                            if (device.status == -1) {
                                it.connect(device, position)
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

    fun addConnectedDevice(device: DeviceModel) {
        data?.let {
            device.status = 0
            it.add(0,device)
            this.notifyItemInserted(0)
        }
    }


    fun clear() {
        data?.clear()
        this.notifyDataSetChanged()
    }

    fun connect(position: Int) {
        data?.let { data ->
            val device: DeviceModel = data[position]
            device.status = 0
            data.removeAt(position)
            data.add(0, device)
            this.notifyItemMoved(position, 0)
            this.notifyItemChanged(position)
        }
    }

    fun connected(detail: DeviceDetail) {
        data?.let { data ->
            val device: DeviceModel = data[0]
            device.status = 0
            device.detail = detail
            data[0] = device
            this.notifyItemChanged(0)
        }
    }

    fun readerCreated() {
        data?.let { data ->
            val device: DeviceModel = data[0]
            device.status = 1
            data[0] = device
            this.notifyItemChanged(0)
        }
    }

    fun disconnected(position: Int) {
        data?.let { data ->
            val device: DeviceModel = data[position]
            device.status = -1
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
        val deviceStatus: TextView = binding.deviceStatus

        val deviceMode: TextView = binding.deviceMode
        val deviceDetail: LinearLayout = binding.deviceDetail


    }

}