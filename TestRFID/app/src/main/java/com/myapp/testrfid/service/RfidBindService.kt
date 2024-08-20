package com.myapp.testrfid.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.apulsetech.lib.barcode.Scanner
import com.apulsetech.lib.barcode.type.BarcodeType
import com.apulsetech.lib.event.DeviceEvent
import com.apulsetech.lib.event.ReaderEventListener
import com.apulsetech.lib.event.ScannerEventListener
import com.apulsetech.lib.rfid.Reader
import com.apulsetech.lib.rfid.type.RFID
import com.apulsetech.lib.rfid.type.RfidResult
import com.myapp.testrfid.model.TagItem
import com.myapp.testrfid.util.Constants.ACTION_BUTTON_DOWN
import com.myapp.testrfid.util.Constants.ACTION_BUTTON_UP
import com.myapp.testrfid.util.Constants.READER_INVENTORY_ACTION
import com.myapp.testrfid.util.Constants.READER_INVENTORY_DATA
import com.myapp.testrfid.util.Constants.READER_INVENTORY_DATA2
import com.myapp.testrfid.util.Constants.READER_START_FAIL
import com.myapp.testrfid.util.Constants.READER_START_STOP
import com.myapp.testrfid.util.Constants.READER_START_STOP_DATA
import com.myapp.testrfid.util.Constants.READER_START_SUCCESS
import com.myapp.testrfid.util.Constants.SCANNER_SCAN_ACTION
import com.myapp.testrfid.util.Constants.SCANNER_SCAN_DATA
import com.myapp.testrfid.util.ReaderType
import kotlin.math.log

// Bind Service
// 1. RFID Reader : Tag Data를 읽어서 TagItem으로 sendBroadcast
// 2. Scanner     : barcode 데이터 sendBroadcast
// 3. mKeyReceiver: a811 좌측 버튼을 클릭 시 startAndStopInventory
// 3-1. RFID Reader : 버튼 클릭 첫번째  startInventory 실행
//                 => 한번 더 클릭하면  stopInventory 실행
// 3-2. Scanner     : 기본적으로 읽기가 완료되면 자동 정지 ( startDecode, stopDecode )
// 3-2-1 버튼 클릭 첫번째  startDecode 실행 => 빔이 켜짐 => 읽기 완료되면 자동으로 빔이 꺼짐
// 3-2-2 버튼 클릭 첫번째  startDecode 실행 => 빔이 켜짐 => 읽기 전에 한번더 클릭 => stopDedode실행

