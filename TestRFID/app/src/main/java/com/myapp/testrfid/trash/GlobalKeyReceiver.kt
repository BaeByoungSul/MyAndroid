package com.myapp.testrfid.trash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import com.apulsetech.lib.event.DeviceEvent
import com.apulsetech.lib.event.ReaderEventListener
import com.apulsetech.lib.rfid.Reader
import com.apulsetech.lib.rfid.type.RFID
import com.apulsetech.lib.rfid.type.RfidResult
import com.apulsetech.lib.util.LogUtil
import com.myapp.testrfid.App

class GlobalKeyReceiver : BroadcastReceiver() {
    private var mReader: Reader? = null
    private val mInventoryStarted = false
    init {
        initReader()
    }

    private fun initReader() {
        mReader = Reader.getReader(App.context(), true)
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
                        //sendRfidToClient(data)
                        //mRfidTextView.setText(data);
                    }
                }

                Reader.READER_CALLBACK_EVENT_START_INVENTORY -> if (!mInventoryStarted) {
                   // mInventoryStarted = true
                    //sendMessage(RfidHidActivity.MSG_READER_STARTED);
                }

                Reader.READER_CALLBACK_EVENT_STOP_INVENTORY -> if (mInventoryStarted && !mReader!!.isOperationRunning) {
                   // mInventoryStarted = false
                    //mTagList.clear();
                    //sendMessage(RfidHidActivity.MSG_READER_STOPPED);
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ${intent.action}")
        if (ACTION_GLOBAL_BUTTON == intent.action) {
            val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                ?: return
            val keyCode = event.keyCode
            val keyAction = event.action
            if (event.repeatCount > 0) {
                return
            }
            LogUtil.log(
                LogUtil.LV_D, D, TAG,
                "keyCode=$keyCode, keyAction=$keyAction"
            )
            if (keyAction == KeyEvent.ACTION_DOWN) {
                val downIntent = Intent(ACTION_BUTTON_DOWN)
                downIntent.putExtra(KEY_ORIGIN, TAG_ORIGIN_UNIFIED_DEMO)
                downIntent.putExtra(KEY_CODE, keyCode)
                context.sendBroadcast(downIntent)
            } else if (keyAction == KeyEvent.ACTION_UP) {
                val upIntent = Intent(ACTION_BUTTON_UP)
                upIntent.putExtra(KEY_ORIGIN, TAG_ORIGIN_UNIFIED_DEMO)
                upIntent.putExtra(KEY_CODE, keyCode)
                context.sendBroadcast(upIntent)
            }
        }
    }

    companion object {
        @Suppress("unused")
        private val TAG = "GlobalKeyReceiver"

        @Suppress("unused")
        private val D = true
        private const val ACTION_GLOBAL_BUTTON = "android.intent.action.GLOBAL_BUTTON"
        private const val ACTION_BUTTON_DOWN = "com.apulsetech.action.BUTTON_DOWN"
        private const val ACTION_BUTTON_UP = "com.apulsetech.action.BUTTON_UP"
        private const val TAG_ORIGIN_UNIFIED_DEMO = 1
        private const val KEY_ORIGIN = "origin"
        private const val KEY_CODE = "key_code"
    }
}
