package com.melondev.poc_rfid.callback

import com.melondev.poc_rfid.model.TagModel

interface TagCallback {
    fun onClick(tag: TagModel)
}