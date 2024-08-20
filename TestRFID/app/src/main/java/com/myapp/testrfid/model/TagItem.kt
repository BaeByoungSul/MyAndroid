package com.myapp.testrfid.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class  TagItem(
    val tagHexValue: String,
    val tagType: String,
    val tagValue: String,
    var rssiValue: String,
    var dupCount: Int
) :Parcelable