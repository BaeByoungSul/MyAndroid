package com.myapp.testrfid.trash

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import com.apulsetech.lib.barcode.Scanner
import com.apulsetech.lib.barcode.type.BarcodeType
import com.apulsetech.lib.event.DeviceEvent
import com.apulsetech.lib.event.ReaderEventListener
import com.apulsetech.lib.event.ScannerEventListener
import com.apulsetech.lib.rfid.Reader
import com.apulsetech.lib.rfid.type.RFID
import com.apulsetech.lib.rfid.type.RfidResult

/*****************************************************************************************
 * RFID Reader Bind Service..
 * . BUTTON_DOWN, BUTTON_UP Key receiver 에서 startDecode, startInventory
 * . READER_OR_SCANNER action receiver 에서는 Barcode Scanner 사용 혹은 RFID Reader 사용 설정
 */
class ApulseService : Service() {
    // Binder given to clients
    private val mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: ApulseService
            get() = this@ApulseService
    }

    private var mReader: Reader? = null
    private var mScanner: Scanner? = null

    // Apulse 관련 function
    var currentReader = "READER" // SCANNER OR READER
    private var mInventoryStarted = false
    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBind: ")
        initReader()
        initScanner()
        registerKeyReceiver()
        registerWhatToUseReceiver()
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        stopReader()
        stopScanner()
        unregisterReceiver(mKeyReceiver)
        unregisterReceiver(mWhatToReceiver)
        return super.onUnbind(intent)
    }

    fun getmReader(): Reader? {
        return mReader
    }

    fun getmScanner(): Scanner? {
        return mScanner
    }

    private fun initReader() {
        mReader = Reader.getReader(this, true)
        if (mReader != null) {

            mReader!!.setEventListener(mRfidEventListener)
            if (mReader!!.start()) {
                Log.d(TAG, "start: Reader open success!")
                mReader!!.setInventoryRssiReportState(RFID.OFF)
            } else {
                Log.d(TAG, "start: Reader open failed!")
            }
        } else {
            Log.d(TAG, "start: Reader instance is null!")
        }
    }

    private fun initScanner() {
        mScanner = Scanner.getScanner(this)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mScanner = Scanner.getScanner(this)
            if (mScanner != null) {
                mScanner!!.setEventListener(mBarcodeEventListener)
                if (!mScanner!!.start()) {
                    Log.e(TAG, "failed to start scanner!")
                }
            } else {
                Log.e(TAG, "failed to get scanner instance.")
            }
        } else {
            mScanner = Scanner.getScanner(this)
            if (mScanner != null) {
                mScanner!!.setEventListener(mBarcodeEventListener)
                if (!mScanner!!.start()) {
                    Log.e(TAG, "failed to start scanner!")

                    // mBarcodeScanButton.setEnabled(false);
                }
            } else {
                Log.e(TAG, "failed to get scanner instance.")

                // mBarcodeScanButton.setEnabled(false);
            }
        }
    }

    private fun registerKeyReceiver() {
        val filter = IntentFilter()
        filter.addAction(ACTION_BUTTON_DOWN)
        filter.addAction(ACTION_BUTTON_UP)
        registerReceiver(mKeyReceiver, filter)
    }

    private fun registerWhatToUseReceiver() {
        val filter = IntentFilter()
        filter.addAction(ACTION_READER_OR_SCANNER)
        registerReceiver(mWhatToReceiver, filter)
    }

    private fun stopReader() {
        if (mReader != null) {
            mReader!!.stop()
            mReader!!.destroy()
            mReader!!.removeEventListener(mRfidEventListener)
            mReader = null
        } else {
            Log.d(TAG, "stopReader: " + "Reader instance is null.")
        }
    }

    private fun stopScanner() {
        if (mScanner != null) {
            mScanner!!.stop()
            mScanner!!.destroy()
            mScanner!!.removeEventListener(mBarcodeEventListener)
            mScanner = null
        }
    }

    private val mRfidEventListener: ReaderEventListener = object : ReaderEventListener {
        override fun onReaderDeviceStateChanged(deviceEvent: DeviceEvent) {
            Log.d(TAG, "onReaderDeviceStateChanged: $deviceEvent")
            if (deviceEvent == DeviceEvent.DISCONNECTED) {
                mReader = null
                //sendMessage(RfidHidActivity.MSG_SERVICE_DISCONNECTED);
            } else if (deviceEvent == DeviceEvent.USB_CHARGING_ENABLED) {
                //Notice.ShowUsbChargingEnabledAlert(this);
            }
        }

        override fun onReaderEvent(event: Int, result: Int, data: String?) {
            when (event) {
                Reader.READER_CALLBACK_EVENT_INVENTORY, Reader.READER_CALLBACK_EVENT_READ -> if (result == RfidResult.SUCCESS) {
                    if (data != null) {
                        Log.d(TAG, "rfid : $data")
                        sendRfidToClient(data)
                        //mRfidTextView.setText(data);
                    }
                }

                Reader.READER_CALLBACK_EVENT_START_INVENTORY -> if (!mInventoryStarted) {
                    mInventoryStarted = true
                    //sendMessage(RfidHidActivity.MSG_READER_STARTED);
                }

                Reader.READER_CALLBACK_EVENT_STOP_INVENTORY -> if (mInventoryStarted && !mReader!!.isOperationRunning) {
                    mInventoryStarted = false
                    //mTagList.clear();
                    //sendMessage(RfidHidActivity.MSG_READER_STOPPED);
                }
            }
        }
    }

    private fun sendRfidToClient(tagHex: String) {
        val intent = Intent()
        intent.setAction(ACTION_SEND_RFID_MSG)
        intent.putExtra("ScanMessage", tagHex)
        sendBroadcast(intent)
    }

    private val mBarcodeEventListener: ScannerEventListener = object : ScannerEventListener {
        override fun onScannerDeviceStateChanged(deviceEvent: DeviceEvent) {
            //ScannerEventListener.super.onScannerDeviceStateChanged(deviceEvent);
        }

        override fun onScannerEvent(barcodeType: BarcodeType, s: String) {
            //ScannerEventListener.super.onScannerEvent(barcodeType, s);
            Log.d(TAG, "barcode : $s")

            //ScannerEventListener.super.onScannerEvent(barcodeType, s);
            if (barcodeType != BarcodeType.NO_READ) {
                Log.d(TAG, "barcode : $s")
                // C
                sendBarcodeToClient(s)
            }
        }
    }

    private fun sendBarcodeToClient(barcode: String) {
        val intent = Intent()
        intent.setAction(ACTION_SEND_BARCODE_MSG)
        intent.putExtra("ScanMessage", barcode)
        sendBroadcast(intent)
    }

    // Device Scan Key or Reader Key Receiver >> start reader or stop reader
    private val mKeyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val origin = intent.getIntExtra(KEY_ORIGIN, TAG_ORIGIN_UNIFIED_DEMO)
            val keyCode = intent.getIntExtra(KEY_CODE, KeyEvent.KEYCODE_UNKNOWN)
            Log.d(TAG, "onReceive:  action=$action, origin=$origin")
            if (origin == TAG_ORIGIN_UNIFIED_DEMO) {
                Log.d(TAG, "onReceive: " + "Key broadcast for Demo App!")
                return
            }
            if (ACTION_BUTTON_DOWN == action) {
                Log.d(TAG, "onReceive: ACTION_BUTTON_DOWN")
                //processKeyDown();
            } else if (ACTION_BUTTON_UP == action) {
                Log.d(TAG, "onReceive: ACTION_BUTTON_UP")
                processKeyUp()
            }
        }
    }

    private fun processKeyUp() {
        Log.d(TAG, "processKeyUp: ")
        if (currentReader == "SCANNER") {
            processKeyUpScanner()
        } else {
            processKeyUpReader()
        }
    }

    private fun processKeyUpReader() {
        Log.d(TAG, "processKeyUp: ")
        if (!mInventoryStarted) {
            val ret = mReader!!.startInventory()
            if (ret == RfidResult.SUCCESS) {
                mInventoryStarted = true
            } else if (ret == RfidResult.LOW_BATTERY) {
                Toast.makeText(
                    applicationContext,
                    "R.string.rfid_alert_low_battery_warning",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "R.string.rfid_alert_start_inventory_failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            val ret = mReader!!.stopOperation()
            if (ret == RfidResult.SUCCESS) {
                mInventoryStarted = false
            } else if (ret == RfidResult.STOP_FAILED_TRY_AGAIN) {
                Toast.makeText(
                    applicationContext,
                    "R.string.rfid_alert_stop_inventory_failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun processKeyUpScanner() {
        Log.d(TAG, "processKeyUpScanner: ")
        if (mScanner == null) {
            Log.i(TAG, "GetBarcode : mScanner is null. ")
        }
        if (mScanner!!.isDecoding) {
            mScanner!!.stopDecode()
        } else {
            mScanner!!.startDecode(false)
        }
    }

    private val mWhatToReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_READER_OR_SCANNER != action) return
            if (mInventoryStarted) {
                Log.d(TAG, "onReceive: mWhatToReceiver : 처리 중... ")
                return
            }
            val whatToUse = intent.getStringExtra("READER_OR_SCANNER")
            Log.d(TAG, "onReceive: mWhatToReceiver : $whatToUse")
            if (whatToUse == "Reader" || whatToUse == "Scanner") {
                currentReader = whatToUse
            }
        }
    }

    companion object {
        private const val TAG = "ApulseService"
        private const val ACTION_BUTTON_DOWN = "com.apulsetech.action.BUTTON_DOWN"
        private const val ACTION_BUTTON_UP = "com.apulsetech.action.BUTTON_UP"
        private const val TAG_ORIGIN_UNIFIED_DEMO = 1
        private const val KEY_ORIGIN = "origin"
        private const val KEY_CODE = "key_code"
        private const val ACTION_SEND_BARCODE_MSG = "com.kolon.SEND_BARCODE_MSG"
        private const val ACTION_SEND_RFID_MSG = "com.kolon.SEND_RFID_MSG"
        private const val ACTION_READER_OR_SCANNER = "com.kolon.READER_OR_SCANNER"
    }
}
