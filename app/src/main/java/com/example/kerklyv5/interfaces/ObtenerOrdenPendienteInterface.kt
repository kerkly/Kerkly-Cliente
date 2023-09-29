package com.example.kerklyv5.interfaces

import com.example.kerklyv5.modelo.serial.OrdenPendiente
import com.example.kerklyv5.modelo.serial.OrdenPendienteUrgente
import retrofit.http.FormUrlEncoded
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ObtenerOrdenPendienteInterface {

    @FormUrlEncoded
    @GET("ObtenerOrdenesPendientes.php")
    open fun ordenP(@Query("telefonoCliente") telefonoCliente: String):
            Call<List<OrdenPendiente?>?>?

    @FormUrlEncoded
    @GET("obtenerSolicitudUrgentes.php")
    open fun ordenUrgente(@Query("telefonoCliente") telefonoCliente: String):
            Call<List<OrdenPendienteUrgente?>?>?
}