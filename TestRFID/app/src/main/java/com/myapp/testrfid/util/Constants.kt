package com.myapp.testrfid.util

object Constants {
    var BASE_URL = "http://172.17.0.236:8889/"
    //const val BASE_URL = "http://172.20.105.223:11000/"
    const val SHARED_PREFS = "shared_prefs"
    const val EMAIL_KEY = "email_key"
    const val PASSWORD_KEY = "password_key"
    const val USER_ACCESS_TOKEN = "user_access_token"
    const val USER_REFRESH_TOKEN= "user_refresh_token"
    const val SHARED_PREFS_SETTING = "shared_prefs_setting"
    const val IPADDRESS_KEY = "ipaddress_key"
    const val PORT_KEY = "port_key"

    const val READER_START_SUCCESS = "reader.start.success"
    const val READER_START_FAIL = "reader.start.fail"
    const val READER_INVENTORY_ACTION = "reader.inventory.action"
    const val READER_INVENTORY_DATA = "reader.inventory.data"
    const val READER_INVENTORY_DATA2 = "reader.inventory.data2"
    const val READER_START_STOP = "reader.start.stop"
    const val READER_START_STOP_DATA = "reader.start.stop.data"
    const val SCANNER_SCAN_ACTION = "scanner.scan.action"
    const val SCANNER_SCAN_DATA = "scanner.scan.data"

    const val ACTION_GLOBAL_BUTTON = "android.intent.action.GLOBAL_BUTTON"
    const val ACTION_BUTTON_DOWN = "com.apulsetech.action.BUTTON_DOWN"
    const val ACTION_BUTTON_UP = "com.apulsetech.action.BUTTON_UP"
    const val TAG_ORIGIN_UNIFIED_DEMO = 1
    const val KEY_ORIGIN = "origin"
    const val KEY_CODE = "key_code"
}
