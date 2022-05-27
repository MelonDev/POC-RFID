package com.melondev.poc_rfid.callback

interface DeviceCallback {
    fun connect(macAddress: String,position :Int)
    fun disconnect(position :Int)
}