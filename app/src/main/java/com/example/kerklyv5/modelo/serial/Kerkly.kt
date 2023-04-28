package com.example.kerklyv5.modelo.serial

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Kerkly {
    @SerializedName("Curp")
    @Expose
    var Curp: String = ""

    @SerializedName("Nombre")
    @Expose
    var Nombre: String = ""

    @SerializedName("Apellido_Paterno")
    @Expose
    var Apellido_Paterno: String = ""

    @SerializedName("Apellido_Materno")
    @Expose
    var Apellido_Materno: String = ""

    @SerializedName("Telefono")
    @Expose
    var Telefonok: String = ""

    @SerializedName("correo_electronico")
    @Expose
    var correo_electronico: String = ""

    @SerializedName("latitud")
    @Expose
    var latitud: Double = 0.0

    @SerializedName("longitud")
    @Expose
    var longitud: Double = 0.0

    var hora: Int = 0
    var minutos: Int = 0
    var horaMin: Int = 0
}