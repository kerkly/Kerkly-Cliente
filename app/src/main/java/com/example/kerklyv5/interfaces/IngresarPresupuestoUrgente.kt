package com.example.kerklyv5.interfaces

import retrofit.Callback
import retrofit.client.Response
import retrofit.http.Field
import retrofit.http.FormUrlEncoded
import retrofit.http.POST

interface IngresarPresupuestoUrgente {
    @FormUrlEncoded
    @POST("/IngresarPresupuestoUrgente.php")
    fun presupuesto_urgente(
        @Field("problema") problema: String,
        @Field("telefonoCliente") telefonoCliente: String,
        @Field("nombreO") nombreO: String,
        @Field("latitud") latitud: Double,
        @Field("longitud") longitud: Double,
        @Field("Ciudad")  Ciudad: String,
        @Field("Estado") Estado: String,
        @Field("Pais") Pais: String,
        @Field("Calle") Calle: String,
        @Field("Colonia") Colonia: String,
        @Field("No_Exterior") No_Exterior: String,
        @Field("Codigo_Postal") Codigo_Postal: String,
       @Field("uidCliente") uidCliente: String,
        callback: Callback<Response?>
    )
}