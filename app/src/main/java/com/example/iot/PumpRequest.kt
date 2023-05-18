package com.example.iot


import com.google.gson.annotations.SerializedName

data class PumpRequest(
    @SerializedName("hum")
    var hum: Int? = 0,
    @SerializedName("message")
    var message: String? = "",
    @SerializedName("temp")
    var temp: Int? = 0
)