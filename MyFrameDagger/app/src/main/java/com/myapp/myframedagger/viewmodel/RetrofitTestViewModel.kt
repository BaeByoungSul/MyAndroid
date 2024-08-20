package com.myapp.myframedagger.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.myframedagger.common.model.LoginRequest
import com.myapp.myframedagger.common.model.LoginResponse
import com.myapp.myframedagger.common.retrofit_myapi.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class RetrofitTestViewModel (private val apiService: ApiService) :ViewModel() {
    companion object {
        private const val TAG = "RetrofitTestViewModel"
    }

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _toastMessage: MutableLiveData<String?> = MutableLiveData()
    val toastMessage: LiveData<String?> get() = _toastMessage

    private val _loginSuccess: MutableLiveData<LoginResponse> = MutableLiveData()
    val loginSuccess: LiveData<LoginResponse?> get() = _loginSuccess

    fun loginUser(email: String, password: String){
        _isLoading.postValue(true)

        viewModelScope.launch {
            try {
                val loginRequest = LoginRequest(
                    password = password,
                    email = email
                )
                val response = apiService.authLogin(loginRequest)
                if (response.isSuccessful){
                    _loginSuccess.postValue(response.body())
                    //Log.d(TAG, "loginUser: ${loginReturn?.accessToken}")

                }else{
                    _toastMessage.postValue(response.errorBody()?.string())
                }
            }catch ( ex: Exception){
                _toastMessage.postValue(ex.message)

            }finally {
                _isLoading.postValue(false)
            }
        }
    }
    fun loginUser3(email: String, password: String){
        _isLoading.postValue(true)
        CoroutineScope(IO).launch {
            CoroutineScope(Dispatchers.IO).launch {
                val loginRequest = LoginRequest(
                    password = password,
                    email = email
                )

                val response = apiService.authLogin(loginRequest)
                if (response.isSuccessful) {
                    launch ( Dispatchers.Main ){
                        Log.d(TAG, "loginUser3: ${response.body()}")
                    }
//                    launch(Dispatchers.Main) {
//                        Log.d(TAG, "onCreate: ${GsonBuilder().setPrettyPrinting().create().toJson(response.body())}")
//                        if (!response.body().isNullOrEmpty()) {
//                            //UI data manipulation code
//                            val recyclerAdapter = response.body()?.let { RecyclerAdapter(it) }
//                            binding.recyclerView.adapter = recyclerAdapter
//                        }
//                    }
                }
            }
        }
        viewModelScope.launch {
            try {
                val loginRequest = LoginRequest(
                    password = password,
                    email = email
                )
                val response = apiService.authLogin(loginRequest)
                if (response.isSuccessful){
                    _loginSuccess.postValue(response.body())
                    //Log.d(TAG, "loginUser: ${loginReturn?.accessToken}")

                }else{
                    _toastMessage.postValue(response.errorBody()?.string())
                }
            }catch ( ex: Exception){
                _toastMessage.postValue(ex.message)

            }finally {
                _isLoading.postValue(false)
            }
        }
    }
}