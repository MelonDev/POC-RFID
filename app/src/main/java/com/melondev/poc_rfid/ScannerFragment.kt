package com.melondev.poc_rfid

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.honeywell.rfidservice.EventListener
import com.honeywell.rfidservice.RfidManager
import com.honeywell.rfidservice.TriggerMode
import com.honeywell.rfidservice.rfid.*
import com.melondev.poc_rfid.adapters.DevicesRecyclerViewAdapter
import com.melondev.poc_rfid.adapters.ScannerRecyclerViewAdapter
import com.melondev.poc_rfid.model.TagModel
import com.melondev.poc_rfid.placeholder.PlaceholderContent
import java.text.DateFormat
import java.text.SimpleDateFormat

class ScannerFragment : Fragment() {

    private lateinit var rfidManager: RfidManager
    private lateinit var rfidReader: RfidReader
    private var tags: MutableList<String> = ArrayList()

    private var mockData: MutableList<TagModel> = ArrayList()


    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: ScannerRecyclerViewAdapter
    private lateinit var notAvailableView : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rfidManager = MyApplication.getInstance().rfidMgr
        if (rfidManager.isConnected) {
            rfidReader = MyApplication.getInstance().mRfidReader
        }

        adapter = ScannerRecyclerViewAdapter()

        mockData.addAll(
            arrayOf(
                TagModel(
                    name = "ต้นประดู่",
                    address = "E2806894000050106F36D612"
                ),
                TagModel(name = "ต้นมะขาม", address = "E2005175881902411140A540")
            )
        )


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scanner_list, container, false)


        recyclerview = view.findViewById<RecyclerView>(R.id.scanner_recycler_view)
        notAvailableView = view.findViewById<TextView>(R.id.scanner_not_available)

        if (isReaderAvailable()) {
            notAvailableView.visibility = View.GONE

        }else{
            notAvailableView.visibility = View.VISIBLE

        }

        recyclerview.layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = this.adapter



        return view
    }

    override fun onResume() {
        super.onResume()
        rfidManager.addEventListener(eventListener)
    }

    override fun onPause() {
        super.onPause()
        rfidManager.removeEventListener(eventListener)
        stopRead()
    }

    private val eventListener: EventListener = object : EventListener {
        override fun onDeviceConnected(o: Any) {
            notAvailableView.visibility = View.VISIBLE

        }
        override fun onDeviceDisconnected(o: Any) {
            notAvailableView.visibility = View.VISIBLE

        }
        override fun onReaderCreated(b: Boolean, rfidReader: RfidReader) {
            notAvailableView.visibility = View.GONE
        }
        override fun onRfidTriggered(trigger: Boolean) {
            if (!trigger) {
                stopRead()
            } else {
                read()
            }
        }

        override fun onTriggerModeSwitched(triggerMode: TriggerMode) {}
    }

    private fun isReaderAvailable(): Boolean {

        if (rfidManager.isConnected) {
            return rfidReader.available()
        }

        return false
    }

    private fun read() {
        if (isReaderAvailable()) {
            adapter.clear()

            tags.clear()
            rfidReader.setOnTagReadListener(dataListener)
            val option = TagReadOption()
            option.isRssi = true
            option.isAntennaId = true
            option.isData = true
            option.isReadCount = true
            option.isFrequency = true
            option.isProtocol = true
            option.isTimestamp = true
            rfidReader.read(TagAdditionData.get("None"), option)
        }
    }

    private fun stopRead() {
        if (isReaderAvailable()) {
            rfidReader.stopRead()
            rfidReader.removeOnTagReadListener(dataListener)
        }
    }

    private val dataListener =
        OnTagReadListener { t ->

            activity?.let {
                it.runOnUiThread(Runnable {
                    val data: MutableList<TagModel> = ArrayList()
                    for (trd in t) {
                        val epc = trd.epcHexStr
                        val rssi = trd.rssi
                        Log.e("EPC", epc)
                        Log.e("RSSI", Integer.toString(rssi))

                        val filterData = mockData.filter {
                            it.address?.let {
                                it.contains(epc)
                            } ?: run {
                                false
                            }
                        }

                        if(filterData.isNotEmpty()){
                            val item = filterData[0]
                            val tag = TagModel(name = item.name,address = epc, rssi = rssi)
                            data.add(tag)
                        }else {
                            val tag = TagModel(address = epc, rssi = rssi)
                            data.add(tag)
                        }


                    }
                    data.sortByDescending { it.rssi }
                    adapter.addAll(data)
                })
            }


            synchronized(t) {
                for (trd in t) {

                    val epc = trd.epcHexStr
                    val antenna = trd.antenna
                    val count = trd.readCount
                    val rssi = trd.rssi
                    val frequency = trd.frequency
                    val date = trd.time
                    //Log.e("EPC", epc)
                    //Log.e("ANTENNA", Integer.toString(antenna))
                    //Log.e("COUNT", Integer.toString(count))
                    //Log.e("RSSI", Integer.toString(rssi))
                    //Log.e("FREQUENCY", Integer.toString(frequency))
                    val pattern = "MM/dd/yyyy HH:mm:ss.SSS"
                    val df: DateFormat = SimpleDateFormat(pattern)
                    val todayAsString = df.format(date)
                    Log.e("DATE", todayAsString)
                    Log.e("", "")
                    //adapter.add(trd)
                    val tag = TagModel(name = epc, rssi = rssi)
                    //data.add(tag)

                }
                //adapter.addAll(data)
                //adapter.notifyDataSetChanged()

                //mHandler.sendEmptyMessage(0)
            }
        }
    /*
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                0 -> mAdapter.notifyDataSetChanged()
            }
        }
    }

     */


}