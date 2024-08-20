package com.myapp.testrfid.model

import com.google.gson.annotations.SerializedName

data class TaskId(
    @SerializedName("TASK_ID") val taskId: Long,
)
