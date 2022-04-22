package com.example.kerklyv5.vista

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.R
import com.example.kerklyv5.SolicitarServicio
import com.example.kerklyv5.controlador.AdapterKerkly
import com.example.kerklyv5.distancia_tiempo.CalcularTiempoDistancia
import com.example.kerklyv5.interfaces.IngresarPresupuestoClienteInterface
import com.example.kerklyv5.interfaces.ObtenerKerklyInterface
import com.example.kerklyv5.modelo.serial.Kerkly
import com.example.kerklyv5.url.Url
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList

class KerklyListActivity : AppCompatActivity(), CalcularTiempoDistancia.Geo {

    private lateinit var oficio: String
    private lateinit var problema: String
    private lateinit var curp: String
    private lateinit var telefono: String
    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: AdapterKerkly
    private lateinit var b: Bundle
    private var latitud = 0.0
    private var longitud = 0.0
    var i2: Int? = 0
    var postlist: ArrayList<Kerkly>? =null
    private lateinit var context: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kerkly_list)

        b = intent.extras!!

        context = this

        latitud = b.getDouble("Latitud")
        longitud = b.getDouble("Longitud")

        recyclerview = findViewById(R.id.recycler_kerkly)
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager= LinearLayoutManager(applicationContext)

        telefono = b.get("Telefono").toString()
        oficio = b.getString("Oficio").toString()
        problema = b.get("Problema").toString()

        getOficios()
    }

    private fun getOficios () {
        val ROOT_URL = Url().url
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client: OkHttpClient = OkHttpClient.Builder().build()

        val retrofit = Retrofit.Builder()
            .baseUrl("$ROOT_URL/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val presupuestoGET = retrofit.create(ObtenerKerklyInterface::class.java)
        val call = presupuestoGET.kerklys(oficio)

        call?.enqueue(object : retrofit2.Callback<List<Kerkly?>?> {

            override fun onResponse(
                call: Call<List<Kerkly?>?>,
                response: retrofit2.Response<List<Kerkly?>?>
            ) {
                postlist = response.body()
                        as ArrayList<Kerkly>

                var size = 0

                if (postlist!!.size > 0) {
                    if (postlist!!.size >= 5) {
                        size = 5
                    } else {
                        size = postlist!!.size
                    }


                    for (i in 0 until size) {
                        var latitudFinal = postlist!![i].latitud
                        var longitudFinal = postlist!![i].longitud

                        val url2 = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$latitud,$longitud&destinations=$latitudFinal,$longitudFinal&mode=driving&language=fr-FR&avoid=tolls&key=AIzaSyAp-2jznuGLfRJ_en09y1sp6A-467zrXm0"
                        CalcularTiempoDistancia(context).execute(url2)
                    }
                }

                adapter = AdapterKerkly(postlist!!)

                adapter.setOnClickListener {
                    Log.d("curp", "Hora: " + postlist!![recyclerview.getChildAdapterPosition(it)].minutos)

                    curp = postlist!![recyclerview.getChildAdapterPosition(it)].Curp

                        intent = Intent(applicationContext, SolicitarServicio::class.java)
                        ingresarPresupuesto()

                    b.putBoolean("PresupuestoListo", true)
                    intent.putExtras(b)
                    startActivity(intent)

                }

            }

            override fun onFailure(call: Call<List<Kerkly?>?>, t: Throwable) {

                Log.d("error del retrofit", t.toString())
                Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun setDouble(min: String?) {
        val res = min!!.split(",").toTypedArray()
        val min = res[0].toDouble() / 60
        val dist = res[1].toInt() / 1000

        i2 = i2!! +1

        val e = i2!!-1


        postlist!![e].hora = (min / 60).toInt()
        postlist!![e].minutos = (min % 60).toInt()
        postlist!![e].horaMin = postlist!![e].hora + postlist!![e].minutos


        if (e == (postlist!!.size-1)) {
            postlist!!.sortBy {
                it.horaMin
            }
            //System.out.println("Kerkly $e ${postlist!![e].horaMin}")
            recorrerLista()
        }

    }

    fun recorrerLista (){
        for(i in 0 until postlist!!.size){
            System.out.println(postlist!![i].Nombre)
            System.out.println("hora " + postlist!!.get(i).hora + ":" + postlist!!.get(i).minutos)

        }

        recyclerview.adapter = adapter
    }

    private fun ingresarPresupuesto() {
        val ROOT_URL = Url().url
        val adapter = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api = adapter.create(IngresarPresupuestoClienteInterface::class.java)
        api.presupuesto(curp, problema, telefono, oficio, latitud, longitud,
            object : Callback<Response?> {
                override fun success(t: Response?, response: Response?) {
                    var salida: BufferedReader? = null
                    var entrada = ""
                    try {
                        salida = BufferedReader(InputStreamReader(t?.body?.`in`()))
                        entrada = salida.readLine()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    Toast.makeText(applicationContext, "Entre por aqui", Toast.LENGTH_LONG).show()

                    val cadena = "Datos enviados"
                    if (cadena.equals(entrada)){
                        Toast.makeText(applicationContext,"Datos enviados", Toast.LENGTH_LONG).show()
                    }
                }

                override fun failure(error: RetrofitError?) {
                    println("error$error")
                    Toast.makeText(applicationContext, "error $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}