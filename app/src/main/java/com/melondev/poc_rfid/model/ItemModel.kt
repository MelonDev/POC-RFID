package com.melondev.poc_rfid.model

import com.melondev.poc_rfid.callback.TagCallback

data class ItemModel (
    var name: String,
    var enable :Boolean = false
): BaseModel()