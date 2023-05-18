package com.example.iot

import com.google.gson.annotations.SerializedName

data class Point(
    @SerializedName("x")
    var x:Float?,
    @SerializedName("y")
    var y:Float?
)
