package com.melondev.poc_rfid.model

import com.honeywell.rfidservice.TriggerMode
import com.melondev.poc_rfid.callback.DeviceCallback

/*
status:
Disconnect = -1
Connected = 0
ReaderCreated = 1
 */

data class DeviceModel(
    val name: String,
    val address: String,
    var status: Int = -1,
    var callback: DeviceCallback?,
    var detail: DeviceDetail? = null
): BaseModel()

data class DeviceDetail(
    val mode: TriggerMode?
)