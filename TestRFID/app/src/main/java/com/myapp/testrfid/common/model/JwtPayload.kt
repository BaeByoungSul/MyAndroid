package com.myapp.testrfid.common.model

data class JwtPayload (
    val name: String,
    val emailAddress: String,
    val mainRoles: List<String>,
    val role : List<String>,
    val exp : Long
)

