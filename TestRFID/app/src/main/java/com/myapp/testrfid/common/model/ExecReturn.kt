package com.myapp.testrfid.common.model

import com.google.gson.annotations.SerializedName

data class ExecReturn(
    var scannedBarcode: String,

    @SerializedName("ReturnCode")
    val returnCode: String,
    @SerializedName("OutputList")
    val outputList: ArrayList<DBOutput>
){
    data class DBOutput(
        @SerializedName("Rowseq")
        var rowseq: Int = 0,
        @SerializedName("CommandName")
        var commandName: String = "",
        @SerializedName("ParameterName")
        var parameterName: String = "",
        @SerializedName("OutValue")
        var outValue: String = ""
    )
}
//sealed class MergedData
//data class PasswordData(val dbOutput: ExecReturn): MergedData()
//data class CategoryData(val scannedBarcode: String): MergedData()
//
//data class DBOutputResult(
//    val returnCode: String,
//    val outputList: ArrayList<DBOutput>,
//    val scannedBarcode: String
// ){
//        data class DBOutput(
//            var rowseq: Int = 0,
//            var commandName: String = "",
//            var parameterName: String = "",
//            var outValue: String = ""
//        )
//}
