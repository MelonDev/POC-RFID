package com.melondev.poc_rfid

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.honeywell.rfidservice.EventListener
import com.honeywell.rfidservice.RfidManager
import com.honeywell.rfidservice.TriggerMode
import com.honeywell.rfidservice.rfid.OnTagReadListener
import com.honeywell.rfidservice.rfid.RfidReader
import com.honeywell.rfidservice.rfid.TagAdditionData
import com.honeywell.rfidservice.rfid.TagReadOption
import com.melondev.poc_rfid.model.TagActionModel
import com.melondev.poc_rfid.model.TagModel
import java.text.DateFormat
import java.text.SimpleDateFormat

class TagsFragment : Fragment() {

    private lateinit var rfidManager: RfidManager
    private lateinit var rfidReader: RfidReader

    private var tags: MutableList<String> = ArrayList()


    private var mockData: MutableList<TagModel> = ArrayList()
    private lateinit var notAvailableView: TextView
    private lateinit var layoutView: LinearLayout

    private lateinit var nameView: TextView
    private lateinit var addressView: TextView
    private lateinit var rssiView: TextView
    private lateinit var imageView: ImageView

    private lateinit var actionLayout: LinearLayout

    private lateinit var waterOutsideView: CardView
    private lateinit var waterInsideView: CardView
    private lateinit var waterImageView: ImageView
    private lateinit var waterTextView: TextView

    private lateinit var fertilizerOutsideView: CardView
    private lateinit var fertilizerInsideView: CardView
    private lateinit var fertilizerImageView: ImageView
    private lateinit var fertilizerTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rfidManager = MyApplication.getInstance().rfidMgr
        if (rfidManager.isConnected) {
            rfidReader = MyApplication.getInstance().mRfidReader
        }

        mockData.addAll(
            arrayOf(
                TagModel(
                    name = "ต้นประดู่",
                    address = "E2806894000050106F36D612",
                    image = R.drawable.padauk,
                    action = TagActionModel()
                ),
                TagModel(
                    name = "ต้นมะขาม",
                    address = "E2005175881902411140A540",
                    image = R.drawable.tamarind,
                    action = TagActionModel()

                )
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
        imageView = view.findViewById<ImageView>(R.id.tag_image)

        actionLayout = view.findViewById<LinearLayout>(R.id.tag_action_layout)

        waterOutsideView = view.findViewById<CardView>(R.id.tag_action_water_circle_outside)
        waterInsideView = view.findViewById<CardView>(R.id.tag_action_water_circle_inside)
        waterImageView = view.findViewById<ImageView>(R.id.tag_action_water_circle_image)
        waterTextView = view.findViewById<TextView>(R.id.tag_action_water_circle_text)

        fertilizerOutsideView = view.findViewById<CardView>(R.id.tag_action_fertilizer_circle_outside)
        fertilizerInsideView = view.findViewById<CardView>(R.id.tag_action_fertilizer_circle_inside)
        fertilizerImageView = view.findViewById<ImageView>(R.id.tag_action_fertilizer_circle_image)
        fertilizerTextView = view.findViewById<TextView>(R.id.tag_action_fertilizer_circle_text)



        if (isReaderAvailable()) {
            notAvailableView.visibility = View.GONE

        } else {
            notAvailableView.visibility = View.VISIBLE

        }



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
            actionLayout.visibility = View.GONE

        }

        override fun onDeviceDisconnected(o: Any) {
            notAvailableView.visibility = View.VISIBLE
            layoutView.visibility = View.GONE
            actionLayout.visibility = View.GONE


        }

        override fun onReaderCreated(b: Boolean, rfidReader: RfidReader) {
            notAvailableView.visibility = View.GONE
            layoutView.visibility = View.GONE

        }

        override fun onRfidTriggered(trigger: Boolean) {

            if (!trigger) {
                beeping()
                stopRead()
            } else {
                read()
            }
        }

        override fun onTriggerModeSwitched(triggerMode: TriggerMode) {}
    }

    private fun beeping(){
        rfidManager.setBeeper(true, 10, 10)
        rfidManager.setBeeper(false, 10, 10)
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

    private fun showTag(tag: TagModel?) {
        tag?.let {tag->
            layoutView.visibility = View.VISIBLE

            nameView.text = tag.name ?: "ไม่ทราบชี่อ"
            addressView.text = "ไอดี: " + (tag.address ?: "ไม่ทราบ")
            rssiView.text = "ระยะห่าง: " + (tag.rssi ?: "ไม่ทราบ").toString()

            tag.image?.let { image ->
                imageView.visibility = View.VISIBLE
                imageView.setImageResource(image)


            } ?: run {
                imageView.visibility = View.GONE
            }

            tag.action?.let { action ->
                actionLayout.visibility = View.VISIBLE

                context?.let {context ->

                    waterAction(false)

                    waterOutsideView.setOnClickListener {
                        waterAction(true)
                    }

                    fertilizerAction(false)

                    fertilizerOutsideView.setOnClickListener {
                        fertilizerAction(true)
                    }


                }



            }?: run {
                actionLayout.visibility = View.GONE
            }

        } ?: run {
            layoutView.visibility = View.GONE
        }
    }

    private fun waterAction(isEnabled :Boolean){
        context?.let {context ->
            val enableColor = ContextCompat.getColor(
                context,
                R.color.enableColor
            )

            val disableColor = ContextCompat.getColor(
                context,
                R.color.disableColor
            )

            val waterColor = if (isEnabled) enableColor else disableColor
            val image = if (isEnabled) R.drawable.ic_baseline_check_24 else R.drawable.ic_baseline_water_24
            waterOutsideView.setCardBackgroundColor(waterColor)
            waterTextView.setTextColor(waterColor)
            waterImageView.setColorFilter(waterColor)

            waterImageView.setImageResource(image)


        }
    }

    private fun fertilizerAction(isEnabled :Boolean){
        context?.let {context ->
            val enableColor = ContextCompat.getColor(
                context,
                R.color.enableColor
            )

            val disableColor = ContextCompat.getColor(
                context,
                R.color.disableColor
            )

            val fertilizerColor = if (isEnabled) enableColor else disableColor
            val image = if (isEnabled) R.drawable.ic_baseline_check_24 else R.drawable.ic_baseline_fertilizer_2_24


            fertilizerOutsideView.setCardBackgroundColor(fertilizerColor)
            fertilizerTextView.setTextColor(fertilizerColor)
            fertilizerImageView.setColorFilter(fertilizerColor)

            fertilizerImageView.setImageResource(image)


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

                        if (filterData.isNotEmpty()) {
                            val item = filterData[0]
                            val tag = TagModel(name = item.name, address = epc, rssi = rssi,image=item.image,action=item.action)
                            data.add(tag)
                        } else {
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