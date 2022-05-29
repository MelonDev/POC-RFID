package com.melondev.poc_rfid.callback

import com.melondev.poc_rfid.model.DeviceModel

interface DeviceCallback {
    fun connect(device: DeviceModel, position :Int)
    fun disconnect(position :Int)
}