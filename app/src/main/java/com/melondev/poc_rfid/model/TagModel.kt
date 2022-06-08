package com.melondev.poc_rfid.model

import com.melondev.poc_rfid.callback.TagCallback

data class TagModel(
    var name: String? = null,
    var address: String? = null,
    var rssi: Int? = null,
    var image: Int? = null,
    var environment :TagEnvironment? = null,
    var lot: String? = null,
    var callback: TagCallback? = null
) : BaseModel()

data class TagEnvironment(
    var water :Boolean = false,
    var fertilizer :Boolean = false,
    var location: String? = null,
    var status: String? = null,

    )