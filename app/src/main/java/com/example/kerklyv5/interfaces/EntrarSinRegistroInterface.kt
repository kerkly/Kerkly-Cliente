package com.example.kerklyv5.interfaces

import com.example.kerklyv5.modelo.serial.ModeloIntentos
import retrofit.http.Field
import retrofit.http.FormUrlEncoded
import retrofit.http.POST
import retrofit.Callback
import retrofit.client.Response
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface EntrarSinRegistroInterface {
    @FormUrlEncoded
    @POST("/pruebaSinRegistroNumero.php")
    fun sinRegistro(
        @Field("telefonoCliente") telefonoCliente: String,
        callback: Callback<Response?>
    )

    @FormUrlEncoded
    @GET("clienteNoRegistradoVerificarIntentos.php")
    open fun verificarClienteNoR(@Query("telefono_NoR") telefono_NoR: String):
            Call<List<ModeloIntentos?>?>?



    @FormUrlEncoded
    @POST("/InsertarNombreClienteNR.php")
    fun insertarNombreNR(
        @Field("telefono_NoR") telefono_NoR: String,
        @Field("nombre_noR") nombre_noR: String,
        @Field("apellidoP_noR") apellidoP_noR: String,
        @Field("apellidoM_noR") apellidoM_noR: String,
        callback: Callback<Response?>
    )
}