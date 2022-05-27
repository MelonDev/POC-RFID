package com.melondev.poc_rfid

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.honeywell.rfidservice.RfidManager
import com.melondev.poc_rfid.placeholder.PlaceholderContent


class DevicesFragment : Fragment() {

    private var columnCount = 1

    private val mRequestPermissions: MutableList<String> = ArrayList()


    private lateinit var myApplication: MyApplication

    private lateinit var rfidManager: RfidManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothManager: BluetoothManager

    private val PERMISSION_CODE = 1

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.e("Bluetooth", ":request permission result ok")
        } else {
            Log.e("Bluetooth", ":request permission result canceled / denied")
        }
    }

    private fun requestBluetoothPermission() {
        Log.e("Permission","REQUEST")
        val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activityResultLauncher.launch(enableBluetoothIntent)
    }

    var discoveredDevices: Set<BluetoothDevice> = emptySet()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            Log.e("Bluetooth", "onReceive")
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {

                        //Log.e("NAME",device.name)

                        Log.e("ADDS",device.address)


                        val updated = discoveredDevices.plus(device)
                        discoveredDevices = updated
                    }
                    Log.i("Bluetooth", "onReceive: Device found")
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.i("Bluetooth", "onReceive: Started Discovery")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i("Bluetooth", "onReceive: Finished Discovery")
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun scan(): Set<BluetoothDevice> {
        context?.let {
            if (ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (bluetoothAdapter.isDiscovering) {
                    Log.e("Bluetooth","RESTART")

                    bluetoothAdapter.cancelDiscovery()
                    bluetoothAdapter.startDiscovery()
                } else {
                    Log.e("Bluetooth","START")
                    bluetoothAdapter.startDiscovery()
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    Log.e("Bluetooth","STOP")
                    bluetoothAdapter.cancelDiscovery()
                }, 10000L)

            }
        }

        return discoveredDevices
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MyApplication.getInstance()?.let { app ->
            myApplication = app
            app.rfidMgr?.let { manager ->
                rfidManager = manager
            }
        }

        val foundFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val startFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        val endFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        context?.let {
            bluetoothManager = it.getSystemService(BluetoothManager::class.java)
            bluetoothAdapter = bluetoothManager.adapter

            it.registerReceiver(receiver, foundFilter)
            it.registerReceiver(receiver, startFilter)
            it.registerReceiver(receiver, endFilter)
        }

        if (!bluetoothAdapter.isEnabled) {
            requestBluetoothPermission()
        }



        context?.let {
            if (ContextCompat.checkSelfPermission(
                    it, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                getActivity()?.let { activity ->

                    requestPermissions(
                        activity, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        PERMISSION_CODE
                    )
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_devices_list, container, false)
        val recyclerview = view.findViewById<RecyclerView>(R.id.devices_recycler_view)
        val scan_button = view.findViewById<FloatingActionButton>(R.id.devices_scan_button)


        scan_button.setOnClickListener {
            scan()
        }


        with(recyclerview) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = DevicesRecyclerViewAdapter(PlaceholderContent.ITEMS)
        }

        return view
    }


    override fun onDestroy() {
        super.onDestroy()
        context?.let {
            if (ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (bluetoothAdapter.isDiscovering)
                    bluetoothAdapter.cancelDiscovery()
                return
            }

            it.unregisterReceiver(receiver)
        }

    }


}