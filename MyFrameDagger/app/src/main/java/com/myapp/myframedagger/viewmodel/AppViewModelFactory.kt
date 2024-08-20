package com.myapp.myframedagger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myapp.myframedagger.common.retrofit_myapi.ApiService

import javax.inject.Inject

/**
 * since we are using constructor injection here
 * and Dagger will provide the object here automatically
 * and we will pass it into the MainViewModel
 */
/* ViewModelProvider.Factory 가 필요한 이유
 * We can not create ViewModel on our own.
 * We need ViewModelProviders utility provided by Android to create ViewModels.
 * But ViewModelProviders can only instantiate ViewModels with no arg constructor
 */

class AppViewModelFactory
@Inject constructor(private val apiService: ApiService)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return  when(modelClass){
            RetrofitTestViewModel::class.java -> RetrofitTestViewModel(apiService)
            //MainViewModel::class.java -> MainViewModel(repository)
            //ScanTestViewModel::class.java -> ScanTestViewModel(repository)
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        } as T
        //return super.create(modelClass)
        //return LoginViewModel(repository) as T
    }
}