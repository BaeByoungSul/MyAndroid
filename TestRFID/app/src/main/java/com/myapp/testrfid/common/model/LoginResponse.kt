package com.myapp.testrfid.common.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class LoginResponse(
    @SerializedName("AccessToken")
    val accessToken: String,
    @SerializedName("RefreshToken")
    val refreshToken: RefreshToken
)
data class RefreshToken(
    @SerializedName("Token")
    val token: String,
    @SerializedName("Created")
    val created: Date,
    @SerializedName("Expires")
    val expires: Date
)