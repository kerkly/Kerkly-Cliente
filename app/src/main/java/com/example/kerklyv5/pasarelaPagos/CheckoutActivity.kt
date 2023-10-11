package com.example.kerklyv5.pasarelaPagos


import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.kerklyv5.R
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.payments.paymentlauncher.PaymentLauncher
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.model.PaymentMethod
import com.stripe.param.PaymentMethodCreateParams
import org.json.JSONException
import org.json.JSONObject


class CheckoutActivity : AppCompatActivity() {
    lateinit var paymentSheet: PaymentSheet
    //lateinit var customerConfig: PaymentSheet.CustomerConfiguration
  //  lateinit var paymentIntentClientSecret: String
    val Publishablekey = "pk_test_51NxujVD0vuzqQPKcVE6nxzXd9rQGXe1ZaEMjdQpsqiKFL0BcKGaq5WWGKpruBc5aVPnc6mDEk3ck1kEj7zYtOywW00YMW0Kh1n"
    val secretKey = "sk_test_51NxujVD0vuzqQPKcGUiZy6L8iaV08WtZ1GdUxodfKCemXYX0bJGC2JFICyOhI1zA4CCDfcXAHuMt0AIcKzmEMPCf00lKUMivr7"
    var CustomerId: String = ""
    var EphemeralKey:String = ""
    var ClientSecret:String = ""
    val monto = 1000 //centavos equivalentes a 10 pesos mexicanos
    var TipoMoneda = "mxn"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        PaymentConfiguration.init(this, Publishablekey)

        paymentSheet = PaymentSheet(this) { paymentSheetResult: PaymentSheetResult? ->
            onPaymentSheetResult(paymentSheetResult!!)
        }

        val Button = findViewById<Button>(R.id.pay_button)
        Button.setOnClickListener {
            if (ClientSecret != null) {
                val configuration = PaymentSheet.Configuration(
                    "Codes Easy",
                    PaymentSheet.CustomerConfiguration(CustomerId, EphemeralKey)
                )

                paymentSheet.presentWithPaymentIntent(ClientSecret, configuration)
            } else {
                showToast("Cargando...")
            }

        }

        val request = object : StringRequest(Request.Method.POST,
            "https://api.stripe.com/v1/customers",
            Response.Listener<String> { response ->
                try {
                    val objeto = JSONObject(response)
                    CustomerId = objeto.getString("id")
                    obtenerKeyy()
                    println("ID: $CustomerId")
                    showToast("ID: $CustomerId")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                if (error.networkResponse != null) {
                    // Obtener el código de estado HTTP y el mensaje de error si están disponibles
                    val statusCode = error.networkResponse.statusCode
                    val errorMessage = String(error.networkResponse.data)
                    println("Error $statusCode: $errorMessage")
                    showToast("Error $statusCode: $errorMessage")
                } else {
                    // Si no hay información detallada disponible, imprimir un mensaje genérico
                    println("Error desconocido")
                    showToast("Error desconocido")
                }
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                // Aquí puedes configurar encabezados personalizados si es necesario
                val miMapa: MutableMap<String, String> = mutableMapOf()
                // Agregar elementos al mapa
              //  miMapa["Autorization"] = "Bearer  $secretKey"
                miMapa["Authorization"] = "Bearer $secretKey"

                // Agregar más pares clave-valor según tus necesidades

                return miMapa
            }
        }


        val queue = Volley.newRequestQueue(this)
        queue.add(request)


    }



    private fun obtenerKeyy() {
        val request = object : StringRequest(
            Request.Method.POST,
            "https://api.stripe.com/v1/ephemeral_keys",
            Response.Listener<String> { response ->
                try {
                    val objeto = JSONObject(response)
                    EphemeralKey = objeto.getString("id")
                    obtenerCliente(CustomerId, EphemeralKey)
                    println("ID: $EphemeralKey")
                    //showToast("ID: $CustomerId")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                if (error.networkResponse != null) {
                    // Obtener el código de estado HTTP y el mensaje de error si están disponibles
                    val statusCode = error.networkResponse.statusCode
                    val errorMessage = String(error.networkResponse.data)
                    println("Error $statusCode: $errorMessage")
                    showToast("Error $statusCode: $errorMessage")
                } else {
                    // Si no hay información detallada disponible, imprimir un mensaje genérico
                    println("Error desconocido")
                    showToast("Error desconocido")
                }
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                // Aquí puedes configurar encabezados personalizados si es necesario
                val miMapa: MutableMap<String, String> = mutableMapOf()
                // Agregar elementos al mapa
                miMapa["Authorization"] = "Bearer  $secretKey"
                miMapa["Stripe-Version"] = "2023-08-16"
                // Agregar más pares clave-valor según tus necesidades

                return miMapa
            }

            override fun getParams(): MutableMap<String, String>? {
                val miMapa: MutableMap<String, String> = mutableMapOf()
                miMapa["customer"] = CustomerId
                //miMapa["Stripe-Version"] = "2023-08-16"
                return miMapa
            }
        }


        val queue = Volley.newRequestQueue(this)
        queue.add(request)

    }

    private fun obtenerCliente(customerId: String, ephemeralKey: String) {
        val request = object : StringRequest(
            Request.Method.POST,
            "https://api.stripe.com/v1/payment_intents",
            Response.Listener<String> { response ->
                try {
                    val objeto = JSONObject(response)
                    ClientSecret = objeto.getString("client_secret")
                    println("cliente: $ClientSecret")
                    showToast("cliente: $ClientSecret")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                if (error.networkResponse != null) {
                    // Obtener el código de estado HTTP y el mensaje de error si están disponibles
                    val statusCode = error.networkResponse.statusCode
                    val errorMessage = String(error.networkResponse.data)
                    println("Error 177 $statusCode: $errorMessage")
                    showToast("Error 177 $statusCode: $errorMessage")
                } else {
                    // Si no hay información detallada disponible, imprimir un mensaje genérico
                    println("Error desconocido 181")
                    showToast("Error desconocido 182")
                }
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                // Aquí puedes configurar encabezados personalizados si es necesario
                val miMapa: MutableMap<String, String> = mutableMapOf()
                // Agregar elementos al mapa
                miMapa["Authorization"] = "Bearer  $secretKey"
                return miMapa
            }

            override fun getParams(): MutableMap<String, String>? {
                val miMapa: MutableMap<String, String> = mutableMapOf()
                miMapa["customer"] = CustomerId
                miMapa["amount"] = "$monto"
                miMapa["currency"] = TipoMoneda
              // miMapa["automatic_payment_methods[enabled]"] = "true"
                //miMapa["payment_method_types[]"] = "card"
                miMapa["payment_method_types[]"] = "card"
                miMapa[ "payment_method_types[]"]="oxxo"
              //  miMapa["payment_method_types[]"] = "google_pay"
                return miMapa
            }
        }


        val queue = Volley.newRequestQueue(this)
        queue.add(request)

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

            else -> {
                print("nadad --->")
            }
        }
    }

    private fun showToast(mensaje:String){
        Toast.makeText(this,mensaje,Toast.LENGTH_LONG).show()
    }

 /*   fun fetchApi(postData: JSONObject) {
        val baseUrl = "https://api.stripe.com/v1/customers/"


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
    }*/

}