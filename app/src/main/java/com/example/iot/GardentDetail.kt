package com.example.iot


import com.google.gson.annotations.SerializedName

data class GardentDetail(
    @SerializedName("hum2")
    var hum2: Any?="",
    @SerializedName("lamp")
    var lamp: Any?="",
    @SerializedName("pump2")
    var pump2: Any?="",
    @SerializedName("temp")
    var temp: Any?="",
    @SerializedName("flux2")
    var flux:Any?=""
):java.io.Serializable