package com.melondev.poc_rfid.model

import com.melondev.poc_rfid.callback.DeviceCallback

data class DeviceModel(val name: String, val address: String, var connected: Boolean = false,var callback : DeviceCallback?)