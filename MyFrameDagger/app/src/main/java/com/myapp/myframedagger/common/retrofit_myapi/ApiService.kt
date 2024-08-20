package com.myapp.myframedagger.common.retrofit_myapi

import com.google.gson.JsonObject
import com.myapp.myframedagger.common.model.ExecReturn
import com.myapp.myframedagger.common.model.LoginRequest
import com.myapp.myframedagger.common.model.LoginResponse
import com.myapp.myframedagger.common.model.MyDbCommand

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/Auth/Login")
    suspend fun authLogin (@Body loginRequest: LoginRequest) : Response<LoginResponse>

    @POST("api/Db/ExecNonQuery")
    suspend fun execNonQuery(@Body body: List<MyDbCommand>): Response<ExecReturn>

    @POST("api/Db/GetDataSet")
    suspend fun getDataSet(@Body body: MyDbCommand): Response<JsonObject>
}