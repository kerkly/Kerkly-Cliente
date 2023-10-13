package com.example.kerklyv5

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.kerklyv5.express.FormaPagoExrpess
import com.example.kerklyv5.interfaces.AceptarPresupuestoInterface
import com.example.kerklyv5.interfaces.AceptarPresupuestoNormalInterface
import com.example.kerklyv5.notificaciones.llamarTopico
import com.example.kerklyv5.notificaciones.obtenerKerklys_y_tokens
import com.example.kerklyv5.pasarelaPagos.CheckoutActivity
import com.example.kerklyv5.url.Url
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivityAceptarServicio : AppCompatActivity() {
    lateinit var botonAceptar: Button
    lateinit var botonCancelar: Button
    private  var folio:  Int =0
    private lateinit var telefono: String
    private lateinit var tipoServicio: String
    private lateinit var telefonoKerkly:String
    private lateinit var problema: String
    private lateinit var nombreCliente: String
    var b: Bundle? = null
    private lateinit var pagoTotal: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_aceptar_servicio)

        botonAceptar = findViewById(R.id.buttonAceptarPresupuesto)
        botonCancelar = findViewById(R.id.buttonCancelarPresupuesto)
        b = intent.extras!!
        telefono = b!!.getString("telefonoCliente")!!
        folio = b!!.getInt("IdContrato")
        tipoServicio = b!!.getString("tipoServicio").toString()
        telefonoKerkly = b!!.getString("telefonokerkly").toString()
        nombreCliente = b!!.getString("nombreCompletoCliente").toString()
        problema = b!!.getString("problema").toString()
        pagoTotal  =b!!.getString("pagoTotal").toString()



        botonAceptar.setOnClickListener {
            //Toast.makeText(this, "clik", Toast.LENGTH_SHORT).show()
            if (tipoServicio == "Registrado"){
                AceptarServicioNormal(folio)
                obtenerTokenKerkly(telefonoKerkly, problema,nombreCliente)
              //  val intent = Intent(this, SolicitarServicio::class.java)
                //intent.putExtra("Telefono",telefono)
                //startActivity(intent)

                val intent  = Intent(applicationContext, CheckoutActivity::class.java)
                b!!.putBoolean("Express", true)
                b!!.putString("nombreCliente", nombreCliente)
                b!!.putString("pagoTotal", pagoTotal)
                intent.putExtras(b!!)
                startActivity(intent)
            }

        }

        botonCancelar.setOnClickListener {
           // Toast.makeText(this, "clik", Toast.LENGTH_SHORT).show()
            finish()
           val intent = Intent(this, SolicitarServicio::class.java)
            intent.putExtra("Telefono",telefono)
            startActivity(intent)
        }
    }

    private fun obtenerTokenKerkly(telefonoKerkly: String, problema: String, nombreCliente: String) {
        val kerkly = obtenerKerklys_y_tokens()
       kerkly.obtenerTokenKerkly(telefonoKerkly, problema, nombreCliente, this)
    }

    private fun AceptarServicioNormal(folio: Int) {
        val ROOT_URL = Url().url
        val adaptar = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api: AceptarPresupuestoNormalInterface? = adaptar.create(AceptarPresupuestoNormalInterface ::class.java)
        api!!.Aceptar(
            folio.toString(),
            "1",
            object : Callback<Response?> {
                override fun success(t: Response?, response: Response?) {
                    var entrada: BufferedReader? =  null
                    var Respuesta = ""
                    try {
                        entrada = BufferedReader(InputStreamReader(t?.body?.`in`()))
                        Respuesta = entrada.readLine()
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                    Toast.makeText(applicationContext, Respuesta, Toast.LENGTH_SHORT).show()
                  //  val llamarTopico = llamarTopico()
                    ////llamarTopico.llamartopico(this@MainActivityAceptarServicio, token, )
                }

                override fun failure(error: RetrofitError?) {
                    Toast.makeText(applicationContext, "error $error", Toast.LENGTH_SHORT).show()
                }

            })

    }

    private fun Aceptar (folio: Int){
        val ROOT_URL = Url().url

        val adaptar = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api: AceptarPresupuestoInterface = adaptar.create(AceptarPresupuestoInterface ::class.java)
        api.Aceptar(folio.toString(),
            "1",
            object : Callback<Response?> {
                override fun success(t: Response?, response: Response?) {
                    var entrada: BufferedReader? =  null
                    var Respuesta = ""
                    try {
                        entrada = BufferedReader(InputStreamReader(t?.body?.`in`()))
                        Respuesta = entrada.readLine()
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                    Toast.makeText(applicationContext, Respuesta, Toast.LENGTH_SHORT).show()
                   // val llamarTopico = llamarTopico()
                     //   llamarTopico.llamartopico(this@MainActivityAceptarServicio, token, )
                }

                override fun failure(error: RetrofitError?) {
                    Toast.makeText(applicationContext, "error $error", Toast.LENGTH_SHORT).show()
                }

            })

    }
}