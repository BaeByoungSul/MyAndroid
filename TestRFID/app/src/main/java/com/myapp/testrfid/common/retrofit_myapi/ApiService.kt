package com.myapp.testrfid.common.retrofit_myapi

import com.google.gson.JsonObject
import com.myapp.testrfid.common.model.ExecReturn
import com.myapp.testrfid.common.model.LoginRequest
import com.myapp.testrfid.common.model.LoginResponse
import com.myapp.testrfid.common.model.MyDbCommand
import io.reactivex.rxjava3.core.Observable

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

    @POST("api/Db/GetDataSet")
    fun getDataSetRx(@Body body: MyDbCommand): Observable<Response<JsonObject>>

    @POST("api/Db/ExecNonQuery")
    fun execNonQueryRx(@Body body: List<MyDbCommand>): Observable<Response<ExecReturn>>

}