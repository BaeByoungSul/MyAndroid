package com.myapp.myframedagger.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.myapp.myframedagger.common.utils.Constants.SCAN_ACTION
import com.myapp.myframedagger.common.utils.Constants.SCAN_DATA

import device.common.DecodeResult
import device.common.ScanConst
import device.sdk.ScanManager


class ScanService : Service() {

    private val receiver = object: BroadcastReceiver(){
        private val TAG = "ScanService"
        private var mScanner: ScanManager = ScanManager()
        private var mDecodeResult: DecodeResult = DecodeResult()
        private val enableBeep = true

        init {
            mScanner.aDecodeSetResultType(ScanConst.RESULT_USERMSG)
            //mScanner.aDecodeSetResultType(ScanConst.RESULT_EVENT)

            if (enableBeep) {
                mScanner.aDecodeSetBeepEnable(1)
            } else {
                mScanner.aDecodeSetBeepEnable(0)
            }
            mScanner.aDecodeSymSetEnable(ScanConst.SymbologyID.DCD_SYM_QR, 0)
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null ) return
            val isReadSuccess = intent.getBooleanExtra("EXTRA_USERMSG", false)
            if (isReadSuccess) {
                mScanner.aDecodeGetResult(mDecodeResult.recycle())
                // 스캔한 바코드 sendBroadcast
                Intent().apply {
                    setAction(SCAN_ACTION)
                    putExtra(SCAN_DATA, mDecodeResult.toString())
                    sendBroadcast(this)
                }
                //Log.d(TAG, "SCAN SUCCESS : $mDecodeResult")
                //Log.d(TAG, "onReceive: ${mDecodeResult.symType}, ${mDecodeResult.symName}")
            } else
                Log.d(TAG, "SCAN FAIL !!")
        }

        fun onReceive_bak(context: Context?, intent: Intent?) {
            try {
                if (ScanConst.INTENT_USERMSG == intent!!.action) {
                    val isReadSuccess = intent.getBooleanExtra("EXTRA_USERMSG", false)

                    if (isReadSuccess) {
                        mScanner.aDecodeGetResult(mDecodeResult.recycle())
                        Log.d(TAG, "SCAN SUCCESS : $mDecodeResult")
                        Log.d(TAG, "onReceive: ${mDecodeResult.symType}, ${mDecodeResult.symName}")
                    } else
                        Log.d(TAG, "SCAN FAIL !!")
                } else if (ScanConst.INTENT_EVENT == intent.action) {
                    val result =
                        intent.getBooleanExtra(ScanConst.EXTRA_EVENT_DECODE_RESULT, false)
                    val decodeBytesLength =
                        intent.getIntExtra(ScanConst.EXTRA_EVENT_DECODE_LENGTH, 0)
                    val decodeBytesValue =
                        intent.getByteArrayExtra(ScanConst.EXTRA_EVENT_DECODE_VALUE)
                    val decodeValue = String(decodeBytesValue!!, 0, decodeBytesLength)
                    val decodeLength = decodeValue.length
                    val symbolName = intent.getStringExtra(ScanConst.EXTRA_EVENT_SYMBOL_NAME)
                    val symbolId =
                        intent.getByteExtra(ScanConst.EXTRA_EVENT_SYMBOL_ID, 0.toByte())
                    val symbolType = intent.getIntExtra(ScanConst.EXTRA_EVENT_SYMBOL_TYPE, 0)
                    val letter =
                        intent.getByteExtra(ScanConst.EXTRA_EVENT_DECODE_LETTER, 0.toByte())
                    val modifier =
                        intent.getByteExtra(ScanConst.EXTRA_EVENT_DECODE_MODIFIER, 0.toByte())
                    val decodingTime = intent.getIntExtra(ScanConst.EXTRA_EVENT_DECODE_TIME, 0)
                    Log.d(TAG, "1. result: $result")
                    Log.d(TAG, "2. bytes length: $decodeBytesLength")
                    Log.d(TAG, "3. bytes value: $decodeBytesValue")
                    Log.d(TAG, "4. decoding length: $decodeLength")
                    Log.d(TAG, "5. decoding value: $decodeValue")
                    Log.d(TAG, "6. symbol name: $symbolName")
                    Log.d(TAG, "7. symbol id: $symbolId")
                    Log.d(TAG, "8. symbol type: $symbolType")
                    Log.d(TAG, "9. decoding letter: $letter")
                    Log.d(TAG, "10.decoding modifier: $modifier")
                    Log.d(TAG, "11.decoding time: $decodingTime")
                    //MainActivity.mBarType.setText(symbolName)
                    //MainActivity.mResult.setText(decodeValue)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    override fun onCreate() {
        super.onCreate()
        // 2> IntentFilter 변수 설정하기
        val filter = IntentFilter()
        filter.addAction(ScanConst.INTENT_USERMSG)
        //filter.addAction(ScanConst.INTENT_EVENT)

        // 3> registerReceiver 함수 호출하기
        //receiver = ScanReceiver()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        }else @SuppressLint("UnspecifiedRegisterReceiverFlag"){
            registerReceiver(receiver, filter)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
       //TODO("Return the communication channel to the service.")
        return null
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

}

//class ScanService2 : Service() {
//        var receiver: BroadcastReceiver? = null
//    class ScanReceiver : BroadcastReceiver() {
//        private val TAG = "ScanReceiver"
//        private var mScanner: ScanManager
//        private var mDecodeResult: DecodeResult
//
//        init {
//            mDecodeResult = DecodeResult()
//            mScanner = ScanManager()
//        }
//
//        override fun onReceive(context: Context?, intent: Intent?) {
//            mScanner.aDecodeGetResult(mDecodeResult.recycle())
//            if (!mDecodeResult.symName.equals("READ_FAIL")){
//                Log.d(TAG, "onReceive: ${mDecodeResult.toString()}")
//            }
//        }
//
//        fun setScannerResultType() {
//            mScanner.apply {
//                if (aDecodeGetDecodeEnable() == 1){
//                    //initScanner()
//                    aDecodeSetResultType(ScanConst.RESULT_USERMSG)
//                }
//            }
//        }
//
//    }
//
//
//    override fun onCreate() {
//        super.onCreate()
//        // 2> IntentFilter 변수 설정하기
//        val intentFilter: IntentFilter = IntentFilter(ScanConst.INTENT_USERMSG)
//
//        // 3> registerReceiver 함수 호출하기
//        //receiver = ScanReceiver()
//        registerReceiver(receiver, intentFilter, RECEIVER_NOT_EXPORTED)
//    }
//
//
//    override fun onDestroy() {
//        super.onDestroy()
//        unregisterReceiver(receiver)
//    }
//
//    override fun onBind(intent: Intent): IBinder {
//        TODO("Return the communication channel to the service.")
//    }
//}