package com.myapp.myframedagger.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.myapp.myframedagger.common.model.Album
import com.myapp.myframedagger.common.model.CommandType
import com.myapp.myframedagger.common.model.ExecReturn
import com.myapp.myframedagger.common.model.LoginRequest
import com.myapp.myframedagger.common.model.LoginResponse
import com.myapp.myframedagger.common.model.MsSqlDataType
import com.myapp.myframedagger.common.model.MyDbCommand
import com.myapp.myframedagger.common.model.MyPara
import com.myapp.myframedagger.common.model.ParameterDirection
import com.myapp.myframedagger.common.retrofit_myapi.ApiService
import com.myapp.myframedagger.common.retrofit_myapi.RetrofitObject
import com.myapp.myframedagger.common.retrofit_simple.AlbumService
import com.myapp.myframedagger.common.retrofit_simple.RetrofitInstance
import com.myapp.myframedagger.model.MainDataclass
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

class MainViewModel: ViewModel() {
    private val TAG = "LoginViewModel"

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _toastMessage: MutableLiveData<String?> = MutableLiveData()
    val toastMessage: LiveData<String?> get() = _toastMessage

    private val _loginSuccess: MutableLiveData<LoginResponse> = MutableLiveData()
    val loginSuccess: LiveData<LoginResponse?> get() = _loginSuccess

    private val _getDataSuccess: MutableLiveData<Array<MainDataclass>> = MutableLiveData()
    val getDataSuccess: LiveData<Array<MainDataclass>> get() = _getDataSuccess

    private val _execSuccess = MutableLiveData<ExecReturn>()
    val execSuccess: LiveData<ExecReturn> get() = _execSuccess

    private val _getAlbumSuccess = MutableLiveData<Album>()
    val getAlbumSuccess: LiveData<Album> get() = _getAlbumSuccess


