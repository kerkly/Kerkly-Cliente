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
    @POST("/IncrementarIntentos.php")
    fun IncrementarNumIntentos(
        @Field("telefono_NoR") telefono_NoR: String,
        @Field("numIntentos") numIntentos: Int,
        callback: Callback<Response?>
    )
}