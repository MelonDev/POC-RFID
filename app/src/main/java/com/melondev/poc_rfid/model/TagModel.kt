package com.melondev.poc_rfid.model

import com.melondev.poc_rfid.callback.TagCallback

data class TagModel(
    var name: String? = null,
    var address: String? = null,
    var rssi: Int? = null,
    var image: Int? = null,
    var action :TagActionModel? = null,
    var callback: TagCallback? = null
)

data class TagActionModel(
    var waterEnabled :Boolean = false,
    var fertilizerEnabled :Boolean = false
)