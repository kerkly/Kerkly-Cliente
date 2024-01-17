package com.example.kerklyv5.modelo.serial

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OrdenPendienteUrgente {

    @SerializedName("nombreO")
    @Expose
    var nombreO: String = ""


    @SerializedName("idPresupuesto")
    @Expose
    var idPresupuesto: String = ""


    @SerializedName("pago_total")
    @Expose
    var pago_total: Double = 0.0

    @SerializedName("problema")
    @Expose
    var problema: String = ""

    @SerializedName("fechaP")
    @Expose
    var fechaP: String =  ""

    @SerializedName("idKerklyAcepto")
    @Expose
    var idKerklyAcepto: String =  ""

}