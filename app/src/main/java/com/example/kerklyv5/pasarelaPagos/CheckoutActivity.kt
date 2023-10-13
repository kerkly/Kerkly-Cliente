package com.example.kerklyv5.pasarelaPagos


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.example.kerklyv5.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.payments.paymentlauncher.PaymentLauncher
import com.stripe.android.payments.paymentlauncher.PaymentResult
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.model.PaymentMethod
import com.stripe.param.PaymentMethodCreateParams
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
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
   // private lateinit var correo: String
    private lateinit var nombre: String
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private lateinit var imagen: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        imagen = findViewById(R.id.imagen)
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser
        nombre = intent.getStringExtra("NombreCliente").toString()
      //  cargarImagen(currentUser!!.photoUrl.toString())

        PaymentConfiguration.init(this, Publishablekey)
        paymentSheet = PaymentSheet(this) { paymentSheetResult: PaymentSheetResult? ->
            onPaymentSheetResult(paymentSheetResult!!)
        }
        RealizarPago()
        val email = currentUser!!.email.toString()
        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        editTextEmail.setText(email)
        editTextName.setText(nombre)

        val pagar = findViewById<Button>(R.id.pay_button)
        pagar.setOnClickListener {
            val name = editTextName.text.toString()
            val email = editTextEmail.text.toString()

            // Validar que se hayan ingresado el nombre y el correo electrónico
            if (name.isNotEmpty() && email.isNotEmpty()) {
                if (ClientSecret != null) {
                    val configuration = PaymentSheet.Configuration(
                        "Codes Easy",
                        PaymentSheet.CustomerConfiguration(CustomerId, EphemeralKey)
                    )
                    paymentSheet.presentWithPaymentIntent(ClientSecret, configuration)
                } else {
                    showToast("Cargando...")
                }
            } else {
                // Muestra un mensaje de error si el nombre o el correo electrónico están vacíos
                showToast("Por favor, completa el nombre y el correo electrónico.")
            }
        }

        val oxxo = findViewById<Button>(R.id.button_oxxo)
        oxxo.setOnClickListener {
            val name = editTextName.text.toString()
            val email = editTextEmail.text.toString()
            // Validar que se hayan ingresado el nombre y el correo electrónico
            if (name.isNotEmpty() && email.isNotEmpty()) {
                val intent = Intent(this, MainActivityPagoEnOxxo::class.java)
                // Agrega datos extras al Intent
                intent.putExtra("ClientSecret", ClientSecret)
                intent.putExtra("name" , name)
                intent.putExtra("email" , email)
                intent.putExtra("secretKey", secretKey)
                //intent.putExtra("parametro2", valor2)
                startActivity(intent)
            } else {
                // Muestra un mensaje de error si el nombre o el correo electrónico están vacíos
                showToast("Por favor, completa el nombre y el correo electrónico.")
            }
        }
    }

    private fun RealizarPago(){
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
        val request = @SuppressLint("SuspiciousIndentation")
        object : StringRequest(
            Request.Method.POST,
            "https://api.stripe.com/v1/ephemeral_keys",
            Response.Listener<String> { response ->
                try {
                    val objeto = JSONObject(response)
                    EphemeralKey = objeto.getString("id")
                        obtenerCliente(CustomerId)
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

    private fun obtenerCliente(customerId: String) {
        val request = object : StringRequest(
            Request.Method.POST,
            "https://api.stripe.com/v1/payment_intents",
            Response.Listener<String> { response ->
                try {
                    val objeto = JSONObject(response)
                    ClientSecret = objeto.getString("client_secret")
                    println("cliente: $ClientSecret")
                    //showToast("cliente: $ClientSecret")
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
                  //  showToast("Error 177 $statusCode: $errorMessage")
                } else {
                    // Si no hay información detallada disponible, imprimir un mensaje genérico
                    println("Error desconocido 181")
                    showToast("Error desconocido 182")
                }//
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
                miMapa["customer"] = customerId
                miMapa["amount"] = "$monto"
                miMapa["currency"] = TipoMoneda
               miMapa["automatic_payment_methods[enabled]"] = "true"
                //miMapa["payment_method_types[]"] = "card"
               // miMapa["payment_method_types[]"] = "card"
               // miMapa[ "payment_method_types[]"]="oxxo"
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

    private fun obtenerClienteOxxo(customerId: String) {
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
                miMapa["customer"] = customerId
                miMapa["amount"] = "$monto"
                miMapa["currency"] = TipoMoneda
                miMapa[ "payment_method_types[]"]="oxxo"
                //  miMapa["payment_method_types[]"] = "google_pay"
                return miMapa
            }
        }


        val queue = Volley.newRequestQueue(this)
        queue.add(request)

    }

    private fun cargarImagen(urlImagen: String) {
        val file: Uri
        file = Uri.parse(urlImagen)
        System.out.println("imagen aqui: "+ file)
        Picasso.get().load(urlImagen).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {

            }
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                val multi = MultiTransformation<Bitmap>(RoundedCornersTransformation(128, 0, RoundedCornersTransformation.CornerType.ALL))
                Glide.with(this@CheckoutActivity).load(file)
                    .apply(RequestOptions.bitmapTransform(multi))
                    .into(imagen)
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                System.out.println("Respuesta error 3 "+ e.toString())
                //Toast.makeText(this@SolicitarServicio, "si hay foto respuesta 3", Toast.LENGTH_SHORT).show()
            }

        })
    }

}