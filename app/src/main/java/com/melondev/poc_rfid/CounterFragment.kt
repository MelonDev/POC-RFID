package com.melondev.poc_rfid

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.honeywell.rfidservice.RfidManager
import com.honeywell.rfidservice.rfid.OnTagReadListener
import com.honeywell.rfidservice.rfid.RfidReader
import com.honeywell.rfidservice.rfid.TagAdditionData
import com.honeywell.rfidservice.rfid.TagReadOption
import com.melondev.poc_rfid.adapters.ScannerRecyclerViewAdapter
import com.melondev.poc_rfid.callback.TagCallback
import com.melondev.poc_rfid.model.*
import java.text.DateFormat
import java.text.SimpleDateFormat

class CounterFragment : Fragment() {
    private lateinit var rfidManager: RfidManager
    private lateinit var rfidReader: RfidReader
    private var tags: MutableList<String> = ArrayList()

    private var dialog: BottomSheetDialog? = null

    private lateinit var sharedPref: SharePref

    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: ScannerRecyclerViewAdapter
    private lateinit var notAvailableView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rfidManager = MyApplication.getInstance().rfidMgr
        if (rfidManager.isConnected) {
            rfidReader = MyApplication.getInstance().mRfidReader
        }

        adapter = ScannerRecyclerViewAdapter()
        context?.let {
            sharedPref = SharePref(it)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_counter, container, false)

        recyclerview = view.findViewById<RecyclerView>(R.id.counter_recycler_view)
        notAvailableView = view.findViewById<TextView>(R.id.counter_not_available)


        if (isReaderAvailable()) {
            notAvailableView.visibility = View.GONE

        } else {
            notAvailableView.visibility = View.VISIBLE

        }

        recyclerview.layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = this.adapter

        return view
    }

    private fun beeping() {
        rfidManager.setBeeper(true, 10, 10)
        rfidManager.setBeeper(false, 10, 10)
    }

    private fun isReaderAvailable(): Boolean {

        if (rfidManager.isConnected) {
            if (rfidManager.readerAvailable()) {
                return rfidReader.available()
            }
            return false
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

    private val tagCallback: TagCallback = object : TagCallback {
        override fun onClick(tag: TagModel) {
            context?.let { context ->
                val dialog = BottomSheetDialog(context)
                val view = layoutInflater.inflate(R.layout.tag_sheet, null)

                view.apply {
                    val nameView = findViewById<TextView>(R.id.tag_sheet_name)
                    val addressView = findViewById<TextView>(R.id.tag_sheet_address)
                    val rssiView = findViewById<TextView>(R.id.tag_sheet_rssi)
                    val imageView = findViewById<ImageView>(R.id.tag_sheet_image)


                    nameView.text = tag.name ?: "ไม่ทราบชี่อ"
                    addressView.text = "ไอดี: ${(tag.address ?: "ไม่ทราบ")}"
                    rssiView.text = "ความแรงสัญญาณ: ${(tag.rssi ?: "ไม่ทราบ")}"

                    tag.image?.let { image ->
                        imageView.visibility = View.VISIBLE
                        imageView.setImageResource(image)
                    } ?: run {
                        imageView.visibility = View.GONE
                    }
                }

                dialog.setCancelable(true)
                dialog.setContentView(view)
                dialog.show()
            }
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
                        val rawName: String? = sharedPref.getString("$epc@name", null)

                        rawName?.let { name ->
                            val image: Int? = sharedPref.getInt("$epc@image", null)
                            val water: Boolean = sharedPref.getBoolean("$epc@water", false)
                            val fertilizer: Boolean =
                                sharedPref.getBoolean("$epc@fertilizer", false)
                            val location: String? = sharedPref.getString("$epc@location", null)
                            val status: String? = sharedPref.getString("$epc@status", null)
                            val lot: String? = sharedPref.getString("$epc@lot", null)

                        } ?: run {
                            val tag = TagModel(address = epc, rssi = rssi)
                            data.add(tag)
                        }

                    }

                })
            }

        }

}