package com.melondev.poc_rfid.callback

import com.melondev.poc_rfid.model.ItemModel

interface ItemCallback {
    fun onClick(item: ItemModel)
}