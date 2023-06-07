package com.example.kerklyv5

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.controlador.AdapterMensajes
import com.example.kerklyv5.express.MensajesExpress
//import com.example.kerklyv5.express.MensajesExpress
import com.example.kerklyv5.modelo.serial.MensajesDatoss
import com.example.kerklyv5.interfaces.MensajesInterface
import com.example.kerklyv5.url.Url
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import com.google.gson.GsonBuilder

class MensajesActivity : AppCompatActivity() {
    lateinit var MiAdapter: AdapterMensajes
    lateinit var recyclerview: RecyclerView
    private lateinit var telefono: String
    private lateinit var b: Bundle
    private lateinit var img: ImageView
    private lateinit var txt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensajes)
        b = intent.extras!!
        telefono = b.get("Teléfono No Registrado").toString()

        img = findViewById(R.id.img_mensaes_express)
        txt = findViewById(R.id.txt_mensajes_express)



        recyclerview = findViewById(R.id.recycler_mensajes)
        recyclerview.setOnClickListener {

        }
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager= LinearLayoutManager(this)

        getJSON()

       // database.addValueEventListener(postListener)
    }

    val postListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // Get Post object and use the values to update the UI
            //val post = dataSnapshot.getValue<tablaP>()
            Toast.makeText(applicationContext, "alguien actualizo la tabla", Toast.LENGTH_LONG).show()

            // ...
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Post failed, log a message
            Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
        }
    }

    fun getJSON() {
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
        val presupuestoGET = retrofit.create(MensajesInterface::class.java)
        val call = presupuestoGET.EnviarT(telefono)
        call?.enqueue(object : Callback<List<MensajesDatoss?>?> {

            override fun onResponse(
                call: Call<List<MensajesDatoss?>?>,
                response: Response<List<MensajesDatoss?>?>
            ) {
                val postList: ArrayList<MensajesDatoss> = response.body()
                        as ArrayList<MensajesDatoss>

                if (postList.size == 0) {
                    recyclerview.visibility = View.GONE
                } else {
                    img.visibility = View.GONE
                    txt.visibility = View.GONE

                    MiAdapter = AdapterMensajes(postList)

                    MiAdapter.setOnClickListener {
                        val nombre = postList[recyclerview.getChildAdapterPosition(it)].nombre_noR
                        val ap = postList[recyclerview.getChildAdapterPosition(it)].apellidoP_noR
                        val am = postList[recyclerview.getChildAdapterPosition(it)].apellidoM_noR
                        val fecha = postList[recyclerview.getChildAdapterPosition(it)].fechaPresupuesto
                        val problema = postList[recyclerview.getChildAdapterPosition(it)].problema
                        val folio = postList[recyclerview.getChildAdapterPosition(it)].idPresupuestoNoRegistrado
                        val pago = postList[recyclerview.getChildAdapterPosition(it)].PagoTotal
                        val oficio = postList[recyclerview.getChildAdapterPosition(it)].nombreO
                        val telefonoKerkly = postList[recyclerview.getChildAdapterPosition(it)].Telefono
                        val nombreKerkly = postList[recyclerview.getChildAdapterPosition(it)].Nombre
                        val apeP =  postList[recyclerview.getChildAdapterPosition(it)].Apellido_Paterno
                        val apeM =  postList[recyclerview.getChildAdapterPosition(it)].Apellido_Materno
                        val pais =  postList[recyclerview.getChildAdapterPosition(it)].Pais
                        val ciudad =  postList[recyclerview.getChildAdapterPosition(it)].Ciudad
                        val Colonia =  postList[recyclerview.getChildAdapterPosition(it)].Colonia
                        val calle =  postList[recyclerview.getChildAdapterPosition(it)].Calle
                        val correoKerly =  postList[recyclerview.getChildAdapterPosition(it)].correo_electronico

                        val direccionKerly = "$pais $ciudad $Colonia $calle"

                        val nombreCompletoKerkly = "$nombreKerkly $apeP $apeM"

                        val nombreCompletoClienteNR = "$nombre $ap $am"

                        if (pago.equals("0")){
                            Toast.makeText(this@MensajesActivity, "Por favor espere, Seguimos buscando el kerkly mas cercano", Toast.LENGTH_SHORT).show()
                        }else {

                            val i = Intent(applicationContext, MensajesExpress::class.java)

                            b.putString("NombreClienteNR", nombreCompletoClienteNR)
                            b.putString("tipoServicio", "NoRegistrado")
                            b.putString("Telefono", telefono)
                            b.putString("Fecha", fecha)
                            b.putString("Problema", problema)
                            b.putInt("Folio", folio)
                            b.putDouble("Pago total", pago)
                            b.putString("Oficio", oficio)
                            b.putString("telefonoKerkly", telefonoKerkly)
                            b.putString("nombreCompletoKerkly", nombreCompletoKerkly)
                            b.putString("direccionKerly", direccionKerly)
                            b.putString("correoKerly", correoKerly)
                            i.putExtras(b)
                            startActivity(i)

                        }
                    }

                    recyclerview.adapter = MiAdapter
                }

            }

            override fun onFailure(call: Call<List<MensajesDatoss?>?>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Codigo de respuesta de error: $t",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d("error del retrofit", t.toString())
            }

        })
    }

}