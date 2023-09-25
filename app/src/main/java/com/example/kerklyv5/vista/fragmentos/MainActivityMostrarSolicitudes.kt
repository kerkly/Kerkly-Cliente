package com.example.kerklyv5.vista.fragmentos

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
import com.example.kerklyv5.R
import com.example.kerklyv5.controlador.AdapterOrdenPendiente
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.express.FormaPagoExrpess
import com.example.kerklyv5.express.MensajesExpress
import com.example.kerklyv5.interfaces.ObtenerOrdenPendienteInterface
import com.example.kerklyv5.modelo.serial.OrdenPendiente
import com.example.kerklyv5.url.Url
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivityMostrarSolicitudes : AppCompatActivity() {
    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: AdapterOrdenPendiente
    private lateinit var img: ImageView
    private lateinit var txt: TextView
    private lateinit var telefono: String
    private lateinit var nombreCompletoCliente: String

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var dabaseReference: DatabaseReference
    val setProgress= setProgressDialog()

    private lateinit var TipoSolicitud: String
    private lateinit var b: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_mostrar_solicitudes)

        recyclerview = findViewById(R.id.recycler_ordenesPendientes)
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager = LinearLayoutManager(this)
        img = findViewById(R.id.img_ordenesPendientes)
        txt = findViewById(R.id.txt_ordenesPendientes)
        firebaseDatabase = FirebaseDatabase.getInstance()


        b = intent.extras!!
        TipoSolicitud = b!!.getString("TipoDeSolicitud").toString()
        telefono = b!!.getString("Telefono").toString()
        nombreCompletoCliente =  b!!.getString("nombreCompletoCliente").toString()

        if (TipoSolicitud.equals("normal")){
            getOrdenesNormal()
        }

        if (TipoSolicitud.equals("urgente")){
            getOrdenesUrgente()
            setProgress.setProgressDialog(this)
            recyclerview.visibility = View.GONE
            setProgress.dialog.dismiss()
        }
    }

    private fun getOrdenesUrgente() {
        val ROOT_URL = Url().url
        val gson = GsonBuilder().setLenient().create()
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client: OkHttpClient = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl("$ROOT_URL/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val presupuestoGET = retrofit.create(ObtenerOrdenPendienteInterface::class.java)
        val call = presupuestoGET.ordenP(telefono)

        call?.enqueue(object : retrofit2.Callback<List<OrdenPendiente?>?> {

            override fun onResponse(
                call: Call<List<OrdenPendiente?>?>,
                response: retrofit2.Response<List<OrdenPendiente?>?>
            ) {
                val postList: ArrayList<OrdenPendiente> = response.body()
                        as ArrayList<OrdenPendiente>

                //Log.d("lista", postList.toString())
                // postList.clear()
                if (postList.size == 0) {
                    recyclerview.visibility = View.GONE
                    setProgress.dialog.dismiss()

                } else {
                    img.visibility = View.GONE
                    txt.visibility = View.GONE
                    adapter = AdapterOrdenPendiente(postList)
                    setProgress.dialog.dismiss()

                    adapter.setOnClickListener {

                        val nombre_kerkly = postList[recyclerview.getChildAdapterPosition(it)].NombreK.trim()
                        val ap_kerkly = postList[recyclerview.getChildAdapterPosition(it)].Apellido_PaternoK.trim()
                        val ap_kerkly_M = postList[recyclerview.getChildAdapterPosition(it)].Apellido_MaternoK.trim()
                        val id = postList[recyclerview.getChildAdapterPosition(it)].idContrato
                        val problema = postList[recyclerview.getChildAdapterPosition(it)].problema
                        val fecha = postList[recyclerview.getChildAdapterPosition(it)].fechaP
                        val aceptoCliente = postList[recyclerview.getChildAdapterPosition(it)].aceptoCliente

                        val telefonoKerkly = postList[recyclerview.getChildAdapterPosition(it)].Telefono
                        val correoKerkly = postList[recyclerview.getChildAdapterPosition(it)].correo_electronico

                        val pais = postList[recyclerview.getChildAdapterPosition(it)].Pais
                        val ciudad = postList[recyclerview.getChildAdapterPosition(it)].Ciudad
                        val calle = postList[recyclerview.getChildAdapterPosition(it)].Colonia
                        val colonia = postList[recyclerview.getChildAdapterPosition(it)].Calle

                        val nombre_completo_kerkly = "$nombre_kerkly $ap_kerkly $ap_kerkly_M"
                        val direccionKerkly = "$pais $ciudad $colonia $calle"
                        val nombreCliente = nombreCompletoCliente
                        var pagoTotal = postList[recyclerview.getChildAdapterPosition(it)].pago_total
                        var oficio = postList[recyclerview.getChildAdapterPosition(it)].nombreO


                        if (pagoTotal == 0.0){
                            Toast.makeText(this@MainActivityMostrarSolicitudes, "Por favor espere, le notificaremos en un momento", Toast.LENGTH_SHORT).show()
                            /* val intent = Intent(requireContext(), MainActivityAceptarServicio::class.java)
                               intent.putExtra("Nombre_Kerkly", nombre_kerkly)
                               intent.putExtra("Ap_Kerkly", ap_kerkly)
                               intent.putExtra("Nombre_completo_Kerkly", nomre_completo_kerkly)
                               intent.putExtra("IdContrato", id)
                               intent.putExtra("telefonoCliente", telefono)
                               intent.putExtra("nombreCompletoCliente",nombreCliente)
                               intent.putExtra("telefonokerkly", telefonoKerkly)
                               startActivity(intent)*/
                        }else{
                            //Toast.makeText(requireContext(), "ya hay presupuesto", Toast.LENGTH_SHORT).show()
                            if (aceptoCliente == "1"){
                                //Toast.makeText(requireContext(), "este presuepuesto ya sido aceptado", Toast.LENGTH_SHORT).show()
                                val intent  = Intent(this@MainActivityMostrarSolicitudes, FormaPagoExrpess::class.java)
                                b.putBoolean("Normal", true)
                                intent.putExtras(b)
                                startActivity(intent)
                            }else {
                                val i = Intent(this@MainActivityMostrarSolicitudes, MensajesExpress::class.java)
                                b.putString("NombreCliente", nombreCliente)
                                b.putString("tipoServicio", "Registrado")
                                b.putString("Telefono", telefono)
                                b.putString("Fecha", fecha)
                                b.putString("Problema", problema)
                                b.putInt("Folio", id)
                                b.putDouble("Pago total", pagoTotal)
                                b.putString("Oficio", oficio)
                                b.putString("telefonoKerkly", telefonoKerkly)
                                b.putString("nombreCompletoKerkly", nombre_completo_kerkly)
                                b.putString("direccionKerkly", direccionKerkly)
                                b.putString("correoKerkly", correoKerkly)
                                i.putExtras(b)
                                startActivity(i)
                            }
                        }
                    }
                    recyclerview.adapter = adapter
                }
            }
            override fun onFailure(call: Call<List<OrdenPendiente?>?>, t: Throwable) {
                setProgress.dialog.dismiss()
                Log.d("error del retrofit", t.toString())
                Toast.makeText(this@MainActivityMostrarSolicitudes, t.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun getOrdenesNormal () {
        setProgress.setProgressDialog(this)
        val ROOT_URL = Url().url
        val gson = GsonBuilder().setLenient().create()
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client: OkHttpClient = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl("$ROOT_URL/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val presupuestoGET = retrofit.create(ObtenerOrdenPendienteInterface::class.java)
        val call = presupuestoGET.ordenP(telefono)

        call?.enqueue(object : retrofit2.Callback<List<OrdenPendiente?>?> {

            override fun onResponse(
                call: Call<List<OrdenPendiente?>?>,
                response: retrofit2.Response<List<OrdenPendiente?>?>
            ) {
                val postList: ArrayList<OrdenPendiente> = response.body()
                        as ArrayList<OrdenPendiente>

                //Log.d("lista", postList.toString())
                // postList.clear()
                if (postList.size == 0) {
                    recyclerview.visibility = View.GONE
                    setProgress.dialog.dismiss()

                } else {
                    img.visibility = View.GONE
                    txt.visibility = View.GONE
                    adapter = AdapterOrdenPendiente(postList)
                    setProgress.dialog.dismiss()

                    adapter.setOnClickListener {

                        val nombre_kerkly = postList[recyclerview.getChildAdapterPosition(it)].NombreK.trim()
                        val ap_kerkly = postList[recyclerview.getChildAdapterPosition(it)].Apellido_PaternoK.trim()
                        val ap_kerkly_M = postList[recyclerview.getChildAdapterPosition(it)].Apellido_MaternoK.trim()
                        val id = postList[recyclerview.getChildAdapterPosition(it)].idContrato
                        val problema = postList[recyclerview.getChildAdapterPosition(it)].problema
                        val fecha = postList[recyclerview.getChildAdapterPosition(it)].fechaP
                        val aceptoCliente = postList[recyclerview.getChildAdapterPosition(it)].aceptoCliente

                        val telefonoKerkly = postList[recyclerview.getChildAdapterPosition(it)].Telefono
                        val correoKerkly = postList[recyclerview.getChildAdapterPosition(it)].correo_electronico

                        val pais = postList[recyclerview.getChildAdapterPosition(it)].Pais
                        val ciudad = postList[recyclerview.getChildAdapterPosition(it)].Ciudad
                        val calle = postList[recyclerview.getChildAdapterPosition(it)].Colonia
                        val colonia = postList[recyclerview.getChildAdapterPosition(it)].Calle

                        val nombre_completo_kerkly = "$nombre_kerkly $ap_kerkly $ap_kerkly_M"
                        val direccionKerkly = "$pais $ciudad $colonia $calle"
                        val nombreCliente = nombreCompletoCliente
                        var pagoTotal = postList[recyclerview.getChildAdapterPosition(it)].pago_total
                        var oficio = postList[recyclerview.getChildAdapterPosition(it)].nombreO


                        if (pagoTotal == 0.0){
                            Toast.makeText(this@MainActivityMostrarSolicitudes, "Por favor espere, le notificaremos en un momento", Toast.LENGTH_SHORT).show()
                            /* val intent = Intent(requireContext(), MainActivityAceptarServicio::class.java)
                               intent.putExtra("Nombre_Kerkly", nombre_kerkly)
                               intent.putExtra("Ap_Kerkly", ap_kerkly)
                               intent.putExtra("Nombre_completo_Kerkly", nomre_completo_kerkly)
                               intent.putExtra("IdContrato", id)
                               intent.putExtra("telefonoCliente", telefono)
                               intent.putExtra("nombreCompletoCliente",nombreCliente)
                               intent.putExtra("telefonokerkly", telefonoKerkly)
                               startActivity(intent)*/
                        }else{
                            //Toast.makeText(requireContext(), "ya hay presupuesto", Toast.LENGTH_SHORT).show()
                            if (aceptoCliente == "1"){
                                //Toast.makeText(requireContext(), "este presuepuesto ya sido aceptado", Toast.LENGTH_SHORT).show()
                                val intent  = Intent(this@MainActivityMostrarSolicitudes, FormaPagoExrpess::class.java)
                                b.putBoolean("Normal", true)
                                intent.putExtras(b)
                                startActivity(intent)
                            }else {
                                val i = Intent(this@MainActivityMostrarSolicitudes, MensajesExpress::class.java)
                                b.putString("NombreCliente", nombreCliente)
                                b.putString("tipoServicio", "Registrado")
                                b.putString("Telefono", telefono)
                                b.putString("Fecha", fecha)
                                b.putString("Problema", problema)
                                b.putInt("Folio", id)
                                b.putDouble("Pago total", pagoTotal)
                                b.putString("Oficio", oficio)
                                b.putString("telefonoKerkly", telefonoKerkly)
                                b.putString("nombreCompletoKerkly", nombre_completo_kerkly)
                                b.putString("direccionKerkly", direccionKerkly)
                                b.putString("correoKerkly", correoKerkly)
                                i.putExtras(b)
                                startActivity(i)
                            }
                        }
                    }
                    recyclerview.adapter = adapter
                }
            }
            override fun onFailure(call: Call<List<OrdenPendiente?>?>, t: Throwable) {
                setProgress.dialog.dismiss()
                Log.d("error del retrofit", t.toString())
                Toast.makeText(this@MainActivityMostrarSolicitudes, t.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

   /* private fun obtenerPresupuestoFirebase(id: Int, telefonoKerkly: String) {
        dabaseReference = firebaseDatabase.getReference("UsuariosR").child(telefonoKerkly).child("Presupuesto Normal"
        ).child("Presupuesto Normal $id")

        dabaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val m= snapshot.getValue()
                println(m)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }*/
}