package com.myapp.testrfid.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson

import com.myapp.testrfid.common.model.CommandType
import com.myapp.testrfid.common.model.MsSqlDataType
import com.myapp.testrfid.common.model.MyDbCommand
import com.myapp.testrfid.common.model.MyPara
import com.myapp.testrfid.common.model.ParameterDirection
import com.myapp.testrfid.common.retrofit_myapi.RetrofitObject
import com.myapp.testrfid.common.retrofit_myapi.RetrofitObjectRx
import com.myapp.testrfid.model.TagItem
import com.myapp.testrfid.model.TaskId
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch


class TagSaveViewModel: ViewModel() {
    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _toastMessage: MutableLiveData<String?> = MutableLiveData()
    val toastMessage: LiveData<String?> get() = _toastMessage

    private val _mItemHashMap: HashMap<String, TagItem> = HashMap<String, TagItem>()
    val mItemMapLiveData: MutableLiveData<HashMap<String, TagItem>> = MutableLiveData()
    private val disposable = CompositeDisposable()

    fun addTagItem(tagItem: TagItem){
        val storedItem = _mItemHashMap[tagItem.tagValue]
        if( storedItem != null ){
            // 수정
            storedItem.rssiValue = tagItem.rssiValue ?: ""
            storedItem.dupCount += 1
            _mItemHashMap[tagItem.tagValue] = storedItem
        }else {
            _mItemHashMap[tagItem.tagValue] = tagItem
        }
        ///mItemMapLiveData.value = _mItemHashMap
        mItemMapLiveData.postValue(_mItemHashMap)
    }


