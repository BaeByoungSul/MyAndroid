package com.myapp.myframedagger.model

import com.google.gson.annotations.SerializedName

data class MainDataclass(
    @SerializedName("MATNR") val matnr: String,
    @SerializedName("MAKTX") val maktx: String,
    @SerializedName("MTART") val mtart: String,
    @SerializedName("DISPO") val dispo: String
)