    fun getAlbums(){
        _isLoading.postValue(true)

        viewModelScope.launch {
            try {
                //val cmd = createCommand1("5132", "ROH")
                val service =RetrofitInstance.getRetrofit().create(AlbumService::class.java)
                val response = service.getAlbums()
                if (response.isSuccessful){
                    Log.d(TAG, "getAlbums: ${response.body()}")
                    _getAlbumSuccess.postValue(response.body())
                }else{
                    _toastMessage.postValue(response.errorBody()?.string())
                    //Log.d(TAG, "getDataSet: ")
                }
            }catch ( ex: Exception){
                _toastMessage.postValue(ex.message)
                Log.d(TAG, "getDataSet: ${ex.message}")
            }finally {
                _isLoading.postValue(false)
            }
        }
    }
    fun loginUser(email: String, password: String){
        _isLoading.postValue(true)

        viewModelScope.launch {
            try {
                val loginRequest = LoginRequest(
                    password = password,
                    email = email
                )
                val response = RetrofitObject.api.authLogin(loginRequest)
                if (response.isSuccessful){
                    _loginSuccess.postValue(response.body())
                }else{
                    _toastMessage.postValue(response.errorBody()?.string())
                    //Log.d(TAG, "loginUser: ")
                }
            }catch ( ex: Exception){
                _toastMessage.postValue(ex.message)
                //Log.d(TAG, "loginUser: ${ex.message}")
            }finally {
                _isLoading.postValue(false)
            }
        }
    }
    fun getDataSet(){
        _isLoading.postValue(true)

        viewModelScope.launch {
            try {
                val cmd = createCommand1("5132", "ROH")
                val response = RetrofitObject.api.getDataSet(cmd)
                if (response.isSuccessful){
                    val data = response.body()?.getAsJsonArray("Table")
                    val resultData = Gson().fromJson(data, Array<MainDataclass>::class.java)
                    // No need to add TypeAdapter
                    //Log.d(TAG, "getDataSet: $data")
                    //Log.d(TAG, "getDataSet: $apiData")
                    _getDataSuccess.postValue(resultData)
                }else{
                    _toastMessage.postValue(response.errorBody()?.string())
                    //Log.d(TAG, "getDataSet: ")
                }
            }catch ( ex: Exception){
                _toastMessage.postValue(ex.message)
                Log.d(TAG, "getDataSet: ${ex.message}")
            }finally {
                _isLoading.postValue(false)
            }
        }
    }
    fun saveData(boxBarcode: String, barcode: String){
        _isLoading.postValue(true)

        viewModelScope.launch {
            try {
                val apiRequest = createExecCommands(
                    boxBarcode,
                    barcode
                )
                val response = RetrofitObject.api.execNonQuery(apiRequest)
                if (response.isSuccessful && response.code() == 200 ){
                    //Log.d(TAG, "saveData: $resBody")
                    _execSuccess.postValue(response.body())

                }
                else{
                    Log.d(TAG, "saveData:errorBody is stream ")
                    _toastMessage.postValue(response.errorBody()?.string())
                }
            }catch ( ex: Exception){
                _toastMessage.postValue(ex.message)
                //Log.d(TAG, "saveData: ${ex.message}")
            }finally {
                _isLoading.postValue(false)
            }
        }
    }
    private fun createCommand1(werks: String, mtart: String): MyDbCommand{
        val myCmd1 = MyDbCommand(
            commandName = "MST",
            connectionName = "Phi_PDA",
            commandType = CommandType.StoredProcedure.type,
            commandText = "ERPPM..USP_PDA_ZBBS_SEL",
//            parameters = mutableListOf( para1,para2, para3),
//            paraValues = mutableListOf(paraValue)
        )
        val para11 = MyPara(parameterName = "@WERKS",
            dbDataType = MsSqlDataType.Varchar.type,
            direction = ParameterDirection.Input.type)
        val para12 = MyPara(parameterName = "@MTART",
            dbDataType = MsSqlDataType.Varchar.type,
            direction = ParameterDirection.Input.type)
        myCmd1.parameters = mutableListOf( para11, para12)

        val paraValue = hashMapOf(
            "@WERKS" to werks,
            "@MTART" to mtart
        )
        myCmd1.paraValues = mutableListOf(paraValue)
        return myCmd1
    }
    private fun createCommand2(year: String, plant: String, process:String): MyDbCommand{
        val myCmd1 = MyDbCommand(
            commandName = "MST",
            connectionName = "HUIZHOU",
            commandType = CommandType.StoredProcedure.type,
            commandText = "ZBBS2..[USP_BCOST_0010_T2_SEL]",
//            parameters = mutableListOf( para1,para2, para3),
//            paraValues = mutableListOf(paraValue)
        )
        val para11 = MyPara(parameterName = "@YYMM",
            dbDataType = MsSqlDataType.Varchar.type,
            direction = ParameterDirection.Input.type)
        val para12 = MyPara(parameterName = "@PLANT",
            dbDataType = MsSqlDataType.Varchar.type,
            direction = ParameterDirection.Input.type)
        val para13 = MyPara(parameterName = "@PROC",
            dbDataType = MsSqlDataType.Varchar.type,
            direction = ParameterDirection.Input.type)
        myCmd1.parameters = mutableListOf( para11, para12, para13)

        val paraValue = hashMapOf(
            "@YYMM" to year,
            "@PLANT" to plant,
            "@PROC" to process
        )
        myCmd1.paraValues = mutableListOf(paraValue)
        return myCmd1
    }
    private fun createExecCommands(boxBarcode: String, barcode: String): List<MyDbCommand>{
        val myCmd1 = MyDbCommand(
            commandName = "MST",
            connectionName = "Phi_PDA",
            commandType = CommandType.StoredProcedure.type,
            commandText = "ERPPM..USP_PDA_IPGO_BOX",
//            parameters = mutableListOf( para1,para2, para3),
//            paraValues = mutableListOf(paraValue)
        )
        val para11 = MyPara(parameterName = "@BOXNO",
            dbDataType = MsSqlDataType.Varchar.type,
            direction = ParameterDirection.Input.type)
        val para12 = MyPara(parameterName = "@BARCD",
            dbDataType = MsSqlDataType.Varchar.type,
            direction = ParameterDirection.Input.type)
        val para13 = MyPara(parameterName = "@USER",
            dbDataType = MsSqlDataType.Varchar.type,
            direction = ParameterDirection.Input.type)
        myCmd1.parameters = mutableListOf( para11, para12, para13)

        val paraValue = hashMapOf(
            "@BOXNO" to boxBarcode,
            "@BARCD" to barcode,
            "@USER" to "1111"
        )
        myCmd1.paraValues = mutableListOf(paraValue)
        return mutableListOf( myCmd1)
    }

}