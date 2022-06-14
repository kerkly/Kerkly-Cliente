package com.example.kerklyv5.interfaces

import retrofit.Callback
import retrofit.client.Response
import retrofit.http.Field
import retrofit.http.FormUrlEncoded
import retrofit.http.POST

interface DeviceIDInterfaceBotonSinRegistro {

    @FormUrlEncoded
    @POST("/deviceID_validarExiste.php")
    fun mensaje (
        @Field("deviceID") deviceID: String,
        callback: Callback<Response?>?
    )
}