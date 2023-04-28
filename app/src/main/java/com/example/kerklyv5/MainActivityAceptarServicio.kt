package com.example.kerklyv5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.kerklyv5.interfaces.AceptarPresupuestoInterface
import com.example.kerklyv5.ui.home.HomeFragment
import com.example.kerklyv5.url.Url
import com.example.kerklyv5.vista.fragmentos.OrdenesPendientesFragment
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
    var b: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_aceptar_servicio)

        botonAceptar = findViewById(R.id.buttonAceptarPresupuesto)
        botonCancelar = findViewById(R.id.buttonCancelarPresupuesto)
        b = intent.extras!!
       // telefono = b!!.getString("Telefono")!!
        folio = b!!.getInt("IdContrato")

        botonAceptar.setOnClickListener {
            Toast.makeText(this, "clik", Toast.LENGTH_SHORT).show()
            Aceptar(folio)
        }

        botonCancelar.setOnClickListener {
            Toast.makeText(this, "clik", Toast.LENGTH_SHORT).show()
        }
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
                }

                override fun failure(error: RetrofitError?) {
                    Toast.makeText(applicationContext, "error $error", Toast.LENGTH_SHORT).show()
                }

            })

    }
}