package com.example.kerklyv5.interfaces

import retrofit.Callback
import retrofit.client.Response
import retrofit.http.Field
import retrofit.http.FormUrlEncoded
import retrofit.http.POST

interface CerrarSesionInterface {

    @FormUrlEncoded
    @POST("/cerrarSesion.php")
    fun cerrar(
        @Field("correo") correo: String,
        callback: Callback<Response?>?
    )
}