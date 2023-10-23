package com.example.kerklyv5.pasarelaPagos

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.kerklyv5.interfaces.PagoContrajeta
import com.example.kerklyv5.modelo.serial.PaymentEvent
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class notificarServer(context: Context) {
    val context = context
    fun notificarPagoExitosoAlWebhook() {
        val ROOT_URL = "https://adminkerkly.com/"
        val gson = GsonBuilder().setLenient().create()
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client: OkHttpClient = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl("$ROOT_URL")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val webhookAPI = retrofit.create(PagoContrajeta::class.java)
        val paymentEvent = PaymentEvent("payment_intent.succeeded")

        // Crea un objeto JSON con los tres parámetros
        val requestBodyJson = JSONObject()
      /*  requestBodyJson.put("parametro1", parametro1)
        requestBodyJson.put("parametro2", parametro2)
        requestBodyJson.put("parametro3", parametro3)*/

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            requestBodyJson.toString()
        )

        // Enviar el evento al webhook con el cuerpo de la solicitud
        val call: Call<Void> = webhookAPI.sendPaymentEvent(paymentEvent, requestBody)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // Procesar la respuesta si es necesario
                showMensaje("Pago Realizado")
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Manejar errores de la solicitud
                showMensaje("Error al procesar pagos")
            }
        })
    }


    private fun  showMensaje(mensaje: String){
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        }
    }


    fun sendWebhookRequest() {
        val url = "https://f251-201-148-25-74.ngrok-free.app/service/public/webhook.php" // Reemplaza con tu URL de punto de conexión de webhook
        val json = "{\"event\": \"payment_intent.succeeded\"}" // El evento que deseas simular

        runBlocking {
            launch(Dispatchers.IO) {

                val client = OkHttpClient()
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val body = RequestBody.create(mediaType, json)

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    // La solicitud fue exitosa
                    showMensaje("Solicitud Exitosa")
                } else {
                    // La solicitud falló, maneja el error
                    showMensaje("La solicitud falló")
                }
            }
        }
    }
}