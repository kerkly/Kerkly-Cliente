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
import com.example.kerklyv5.controlador.AdapterKerkly
import com.example.kerklyv5.distancia_tiempo.CalcularTiempoDistancia
import com.example.kerklyv5.interfaces.ObtenerKerklyInterface
import com.example.kerklyv5.modelo.serial.Kerkly
import com.example.kerklyv5.url.Url
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList

class KerklyListActivity : AppCompatActivity(), CalcularTiempoDistancia.Geo {

    private lateinit var oficio: String
    private lateinit var problema: String
    private lateinit var telefono: String
    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: AdapterKerkly
    private lateinit var b: Bundle
    private var latitud = 0.0
    private var longitud = 0.0
    var i2: Int? =0
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
        oficio = b.getString("Oficio").toString()

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
                       /* Log.d("longFinal", "$longitudFinal")
                        Log.d("latitudFinal", "$latitudFinal")

                        Log.d("longInicial", "$longitud")
                        Log.d("latitudInicial", "$latitud")*/

                        val url2 = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$latitud,$longitud&destinations=$latitudFinal,$longitudFinal&mode=driving&language=fr-FR&avoid=tolls&key=AIzaSyAp-2jznuGLfRJ_en09y1sp6A-467zrXm0"
                        CalcularTiempoDistancia(context).execute(url2)
                    }

                   // Log.d("time", "${postlist!![0].hora}")
                }

                //recorrerLista()



                adapter = AdapterKerkly(postlist!!)

                adapter.setOnClickListener {
                    Log.d("curp", "Hora: " + postlist!![recyclerview.getChildAdapterPosition(it)].hora)

                   // b.putBoolean("Ker", true)

                   // var i = Intent(applicationContext, MapsActivity::class.java)
                    //b.putString("Nombre Kerkly", postList[recyclerview.getChildAdapterPosition((it))].Nombre)
                    //  b.putString("AP Kerkly", postList[recyclerview.getChildAdapterPosition((it))].Apellido_Paterno)
                    // b.putString("AM Kerkly", postList[recyclerview.getChildAdapterPosition((it))].Apellido_Materno)
                   // b.putString("Curp Kerkly", postList[recyclerview.getChildAdapterPosition((it))].Curp)

                 //   i.putExtras(b)
                //    startActivity(i)

                }

                recyclerview.adapter = adapter


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


      //  System.out.println("Hora")
        postlist!![e!!].hora = (min / 60).toInt()
      //  System.out.println("Kerkly $e ${postlist!![e!!].hora}")
       // System.out.println("Minutos")
        postlist!![e!!].minutos = (min % 60).toInt()
        //System.out.println("Kerkly $e ${postlist!![e!!].minutos}")

        /*postlist!!.sortBy {
            it.hora
        }*/

        System.out.println("Kerkly $e ${postlist!![e!!].hora}")
        System.out.println("Kerkly $e ${postlist!![e!!].minutos}")



    }

    fun recorrerLista (){
        for(i in 0 until postlist!!.size){
            System.out.println(postlist!![i].Curp)
            System.out.println("hora " + postlist!!.get(i).hora + ":" + postlist!!.get(i).minutos)

        }
    }
}