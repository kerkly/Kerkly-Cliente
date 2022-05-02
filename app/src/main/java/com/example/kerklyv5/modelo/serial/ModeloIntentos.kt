package com.example.kerklyv5.modelo.serial

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ModeloIntentos {
    @SerializedName("nombre_noR")
    @Expose
    var nombre_noR: String = ""

    @SerializedName("numIntentos")
    @Expose
    var numIntentos: Int = 0
}