    private fun processTagData(tagData: String) : TagItem  {
        //updateCountText()
        var epc: String  = ""
        var rssi: String? = null
        var phase: String? = null
        var fastID: String? = null
        var channel: String? = null
        var antenna: String? = null
//        val dataItems = data.split(";".toRegex()).dropLastWhile { it.isEmpty() }
//            .toTypedArray()
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
//        val item = TagSaveItem(
//            tagHexValue = epc,
//            tagType = epc.substring(0,4).decodeHex().toString(),
//            tagValue =  epc.substring(4) .decodeHex().toString(),
//            dupCount = 1
//        )
        return TagItem(
            tagHexValue = epc,
            tagType = epc.substring(0, 4).decodeHex().toString(),
            tagValue = epc.substring(4).decodeHex().toString(),
            rssiValue = rssi ?: "",
            dupCount = 1
        )
//        val item = TagItem(
//            tagHexValue = epc,
//            tagType = epc.substring(0,4).decodeHex().toString(),
//            tagValue =  epc.substring(4) .decodeHex().toString(),
//            dupCount = 1
//        )
//        if (rssi != null) {
//            item.rssiValue = rssi
//            item.rssiMv?.add(rssi.toFloat())
//        }
//
//        return item

    }
    private fun String.decodeHex(): String {
        require(length % 2 == 0) {"Must have an even length"}
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
            .toString(Charsets.ISO_8859_1)  // Or whichever encoding your input uses
    }

//    private val mTagComparator =
//        java.util.Comparator<TagItem> { tagItem, t1 ->
//            val value = tagItem.mTagValue
//            val valueT1 = t1.mTagValue
//            val valueLength = value!!.length
//            val valueT1Length = valueT1!!.length
//            if (valueLength > valueT1Length) {
//                return@Comparator 1
//            } else if (valueLength == valueT1Length) {
//                return@Comparator value.compareTo(valueT1)
//            }
//            -1
//        }
    fun removeAllItem() {
        _mItemHashMap.clear()
        mItemMapLiveData.postValue(_mItemHashMap)
    }
    fun saveData1(){
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                Log.d(TAG, "saveData: ")
                val response = RetrofitObject.api.getDataSet(createTaskIdCommand())
                if (response.isSuccessful){
                    val data = response.body()?.getAsJsonArray("Table")
                    Log.d(TAG, "saveData: ${response.body()}")
                    val resultData = Gson().fromJson(data, Array<TaskId>::class.java)
                    Log.d(TAG, "saveData: ${resultData[0].taskId}")

                }else{
                    _toastMessage.postValue(response.errorBody()?.string())
                    //Log.d(TAG, "getDataSet: ")
                }


//                val apiRequest = createRequestParameter(
//                    boxBarcode,
//                    barcode
//                )
//                val response = apiRepository.execNonQuery(apiRequest)
//                if (response.isSuccessful && response.code() == 200 ){
//                    //Log.d(TAG, "saveData: $resBody")
//                    _outputResult.postValue(response.body())
//
//                    //_anyMutableList.add( BoxIpGoModel(barcode,"Success",boxBarcode))
//                    //_boxIpGoList.postValue(_anyMutableList)
//                }
//                else{
//                    Log.d(TAG, "saveData:errorBody is stream ")
//                    _toastMessage.postValue(response.errorBody()?.string())
//                }
            }catch ( ex: Exception){
                _toastMessage.postValue(ex.message)
                //Log.d(TAG, "saveData: ${ex.message}")
            }finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun saveData(){
        _isLoading.postValue(true)
            Log.d(TAG, "saveData: ")
            RetrofitObjectRx
                .api.getDataSet(createTaskIdCommand())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { response ->
                    //saveLoginInfo(response.data)
                    val data = response.body()?.getAsJsonArray("Table")
                    val resultData = Gson().fromJson(data, Array<TaskId>::class.java)
                    Log.d(TAG, "saveData: ${resultData[0].taskId}")

                    return@flatMap RetrofitObject.apiRx
                        .execNonQueryRx(createSaveCommand(resultData[0].taskId))
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe (
                    { response ->
                        Log.d(TAG, "saveData: ${response.body()}")
                        removeAllItem()
                    },
                    { err -> Log.v("TAG", err.localizedMessage) },
                    {
                        Log.v("TAG", "Chains Completed")
                        _isLoading.postValue(false)
                    }
                )
                .addTo(disposable)
    }
    private fun createTaskIdCommand(): MyDbCommand {
        return MyDbCommand(
            commandName = "MST",
            connectionName = "Phi_PDA",
            commandType = CommandType.StoredProcedure.type,
            commandText = "WebApiDB..USP_GET_TASK_ID",
//            parameters = mutableListOf( para1,para2, para3),
//            paraValues = mutableListOf(paraValue)
        )
    }
    private fun createSaveCommand(taskId: Long):  List<MyDbCommand> {
        val saveCommand: MutableList<MyDbCommand> = mutableListOf()
        saveCommand.add(createHdrCommand(taskId))
        saveCommand.add(createDtlCommand(taskId))

        return saveCommand
    }
    private fun createHdrCommand(taskId: Long): MyDbCommand {

        val myCmd1 = MyDbCommand(
            commandName = "MST",
            connectionName = "Phi_PDA",
            commandType = CommandType.StoredProcedure.type,
            commandText = "WebApiDB..[USP_PDA_BBS_EXEC_HDR]",
//            parameters = mutableListOf( para1,para2, para3),
//            paraValues = mutableListOf(paraValue)
        )
        val para11 = MyPara(parameterName = "@TASK_ID",
            dbDataType = MsSqlDataType.Varchar.type,
            direction = ParameterDirection.Input.type)
        myCmd1.parameters = mutableListOf( para11)
        val paraValue = hashMapOf(
            "@TASK_ID" to taskId.toString()
        )
        myCmd1.paraValues = mutableListOf(paraValue)
        return myCmd1
    }
    private fun createDtlCommand(taskId: Long ): MyDbCommand {
        val myCmd1 = MyDbCommand(
            commandName = "DTL",
            connectionName = "Phi_PDA",
            commandType = CommandType.StoredProcedure.type,
            commandText = "WebApiDB..[USP_PDA_BBS_EXEC_DTL]",
//            parameters = mutableListOf( para1,para2, para3),
//            paraValues = mutableListOf(paraValue)
        )
        val para11 = MyPara(parameterName = "@TASK_ID",
            dbDataType = MsSqlDataType.Varchar.type,
            direction = ParameterDirection.Input.type)
        val para12 = MyPara(parameterName = "@TAG_VALUE",
            dbDataType = MsSqlDataType.Varchar.type,
            direction = ParameterDirection.Input.type)
        myCmd1.parameters = mutableListOf( para11, para12 )
        val paraValues: MutableList<HashMap<String, String>> = mutableListOf()

        for ((key, tagItem) in _mItemHashMap) {
            paraValues.add( hashMapOf(
                "@TASK_ID" to taskId.toString(),
                "@TAG_VALUE" to tagItem.tagValue
            ))
        }
        myCmd1.paraValues = paraValues

        return myCmd1
    }


    companion object {
        private const val TAG = "BoxIpGoViewModel"
    }
    fun addTagItem_bak(tagData: String){
        val item = processTagData(tagData)
        val storedItem = _mItemHashMap[item.tagValue]
        if( storedItem != null ){
            // 수정
            if (item.rssiValue != null) {
                storedItem.rssiValue = item.rssiValue ?: ""
                //storedItem.rssiMv?.add(item.rssiValue!!.toFloat())
            }
            storedItem.dupCount += 1
            _mItemHashMap[item.tagValue] = storedItem
        }else {
            _mItemHashMap[item.tagValue] = item
        }
        ///mItemMapLiveData.value = _mItemHashMap
        mItemMapLiveData.postValue(_mItemHashMap)


    }
//
}