class RfidBindService() : Service() {
    private lateinit var mReader: Reader
    private lateinit var mScanner: Scanner
    private var mIsInventorying = false
    private var mReaderType = ReaderType.SCANNER
    private val tone = ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)

    //ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
    //tone.startTone(ToneGenerator.TONE_DTMF_S, 500);

    inner class MyBinder: Binder(){
        fun getService(): RfidBindService{
            return this@RfidBindService
        }
    }
    private val binder = MyBinder()
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
        initReader()
        initScanner()
        regKeyReceiver()
        Toast.makeText(applicationContext,"Service Created",Toast.LENGTH_SHORT).show();
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        super.onDestroy()
        mReader.stop()
        mReader.destroy()
        mReader.removeEventListener(mRfidEventListener)

        mScanner.stop()
        mScanner.destroy()
        mScanner.removeEventListener(mBarcodeEventListener)

        unregisterReceiver(mKeyReceiver)
    }
    private fun initReader() {

        val intent = Intent()
        mReader = Reader.getReader(this)
        mReader.setEventListener(mRfidEventListener)
        if (mReader.start()) {
            Log.d(TAG, "start: Reader open success!")
            mReader.setInventoryRssiReportState(RFID.ON)
            intent.setAction(READER_START_SUCCESS)
        } else {
            Log.d(TAG, "start: Reader open failed!")
            intent.setAction(READER_START_FAIL)
        }

        sendBroadcast(intent)

    }
    private fun initScanner() {
        mScanner = Scanner.getScanner(this)
        mScanner.setEventListener(mBarcodeEventListener)
        if (mScanner.start()) {
            Log.d(TAG, "start: scanner open success!")
        } else {
            Log.e(TAG, "failed to start scanner!")
        }

    }

    fun startAndStopInventory(){
        val ret: Int
        tone.startTone(ToneGenerator.TONE_DTMF_S, 500);

        if (mReaderType == ReaderType.SCANNER){
            if (mScanner.isDecoding) {
                mIsInventorying = false;
                mScanner.stopDecode();
            } else {
                mIsInventorying = true;
                mScanner.startDecode(false);
            }
            return
        }

        // mreader가 준비가 되었는지 점검
        if (mIsInventorying) {
            ret = mReader.stopOperation();
            if (ret == RfidResult.SUCCESS) {
                mIsInventorying = false;
            }
        } else {
            if (mReader.isOperationRunning) return
            ret = mReader.startInventory();
            if (ret == RfidResult.SUCCESS) {
                mIsInventorying = true;
            }
        }
        val intent = Intent()
        intent.setAction(READER_START_STOP)
        intent.putExtra(READER_START_STOP_DATA, mIsInventorying)
        sendBroadcast(intent)
    }

    fun setReader(readerType: ReaderType){
        if (mReader.isOperationRunning){
            throw IllegalArgumentException("RFID is running!!")

        }
        mReaderType = readerType
    }
    fun getCurrentReader(): ReaderType {
        Log.d(TAG, "getCurrentReader: ${mReaderType.name}")
        return mReaderType
    }
    fun getReader(): Reader {
        return mReader
    }

    private val mRfidEventListener: ReaderEventListener = object : ReaderEventListener {
        override fun onReaderDeviceStateChanged(state: DeviceEvent?) {}

        override fun onReaderEvent(event: Int, result: Int, data: String?) {
            when (event) {
                Reader.READER_CALLBACK_EVENT_INVENTORY,
                Reader.READER_CALLBACK_EVENT_READ -> if (result == RfidResult.SUCCESS) {
                    if (data != null) {
                        //Log.d(Companion.TAG, "onReaderEvent: $data")
                        sendRfidToClient(data)

                        //processTagData(data)
                        //mRfidTextView.setText(data)
                    }
                }
            }
        }
    }
    private val mBarcodeEventListener : ScannerEventListener = object : ScannerEventListener{
        override fun onScannerDeviceStateChanged(p0: DeviceEvent?) {
            super.onScannerDeviceStateChanged(p0)
        }
        override fun onScannerEvent(p0: BarcodeType?, p1: String?) {
            super.onScannerEvent(p0, p1)
            if (p0 != BarcodeType.NO_READ){
                Log.d(TAG, "onScannerEvent: $p1")
                p1?.let { sendBarcodeToClient(it) }
            }
        }


    }
    // TagItem class Parcelable
    private fun sendRfidToClient(tagData: String) {
        val tagItem = processTagData(tagData)

//        Log.d(TAG, "sendRfidToClient: $tagItem")
//        Log.d(TAG, "sendRfidToClient: $tagData")
//        return

        Intent().apply {
            setAction(READER_INVENTORY_ACTION)
            putExtra(READER_INVENTORY_DATA, tagItem)
            putExtra(READER_INVENTORY_DATA2, tagData)
            sendBroadcast(this)
        }
    }

    private fun sendBarcodeToClient(barcode: String) {
        val intent = Intent()
        intent.setAction(SCANNER_SCAN_ACTION)
        intent.putExtra(SCANNER_SCAN_DATA, barcode)
        sendBroadcast(intent)
    }
    private val mKeyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (ACTION_BUTTON_DOWN == action) {
                Log.d(TAG, "onReceive: ACTION_BUTTON_DOWN")
                startAndStopInventory()
            } else if (ACTION_BUTTON_UP == action) {
                Log.d(TAG, "onReceive: ACTION_BUTTON_UP")

            }
        }

    }
    private fun regKeyReceiver(){

        val filter = IntentFilter()
        filter.addAction(ACTION_BUTTON_DOWN)
        filter.addAction(ACTION_BUTTON_UP)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mKeyReceiver, filter, RECEIVER_NOT_EXPORTED)
        }else @SuppressLint("UnspecifiedRegisterReceiverFlag")
        {
            registerReceiver(mKeyReceiver, filter)
        }

    }
    // TagData : 구분자로 분리해서 처리함
    private fun processTagData(tagData: String): TagItem {
        //updateCountText()
        var epc: String = ""
        var rssi: String? = null
        var phase: String? = null
        var fastID: String? = null
        var channel: String? = null
        var antenna: String? = null
        val dataItems = tagData.split(";")

        for (dataItem in dataItems) {
            if (dataItem.contains("rssi")) {
                val point = dataItem.indexOf(':') + 1
                rssi = dataItem.substring(point)
            } else if (dataItem.contains("phase")) {
                val point = dataItem.indexOf(':') + 1
                phase = dataItem.substring(point)
            } else if (dataItem.contains("fastID")) {
                val point = dataItem.indexOf(':') + 1
                fastID = dataItem.substring(point)
            } else if (dataItem.contains("channel")) {
                val point = dataItem.indexOf(':') + 1
                channel = dataItem.substring(point)
            } else if (dataItem.contains("antenna")) {
                val point = dataItem.indexOf(':') + 1
                antenna = dataItem.substring(point)
            } else {
                epc = dataItem
            }
        }

        return TagItem(
            tagHexValue = epc,
            tagType = epc.substring(0, 4).decodeHex().toString(),
            tagValue = epc.substring(4).decodeHex().toString(),
            rssiValue = rssi ?: "",
            dupCount = 1
        )

    }
    private fun String.decodeHex(): String {
        require(length % 2 == 0) {"Must have an even length"}
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
            .toString(Charsets.ISO_8859_1)  // Or whichever encoding your input uses
    }


    companion object {
        const val TAG = "RfidBindService"

    }
    // Bound 서비스 메세지 직접 호출
    fun serviceMessage() : String {
        return "Hello Activity! I am Service!"
    }

}