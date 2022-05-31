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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.honeywell.rfidservice.EventListener
import com.honeywell.rfidservice.RfidManager
import com.honeywell.rfidservice.TriggerMode
import com.honeywell.rfidservice.rfid.OnTagReadListener
import com.honeywell.rfidservice.rfid.RfidReader
import com.honeywell.rfidservice.rfid.TagAdditionData
import com.honeywell.rfidservice.rfid.TagReadOption
import com.melondev.poc_rfid.adapters.ListDialogRecyclerViewAdapter
import com.melondev.poc_rfid.callback.ItemCallback
import com.melondev.poc_rfid.model.ItemModel
import com.melondev.poc_rfid.model.TagEnvironment
import com.melondev.poc_rfid.model.TagModel
import java.text.DateFormat
import java.text.SimpleDateFormat

class TagsFragment : Fragment() {

    private lateinit var rfidManager: RfidManager
    private lateinit var rfidReader: RfidReader

    private var tags: MutableList<String> = ArrayList()


    private lateinit var notAvailableView: TextView
    private lateinit var layoutView: LinearLayout

    private lateinit var nameView: TextView
    private lateinit var addressView: TextView
    private lateinit var rssiView: TextView
    private lateinit var imageView: ImageView

    private lateinit var areaView: CardView
    private lateinit var areaTextView: TextView
    private lateinit var areaButtonView: LinearLayout

    private lateinit var statusTextView: TextView
    private lateinit var statusButtonView: LinearLayout

    private lateinit var actionLayout: LinearLayout

    private lateinit var waterOutsideView: CardView
    private lateinit var waterInsideView: CardView
    private lateinit var waterImageView: ImageView
    private lateinit var waterTextView: TextView
    private lateinit var waterButtonView: LinearLayout


    private lateinit var fertilizerOutsideView: CardView
    private lateinit var fertilizerInsideView: CardView
    private lateinit var fertilizerImageView: ImageView
    private lateinit var fertilizerTextView: TextView
    private lateinit var fertilizerButtonView: LinearLayout

    private lateinit var sharedPref: SharePref

    private var dialog :BottomSheetDialog? = null

    private var selectedTag :TagModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rfidManager = MyApplication.getInstance().rfidMgr
        if (rfidManager.isConnected) {
            if (rfidManager.readerAvailable()) {
                rfidReader = MyApplication.getInstance().mRfidReader
            }
        }

        context?.let {
            sharedPref = SharePref(it)
        }

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

        areaView = view.findViewById<CardView>(R.id.tag_area)
        areaTextView = view.findViewById<TextView>(R.id.tag_area_text)
        areaButtonView = view.findViewById<LinearLayout>(R.id.tag_area_button)


        actionLayout = view.findViewById<LinearLayout>(R.id.tag_action_layout)

        statusTextView = view.findViewById<TextView>(R.id.tag_action_status_text)
        statusButtonView = view.findViewById<LinearLayout>(R.id.tag_action_status_button)

        waterOutsideView = view.findViewById<CardView>(R.id.tag_action_water_circle_outside)
        waterInsideView = view.findViewById<CardView>(R.id.tag_action_water_circle_inside)
        waterImageView = view.findViewById<ImageView>(R.id.tag_action_water_circle_image)
        waterTextView = view.findViewById<TextView>(R.id.tag_action_water_circle_text)
        waterButtonView = view.findViewById<LinearLayout>(R.id.tag_action_water_circle_button)


        fertilizerOutsideView =
            view.findViewById<CardView>(R.id.tag_action_fertilizer_circle_outside)
        fertilizerInsideView = view.findViewById<CardView>(R.id.tag_action_fertilizer_circle_inside)
        fertilizerImageView = view.findViewById<ImageView>(R.id.tag_action_fertilizer_circle_image)
        fertilizerTextView = view.findViewById<TextView>(R.id.tag_action_fertilizer_circle_text)
        fertilizerButtonView =
            view.findViewById<LinearLayout>(R.id.tag_action_fertilizer_circle_button)




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

    private val locationItemCallback: ItemCallback = object : ItemCallback {
        override fun onClick(item: ItemModel) {
            selectedTag?.let {
                sharedPref.putString("${it.address}@location", item.name)
                sharedPref.commit()

                areaTextView.text = item.name ?: "ไม่ทราบ"
                it.environment?.location = item.name

                dialog?.dismiss()
            }

        }
    }

