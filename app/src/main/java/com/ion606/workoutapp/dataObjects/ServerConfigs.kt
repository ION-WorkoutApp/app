package com.ion606.workoutapp.dataObjects

data class ServerConfigs(
    val debugging: Boolean = false,
    val allowSignups: Boolean = true,
    val requireEmailVerification: Boolean = true,
    val minPasswordReq: Boolean = true,
    val allowSocialLogins: Boolean = false,
    val loginLimit: Int = 5,
    val enableMonetization: Boolean = false
);
