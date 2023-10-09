package com.example.kerklyv5.pasarelaPagos


import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.kerklyv5.R
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import org.json.JSONException
import org.json.JSONObject


class CheckoutActivity : AppCompatActivity() {
    lateinit var paymentSheet: PaymentSheet
    lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    lateinit var paymentIntentClientSecret: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        // Hook up the pay button
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        // Crear un objeto JSON con los datos que deseas enviar
        val postData = JSONObject()
        postData.put("name", "Luis Alfredo") // Reemplaza con el nombre deseado
        postData.put("line1", "el carmen")    // Reemplaza con la dirección deseada
        postData.put("postal_code", "7471503417") // Reemplaza con el código postal deseado
        postData.put("city", "mexico")           // Reemplaza con la ciudad deseada
        postData.put("state", "ny")               // Reemplaza con el estado deseado
        postData.put("country", "mx")             // Reemplaza con el país deseado
        postData.put("amount", 1099)              // Reemplaza con el monto deseado
        postData.put("description", "android studio") // Reemplaza con la descripción deseada

        // Realizar una solicitud HTTP POST al servidor
        fetchApi(postData)
        val Button = findViewById<Button>(R.id.pay_button)
        Button.setOnClickListener {
            if (paymentIntentClientSecret != null){
                paymentSheet.presentWithPaymentIntent(
                    paymentIntentClientSecret,
                    PaymentSheet.Configuration("Codes Easy", customerConfig)
                )
            }else{
                showToast("cargando.....")
            }
        }


    }
    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                // El pago se completó con éxito, puedes mostrar un mensaje de confirmación o realizar acciones adicionales.
                showToast("Payment complete!")
            }
            is PaymentSheetResult.Canceled -> {
                // El usuario ha cancelado el proceso de pago, puedes mostrar un mensaje de cancelación o realizar acciones adicionales según tus necesidades.
                showToast("Payment canceled!")
            }
            is PaymentSheetResult.Failed -> {
                // El pago falló, puedes mostrar un mensaje de error o realizar acciones adicionales según el motivo del fallo.
                val error = paymentSheetResult.error
                showToast("Payment failed " + error.localizedMessage)
            }
        }
    }

    private fun showToast(mensaje:String){
        Toast.makeText(this,mensaje,Toast.LENGTH_LONG).show()
    }

    fun fetchApi(postData: JSONObject) {
        val baseUrl = "https://demo.codeseasy.com/apis/stripe/"


        // Crear una solicitud POST
        val stringRequest = object : StringRequest(
            Request.Method.POST, baseUrl,
            { response ->
                try {
                    // Procesar la respuesta del servidor
                    val jsonObject = JSONObject(response)
                    val customer = jsonObject.getString("customer")
                    val ephemeralKey = jsonObject.getString("ephemeralKey")
                    val paymentIntentClientSecret = jsonObject.getString("paymentIntent")
                    val publishableKey = jsonObject.getString("publishablekey")

                    // Configurar PaymentSheet y PaymentConfiguration
                    customerConfig = PaymentSheet.CustomerConfiguration(customer, ephemeralKey)
                    PaymentConfiguration.init(applicationContext, publishableKey)

                    // Presentar PaymentSheet después de configurar los datos
                    val configuration = PaymentSheet.Configuration(
                        "Título de la transacción",
                        customerConfig
                    )
                    paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                // Manejar errores aquí
                showToast(error.stackTraceToString())
                println("error aqui ----- ${error.stackTraceToString()}")
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return postData.toString().toByteArray()
            }
        }

        // Agregar la solicitud a la cola de Volley
        val queue = Volley.newRequestQueue(this)
        queue.add(stringRequest)
    }

}