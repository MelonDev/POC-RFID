package com.melondev.poc_rfid

import android.Manifest
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.honeywell.rfidservice.EventListener
import com.honeywell.rfidservice.RfidManager
import com.honeywell.rfidservice.TriggerMode
import com.honeywell.rfidservice.rfid.RfidReader
import com.melondev.poc_rfid.adapters.DevicesRecyclerViewAdapter
import com.melondev.poc_rfid.callback.DeviceCallback
import com.melondev.poc_rfid.model.DeviceModel


class DevicesFragment : Fragment() {

    private val PERMISSION_REQUEST_CODE = 1

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private val requestPermissions: MutableList<String> = ArrayList()

    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: DevicesRecyclerViewAdapter


    private lateinit var myApplication: MyApplication

    private lateinit var rfidManager: RfidManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothLeScanner: BluetoothLeScanner

    private var scanning = false

    private var alertDialog: AlertDialog? = null


    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            scan()
        }
    }

    @RequiresApi(VERSION_CODES.M)
    private fun requestPermissions(): Boolean {
        context?.let { context ->
            for (i in permissions.indices) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        permissions.get(i)
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions.add(permissions.get(i))
                }
            }
            if (requestPermissions.size > 0) {
                activity?.let { activity ->
                    ActivityCompat.requestPermissions(
                        activity,
                        permissions,
                        PERMISSION_REQUEST_CODE
                    )
                }
                return false

            }
            return true

        }
        return false
    }


    @RequiresApi(VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //configManager()
        rfidManager = MyApplication.getInstance().rfidMgr


        context?.let {
            bluetoothManager = it.getSystemService(BluetoothManager::class.java)
            bluetoothAdapter = bluetoothManager.adapter
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

            adapter = DevicesRecyclerViewAdapter()


            val dialogBuilder = AlertDialog.Builder(it)
            alertDialog = dialogBuilder.create()

        }

        scan()


    }

    private fun configManager() {
        MyApplication.getInstance()?.let { app ->
            myApplication = app
            app.rfidMgr?.let { manager ->
                rfidManager = manager
                rfidManager.setBeeper(true, 10, 10)
                rfidManager.setLEDBlink(true)
            }
        }
    }

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activityResultLauncher.launch(enableBtIntent)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_devices_list, container, false)
        recyclerview = view.findViewById<RecyclerView>(R.id.devices_recycler_view)
        val scan_button = view.findViewById<FloatingActionButton>(R.id.devices_scan_button)


        scan_button.setOnClickListener {
            if (!bluetoothAdapter.isEnabled) {
                enableBluetooth()
            } else {
                scan()
            }
        }

        recyclerview.layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = this.adapter

        return view
    }

    @RequiresApi(VERSION_CODES.M)
    private fun scan() {
        context?.let { context ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions()
            }

            if (!scanning) { // Stops scanning after a pre-defined scan period.
                Log.e("SCAN", "!scanning")

                Handler(Looper.getMainLooper()).postDelayed({
                    Log.e("Bluetooth LE", "STOP")
                    scanning = false
                    bluetoothLeScanner.stopScan(leScanCallback)
                    dismissDialog()

                }, 5000L)

                adapter.clear()
                showDialog("กำลังค้นหาอุปกรณ์...")
                scanning = true
                bluetoothLeScanner.startScan(leScanCallback)
            } else {
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }
        }

    }

    private val deviceCallback: DeviceCallback = object : DeviceCallback {
        override fun connect(macAddress: String, position: Int) {
            rfidManager.addEventListener(eventListener)
            if (bluetoothAdapter.isEnabled) {

                showDialog("กำลังเชื่อมต่อ...")
                rfidManager.connect(macAddress)
                adapter.connected(position)

            } else {
                enableBluetooth()
            }
        }

        override fun disconnect(position: Int) {
            if (bluetoothAdapter.isEnabled) {
                if (rfidManager.isConnected) {
                    showDialog("กำลังตัดการเชื่อมต่อ...")
                    rfidManager.disconnect()
                    adapter.disconnected(position)
                }
            } else {
                enableBluetooth()
            }
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @RequiresApi(VERSION_CODES.R)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            context?.let { context ->
                result.device?.let { device ->

                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissions()
                    }
                    if (device.name != null && device.address != null) {
                        val deviceModel = DeviceModel(
                            name = device.name,
                            address = device.address,
                            callback = deviceCallback
                        )
                        adapter.add(deviceModel)
                    }
                }
            }

        }
    }

    private val eventListener: EventListener = object : EventListener {
        override fun onDeviceConnected(o: Any) {
            activity?.let {
                it.runOnUiThread(Runnable {
                    rfidManager.createReader()
                    //scan()
                })
            }

        }

        override fun onDeviceDisconnected(o: Any) {
            activity?.let {
                it.runOnUiThread(Runnable {
                    dismissDialog()
                })
            }
        }

        override fun onReaderCreated(b: Boolean, rfidReader: RfidReader) {
            MyApplication.getInstance().mRfidReader = rfidReader
            dismissDialog()
        }

        override fun onRfidTriggered(b: Boolean) {}
        override fun onTriggerModeSwitched(triggerMode: TriggerMode) {}


    }

    fun showDialog(message: String) {
        alertDialog?.setTitle("แจ้งเตือน")
        alertDialog?.setMessage(message)
        alertDialog?.show()
    }

    fun dismissDialog() {
        alertDialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()

    }


}