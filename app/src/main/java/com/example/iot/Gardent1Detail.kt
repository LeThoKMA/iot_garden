package com.example.iot


import com.google.gson.annotations.SerializedName

data class Gardent1Detail(
    @SerializedName("hum1")
    var hum2: Any?="",
    @SerializedName("lamp")
    var lamp: Any?="",
    @SerializedName("pump1")
    var pump2: Any?="",
    @SerializedName("temp")
    var temp: Any?="",
    @SerializedName("flux1")
    var flux:Any?=""

):java.io.Serializable