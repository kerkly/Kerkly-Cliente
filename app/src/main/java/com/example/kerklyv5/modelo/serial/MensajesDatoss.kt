package com.example.kerklyv5.modelo.serial

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MensajesDatoss {
    @SerializedName("idPresupuestoNoRegistrado")
    @Expose
    var idPresupuestoNoRegistrado: Int = 0

    @SerializedName("fechaPresupuesto")
    @Expose
    var fechaPresupuesto: String = ""

    @SerializedName("problema")
    @Expose
    var problema: String = ""

    @SerializedName("PagoTotal")
    @Expose
    var PagoTotal: Double = 0.0

    @SerializedName("nombreO")
    @Expose
    var nombreO = ""

    @SerializedName("nombre_noR")
    @Expose
    var nombre_noR = ""

    @SerializedName("apellidoP_noR")
    @Expose
    var apellidoP_noR = ""

    @SerializedName("apellidoM_noR")
    @Expose
    var apellidoM_noR = ""

    @SerializedName("Telefono")
    @Expose
    var Telefono = ""

    @SerializedName("Nombre")
    @Expose
    var Nombre = ""

    @SerializedName("Apellido_Paterno")
    @Expose
    var Apellido_Paterno = ""

    @SerializedName("Apellido_Materno")
    @Expose
    var Apellido_Materno = ""

    @SerializedName("correo_electronico")
    @Expose
    var correo_electronico = ""

    @SerializedName("Pais")
    @Expose
    var Pais = ""

    @SerializedName("Ciudad")
    @Expose
    var Ciudad = ""

    @SerializedName("Colonia")
    @Expose
    var Colonia = ""

    @SerializedName("Calle")
    @Expose
    var Calle = ""


}