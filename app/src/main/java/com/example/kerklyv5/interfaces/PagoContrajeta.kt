package com.example.kerklyv5.interfaces

import com.example.kerklyv5.modelo.serial.PaymentEvent
import okhttp3.RequestBody
import retrofit.http.POST
import retrofit2.Call
import retrofit2.http.Body


interface PagoContrajeta {
    @POST("webhook.php")
    fun sendPaymentEvent(@Body paymentEvent: PaymentEvent, requestBody: RequestBody): Call<Void>

}