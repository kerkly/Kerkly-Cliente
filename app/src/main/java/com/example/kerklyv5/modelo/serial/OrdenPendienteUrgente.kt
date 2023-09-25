package com.example.kerklyv5.modelo.serial

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OrdenPendienteUrgente {

    @SerializedName("nombreO")
    @Expose
    var nombreO: String = ""

    @SerializedName("idPresupuesto")
    @Expose
    var idContrato: Int = 1

    @SerializedName("Fecha_Inicio")
    @Expose
    var Fecha_Inicio: String = ""

    @SerializedName("cliente_nombre")
    @Expose
    var Nombre: String = ""

    @SerializedName("cliente_ap")
    @Expose
    var Apellido_Paterno: String = ""

    @SerializedName("cliente_am")
    @Expose
    var Apellido_Materno: String = ""

    @SerializedName("Nombre")
    @Expose
    var NombreK: String = ""

    @SerializedName("Apellido_Paterno")
    @Expose
    var Apellido_PaternoK: String = ""

    @SerializedName("Apellido_Materno")
    @Expose
    var Apellido_MaternoK: String = ""

    @SerializedName("correo_electronico")
    @Expose
    var correo_electronico: String = ""

    @SerializedName("Telefono")
    @Expose
    var Telefono: String = ""

    @SerializedName("pago_total")
    @Expose
    var pago_total: Double = 0.0

    @SerializedName("Pais")
    @Expose
    var Pais: String =  ""

    @SerializedName("Ciudad")
    @Expose
    var Ciudad: String =  ""

    @SerializedName("Colonia")
    @Expose
    var Colonia: String =  ""

    @SerializedName("Calle")
    @Expose
    var Calle: String =  ""

    @SerializedName("problema")
    @Expose
    var problema: String =  ""

    @SerializedName("fechaP")
    @Expose
    var fechaP: String =  ""

    @SerializedName("aceptoCliente")
    @Expose
    var aceptoCliente: String =  ""

    @SerializedName("uidCliente")
    @Expose
    var uidCliente: String =  ""
}