    private val statusItemCallback: ItemCallback = object : ItemCallback {
        override fun onClick(item: ItemModel) {
            selectedTag?.let {
                sharedPref.putString("${it.address}@status", item.name)
                sharedPref.commit()

                statusTextView.text = item.name ?: "ไม่ทราบ"
                it.environment?.status = item.name

                dialog?.dismiss()
            }
        }
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
        tag?.let { tag ->
            layoutView.visibility = View.VISIBLE

            selectedTag = tag

            nameView.text = tag.name ?: "ไม่ทราบชี่อ"
            addressView.text = "ไอดี: ${(tag.address ?: "ไม่ทราบ")}"
            rssiView.text = "ความแรงสัญญาณ: ${(tag.rssi ?: "ไม่ทราบ")}"

            tag.image?.let { image ->
                imageView.visibility = View.VISIBLE
                imageView.setImageResource(image)


            } ?: run {
                imageView.visibility = View.GONE
            }

            selectedTag?.environment?.let { environment ->
                areaView.visibility = View.VISIBLE
                actionLayout.visibility = View.VISIBLE

                areaTextView.text = environment.location ?: "ไม่ทราบ"
                statusTextView.text = environment.status ?: "ไม่ทราบ"


                waterAction(environment.water)
                fertilizerAction(environment.fertilizer)


                context?.let { context ->
                    waterButtonView.setOnClickListener {
                        environment.water = !environment.water
                        sharedPref.putBoolean("${tag.address}@water", environment.water)
                        sharedPref.commit()
                        waterAction(environment.water)
                    }

                    fertilizerButtonView.setOnClickListener {
                        environment.fertilizer = !environment.fertilizer

                        sharedPref.putBoolean("${tag.address}@fertilizer", environment.fertilizer)
                        sharedPref.commit()
                        fertilizerAction(environment.fertilizer)
                    }

                    areaButtonView.setOnClickListener {

                        var choices: MutableList<ItemModel> = ArrayList()
                        choices.apply {
                            add(
                                ItemModel(
                                    name = "แปลง A",
                                    enable = environment.location.checkItem("แปลง A")
                                )
                            )
                            add(
                                ItemModel(
                                    name = "แปลง B",
                                    enable = environment.location.checkItem("แปลง B")
                                )
                            )
                            add(
                                ItemModel(
                                    name = "แปลง C",
                                    enable = environment.location.checkItem("แปลง C")
                                )
                            )
                            add(
                                ItemModel(
                                    name = "แปลง D",
                                    enable = environment.location.checkItem("แปลง D")
                                )
                            )
                        }

                        showDialog(
                            title = "กรุณาเลือกพื้นที่",
                            choices = choices,
                            callback = locationItemCallback
                        )
                    }

                    statusButtonView.setOnClickListener {

                        var choices: MutableList<ItemModel> = ArrayList()
                        choices.apply {
                            add(
                                ItemModel(
                                    name = "ปกติ",
                                    enable = environment.status.checkItem("ปกติ")
                                )
                            )
                            add(
                                ItemModel(
                                    name = "ขาดน้ำ",
                                    enable = environment.status.checkItem("ขาดน้ำ")
                                )
                            )
                            add(
                                ItemModel(
                                    name = "เฉา",
                                    enable = environment.status.checkItem("เฉา")
                                )
                            )
                            add(
                                ItemModel(
                                    name = "ตาย",
                                    enable = environment.status.checkItem("ตาย")
                                )
                            )
                        }

                        showDialog(
                            title = "กรุณาเลือกสถานะ",
                            choices = choices,
                            callback = statusItemCallback
                        )
                    }


                }


            } ?: run {
                areaView.visibility = View.GONE

                actionLayout.visibility = View.GONE
            }

        } ?: run {
            layoutView.visibility = View.GONE
        }
    }

    private fun String?.checkItem(y: String?): Boolean {
        if (this != null && y != null) {
            return this.contains(y)
        }
        return false
    }

    private fun showDialog(
        title: String? = null,
        choices: MutableList<ItemModel>?,
        callback: ItemCallback? = null
    ) {
        context?.let { context ->
            dialog = BottomSheetDialog(context)
            dialog?.apply {
                val view = layoutInflater.inflate(R.layout.fragment_list_dialog_list, null)

                view.apply {
                    val titleView = findViewById<TextView>(R.id.list_dialog_name)
                    titleView.text = title ?: "ตัวเลือก"

                    val recyclerView = findViewById<RecyclerView>(R.id.list_dialog_recyclerview)

                    recyclerView.layoutManager = LinearLayoutManager(context)
                    val adapter = ListDialogRecyclerViewAdapter()
                    adapter.callback = callback
                    recyclerView.adapter = adapter
                    choices?.let {
                        adapter.addAll(it)
                    }

                }

                setCancelable(true)
                setContentView(view)
                show()
            }

        }
    }

    private fun waterAction(isEnabled: Boolean) {
        context?.let { context ->
            val enableColor = ContextCompat.getColor(
                context,
                R.color.enableColor
            )

            val disableColor = ContextCompat.getColor(
                context,
                R.color.disableColor
            )

            val waterColor = if (isEnabled) enableColor else disableColor
            val image =
                if (isEnabled) R.drawable.ic_baseline_check_24 else R.drawable.ic_baseline_water_24
            waterOutsideView.setCardBackgroundColor(waterColor)
            waterTextView.setTextColor(waterColor)
            waterImageView.setColorFilter(waterColor)

            waterImageView.setImageResource(image)


        }
    }

    private fun fertilizerAction(isEnabled: Boolean) {
        context?.let { context ->
            val enableColor = ContextCompat.getColor(
                context,
                R.color.enableColor
            )

            val disableColor = ContextCompat.getColor(
                context,
                R.color.disableColor
            )

            val fertilizerColor = if (isEnabled) enableColor else disableColor
            val image =
                if (isEnabled) R.drawable.ic_baseline_check_24 else R.drawable.ic_baseline_fertilizer_2_24


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

                        val rawName: String? = sharedPref.getString("$epc@name", null)

                        rawName?.let { name ->
                            val image: Int? = sharedPref.getInt("$epc@image", null)
                            val water: Boolean = sharedPref.getBoolean("$epc@water", false)
                            val fertilizer: Boolean =
                                sharedPref.getBoolean("$epc@fertilizer", false)
                            val location: String? = sharedPref.getString("$epc@location", null)
                            val status: String? = sharedPref.getString("$epc@status", null)

                            val tag = TagModel(
                                name = name,
                                address = epc,
                                rssi = rssi,
                                image = image,
                                environment = TagEnvironment(
                                    water = water,
                                    fertilizer = fertilizer,
                                    location = location,
                                    status = status
                                )
                            )
                            data.add(tag)

                        } ?: run {
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