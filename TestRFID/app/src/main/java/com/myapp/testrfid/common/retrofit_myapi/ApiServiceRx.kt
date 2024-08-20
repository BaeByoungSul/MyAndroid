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

interface ApiServiceRx {
    @POST("api/Auth/Login")
    fun authLogin (@Body loginRequest: LoginRequest) : Observable<Response<LoginResponse>>

    @POST("api/Db/GetDataSet")
    fun getDataSet(@Body body: MyDbCommand): Observable<Response<JsonObject>>

    @POST("api/Db/ExecNonQuery")
    fun execNonQuery(@Body body: List<MyDbCommand>): Observable<Response<ExecReturn>>

}