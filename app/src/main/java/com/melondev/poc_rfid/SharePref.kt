package com.melondev.poc_rfid

import android.content.Context
import android.content.SharedPreferences

open class SharePref(context: Context) {
    private val editor: SharedPreferences.Editor
    private val sharedPref: SharedPreferences

    init {
        sharedPref = context.getSharedPreferences("DATABASE".uppercase(), Context.MODE_PRIVATE)
        editor = sharedPref.edit()
    }

    fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key.uppercase(), value)
    }

    fun putInt(key: String, value: Int) {
        editor.putInt(key.uppercase(), value)
    }

    fun putString(key: String, value: String?) {
        editor.putString(key.uppercase(), value)
    }

    fun putDouble(key: String, value: Double) {
        editor.putLong(key.uppercase(), value.toLong())
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return sharedPref.getBoolean(key.uppercase(), default)
    }

    fun getInt(key: String, default: Int?): Int? {
        val value = sharedPref.getInt(key.uppercase(), -999999)
        return if (value != -999999) value else default
    }

    fun getString(key: String, default: String?): String? {
        return sharedPref.getString(key.uppercase(), default)
    }

    fun getDouble(key: String, default: Double): Double {
        return sharedPref.getLong(key.uppercase(), default.toLong()).toDouble()
    }

    fun commit() {
        editor.commit()
    }


}