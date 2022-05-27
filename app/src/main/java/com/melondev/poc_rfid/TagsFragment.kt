package com.melondev.poc_rfid

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.honeywell.rfidservice.EventListener
import com.honeywell.rfidservice.RfidManager
import com.honeywell.rfidservice.TriggerMode
import com.honeywell.rfidservice.rfid.OnTagReadListener
import com.honeywell.rfidservice.rfid.RfidReader
import com.honeywell.rfidservice.rfid.TagAdditionData
import com.honeywell.rfidservice.rfid.TagReadOption
import com.melondev.poc_rfid.model.TagModel
import java.text.DateFormat
import java.text.SimpleDateFormat

class TagsFragment : Fragment() {

    private lateinit var rfidManager: RfidManager
    private lateinit var rfidReader: RfidReader

    private var tags: MutableList<String> = ArrayList()


    private var mockData: MutableList<TagModel> = ArrayList()
    private lateinit var notAvailableView : TextView
    private lateinit var layoutView : LinearLayout

    private lateinit var nameView : TextView
    private lateinit var addressView : TextView
    private lateinit var rssiView : TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rfidManager = MyApplication.getInstance().rfidMgr
        if(rfidManager.isConnected){
            rfidReader = MyApplication.getInstance().mRfidReader
        }

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
        val view = inflater.inflate(R.layout.fragment_tags, container, false)

        notAvailableView = view.findViewById<TextView>(R.id.tag_not_available)
        nameView = view.findViewById<TextView>(R.id.tag_name)
        addressView = view.findViewById<TextView>(R.id.tag_address)
        rssiView = view.findViewById<TextView>(R.id.tag_rssi)
        layoutView = view.findViewById<LinearLayout>(R.id.tag_layout)



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
            layoutView.visibility = View.GONE
        }
        override fun onDeviceDisconnected(o: Any) {
            notAvailableView.visibility = View.VISIBLE
            layoutView.visibility = View.GONE

        }
        override fun onReaderCreated(b: Boolean, rfidReader: RfidReader) {
            notAvailableView.visibility = View.GONE
            layoutView.visibility = View.GONE

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

    private fun showTag(tag :TagModel?){
        tag?.let {
            layoutView.visibility = View.VISIBLE

            nameView.text = tag.name ?: "ไม่ทราบชี่อ"
            addressView.text = "ไอดี: " + (tag.address ?: "ไม่ทราบ")
            rssiView.text = "ระยะห่าง: " + (tag.rssi ?: "ไม่ทราบ").toString()

        }?: run {
            layoutView.visibility = View.GONE
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

                    showTag(data.first())
                })
            }

        }

}