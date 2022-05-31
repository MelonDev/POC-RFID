package com.melondev.poc_rfid


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log

import android.view.Menu
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.honeywell.rfidservice.EventListener
import com.honeywell.rfidservice.RfidManager
import com.honeywell.rfidservice.TriggerMode
import com.honeywell.rfidservice.rfid.RfidReader
import com.melondev.poc_rfid.databinding.ActivityMainBinding
import com.melondev.poc_rfid.model.DeviceDetail

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var rfidManager: RfidManager
    private lateinit var sharedPref: SharePref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        sharedPref = SharePref(this)

        preparingTreePref()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        bottomNavigationView.setupWithNavController(navController)
    }

    override fun onDestroy() {
        rfidManager.disconnect()
        super.onDestroy()
    }

    private fun preparingTreePref() {
        sharedPref.putString("E2806894000050106F36D612@name", "ต้นประดู่")
        sharedPref.putInt("E2806894000050106F36D612@image", R.drawable.padauk)
        sharedPref.putBoolean("E2806894000050106F36D612@water", false)
        sharedPref.putBoolean("E2806894000050106F36D612@fertilizer", false)
        sharedPref.putString("E2806894000050106F36D612@location", "แปลง A")
        sharedPref.putString("E2806894000050106F36D612@status", "ขาดน้ำ")
        sharedPref.putStringArray("E2806894000050106F36D612@lots", listOf("A01@20","A02@15","A03@6"))

        sharedPref.putString("E2005175881902411140A540@name", "ต้นมะขาม")
        sharedPref.putInt("E2005175881902411140A540@image", R.drawable.tamarind)
        sharedPref.putBoolean("E2005175881902411140A540@water", false)
        sharedPref.putBoolean("E2005175881902411140A540@fertilizer", false)
        sharedPref.putString("E2005175881902411140A540@location", "แปลง C")
        sharedPref.putString("E2005175881902411140A540@status", "ขาดน้ำ")
        sharedPref.putStringArray("E2005175881902411140A540@lots", listOf("A01@50","B01@10","B02@18"))

        sharedPref.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}