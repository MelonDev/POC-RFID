package com.melondev.poc_rfid

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.honeywell.rfidservice.EventListener
import com.honeywell.rfidservice.RfidManager
import com.honeywell.rfidservice.TriggerMode
import com.honeywell.rfidservice.rfid.*
import com.melondev.poc_rfid.adapters.ListDialogRecyclerViewAdapter
import com.melondev.poc_rfid.adapters.ScannerRecyclerViewAdapter
import com.melondev.poc_rfid.callback.ItemCallback
import com.melondev.poc_rfid.callback.TagCallback
import com.melondev.poc_rfid.model.*
import java.text.DateFormat
import java.text.SimpleDateFormat

class ScannerFragment : Fragment() {

    private lateinit var rfidManager: RfidManager
    private lateinit var rfidReader: RfidReader
    private var tags: MutableList<String> = ArrayList()

    private var dialog: BottomSheetDialog? = null

    private lateinit var sharedPref: SharePref

    private var selectedGroup: String = "ไม่มีรูปแบบ"


    private lateinit var groupCardView: CardView
    private lateinit var groupTextView: TextView

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
        val view = inflater.inflate(R.layout.fragment_scanner_list, container, false)


        recyclerview = view.findViewById<RecyclerView>(R.id.scanner_recycler_view)
        notAvailableView = view.findViewById<TextView>(R.id.scanner_not_available)

        groupCardView = view.findViewById<CardView>(R.id.scanner_group_card)
        groupTextView = view.findViewById<TextView>(R.id.scanner_group_text)


        if (isReaderAvailable()) {
            notAvailableView.visibility = View.GONE

        } else {
            notAvailableView.visibility = View.VISIBLE

        }

        recyclerview.layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = this.adapter



        groupCardView.setOnClickListener {

            var choices: MutableList<ItemModel> = ArrayList()
            choices.apply {
                add(
                    ItemModel(
                        name = "ไม่มีรูปแบบ",
                        enable = selectedGroup.contains("ไม่มีรูปแบบ")
                    )
                )
                add(
                    ItemModel(
                        name = "ตามชื่อ",
                        enable = selectedGroup.contains("ตามชื่อ")
                    )
                )
                add(
                    ItemModel(
                        name = "ตามตำแหน่ง",
                        enable = selectedGroup.contains("ตามตำแหน่ง")
                    )
                )
                add(
                    ItemModel(
                        name = "ตามสถานะ",
                        enable = selectedGroup.contains("ตามสถานะ")
                    )
                )

                add(
                    ItemModel(
                        name = "ตามล็อต",
                        enable = selectedGroup.contains("ตามล็อต")
                    )
                )

            }

            showDialog(
                title = "กรุณาเลือกรูปแบบการนับ",
                choices = choices,
                callback = groupItemCallback
            )
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

        }

        override fun onDeviceDisconnected(o: Any) {
            notAvailableView.visibility = View.VISIBLE

        }

        override fun onReaderCreated(b: Boolean, rfidReader: RfidReader) {
            notAvailableView.visibility = View.GONE
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

    private val groupItemCallback: ItemCallback = object : ItemCallback {
        override fun onClick(item: ItemModel) {
            /*selectedTag?.let {
                sharedPref.putString("${it.address}@location", item.name)
                sharedPref.commit()

                areaTextView.text = item.name ?: "ไม่ทราบ"
                it.environment?.location = item.name

            }
             */

            selectedGroup = item.name
            groupTextView.text = item.name


            dialog?.dismiss()


        }
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

                            if (lot != null && selectedGroup.contains("ตามล็อต")) {

                                val tag = TagModel(
                                    name = name,
                                    address = epc,
                                    rssi = rssi,
                                    image = image,
                                    lot = lot,
                                    callback = tagCallback,
                                    environment = TagEnvironment(
                                        water = water,
                                        fertilizer = fertilizer,
                                        location = location,
                                        status = status
                                    )
                                )
                                data.add(tag)

                            } else {
                                val tag = TagModel(
                                    name = name,
                                    address = epc,
                                    rssi = rssi,
                                    image = image,
                                    callback = tagCallback,
                                    environment = TagEnvironment(
                                        water = water,
                                        fertilizer = fertilizer,
                                        location = location,
                                        status = status
                                    )
                                )
                                data.add(tag)
                            }


                        } ?: run {
                            val tag = TagModel(address = epc, rssi = rssi)
                            data.add(tag)
                        }

                    }

                    val groupData: MutableList<BaseModel> = ArrayList()

                    if (selectedGroup.contains("ไม่มีรูปแบบ")) {
                        data.sortByDescending {
                            it.rssi
                        }
                        groupData.addAll(data)
                        adapter.addAll(groupData)
                    } else {
                        var groupTag: Map<String, List<TagModel>>? = null
                        if (selectedGroup.contains("ตามตำแหน่ง")) {
                            groupTag = data.groupBy {
                                it.environment?.location ?: run {
                                    "ไม่ทราบตำแหน่ง"
                                }
                            }
                        } else if (selectedGroup.contains("ตามสถานะ")) {
                            groupTag = data.groupBy {
                                it.environment?.status ?: run {
                                    "ไม่ทราบสถานะ"
                                }
                            }
                        } else if (selectedGroup.contains("ตามชื่อ")) {
                            groupTag = data.groupBy {
                                it.name ?: run {
                                    "ไม่ทราบชื่อ"
                                }
                            }

                        } else if (selectedGroup.contains("ตามล็อต")) {

                            groupTag = data.groupBy {
                                it.lot ?: run {
                                    "ไม่ทราบล็อต"
                                }
                            }


                        }



                        groupTag?.let { groupTags ->
                            val gt = groupTags.toList().sortedBy { (key, _) -> key }.toMap()


                            gt.forEach { name, items ->
                                val smallGroup: MutableList<TagModel> = ArrayList()
                                smallGroup.addAll(items)
                                smallGroup.sortByDescending {
                                    it.rssi
                                }

                                groupData.add(
                                    TitleCounterModel(
                                        name = if (selectedGroup.contains("ตามล็อต")) (if (name.contains("ไม่ทราบล็อต")) name else "ล็อต $name") else name,
                                        count = items.size
                                    )
                                )
                                groupData.addAll(smallGroup)


                            }
                            adapter.addAll(groupData)
                        }

                    }

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