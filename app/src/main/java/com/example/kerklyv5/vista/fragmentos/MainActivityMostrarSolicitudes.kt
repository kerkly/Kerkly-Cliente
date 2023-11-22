package com.example.kerklyv5.vista.fragmentos

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.MainActivityAceptarServicio
import com.example.kerklyv5.MainActivityChats
import com.example.kerklyv5.R
import com.example.kerklyv5.SQLite.DataManager
import com.example.kerklyv5.SolicitarServicio
import com.example.kerklyv5.controlador.AdapterOrdenPendiente
import com.example.kerklyv5.controlador.AdapterOrdenPendienteUrgente
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.express.FormaPagoExrpess
import com.example.kerklyv5.express.MensajesExpress
import com.example.kerklyv5.interfaces.ObtenerOrdenPendienteInterface
import com.example.kerklyv5.modelo.serial.OrdenPendiente
import com.example.kerklyv5.modelo.serial.OrdenPendienteUrgente
import com.example.kerklyv5.modelo.usuarios
import com.example.kerklyv5.pasarelaPagos.CheckoutActivity
import com.example.kerklyv5.url.Instancias
import com.example.kerklyv5.url.Url
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivityMostrarSolicitudes : AppCompatActivity() {
    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: AdapterOrdenPendiente
    private lateinit var adapterUrgente: AdapterOrdenPendienteUrgente
    private lateinit var img: ImageView
    private lateinit var txt: TextView
    private lateinit var telefonoCliente: String
    private lateinit var nombreCompletoCliente: String

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var dabaseReference: DatabaseReference
    val setProgress= setProgressDialog()

    private lateinit var TipoSolicitud: String
    private lateinit var b: Bundle
    private lateinit var instancias: Instancias
    private lateinit var  uidCliente:String
    private lateinit var Noti: String
    //private var mAuth: FirebaseAuth? = null
  //  private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_mostrar_solicitudes)

        recyclerview = findViewById(R.id.recycler_ordenesPendientes)
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager = LinearLayoutManager(this)
        img = findViewById(R.id.img_ordenesPendientes)
        txt = findViewById(R.id.txt_ordenesPendientes)
        firebaseDatabase = FirebaseDatabase.getInstance()
        instancias = Instancias()
       //  mAuth = FirebaseAuth.getInstance()
       //  currentUser = mAuth!!.currentUser

        b = intent.extras!!
        TipoSolicitud = b!!.getString("TipoDeSolicitud").toString()
        telefonoCliente = b!!.getString("Telefono").toString()
        nombreCompletoCliente =  b!!.getString("nombreCompletoCliente").toString()
        uidCliente = b!!.getString("uidCliente")!!
        Noti = b!!.getString("Noti")!!

        if (TipoSolicitud == "normal"){
            getOrdenesNormal()
        }

        if (TipoSolicitud == "urgente"){
           getOrdenesUrgente()
        }
    }

    private fun getOrdenesUrgente() {
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
        val call = presupuestoGET.ordenUrgente(telefonoCliente)

        call?.enqueue(object : retrofit2.Callback<List<OrdenPendienteUrgente?>?> {
            override fun onResponse(
                call: Call<List<OrdenPendienteUrgente?>?>,
                response: retrofit2.Response<List<OrdenPendienteUrgente?>?>
            ) {
                val postList: ArrayList<OrdenPendienteUrgente> = response.body()
                        as ArrayList<OrdenPendienteUrgente>

                //Log.d("lista", postList.toString())
                // postList.clear()

                if (postList.size == 0) {
                    recyclerview.visibility = View.GONE
                    setProgress.dialog.dismiss()

                } else {
                    img.visibility = View.GONE
                    txt.visibility = View.GONE
                    adapterUrgente = AdapterOrdenPendienteUrgente(postList)
                    setProgress.dialog.dismiss()

                    adapterUrgente.setOnClickListener {

                        var pagoTotal = postList[recyclerview.getChildAdapterPosition(it)].pago_total
                        var oficio = postList[recyclerview.getChildAdapterPosition(it)].nombreO
                        val idKerklyAcepto = postList[recyclerview.getChildAdapterPosition(it)].idKerklyAcepto

                        if (idKerklyAcepto == null) {
                            Toast.makeText(this@MainActivityMostrarSolicitudes, "Por favor espere, le notificaremos en un momento", Toast.LENGTH_SHORT).show()
                        }else{
                           val pago = postList[recyclerview.getChildAdapterPosition(it)].pago_total
                            if (pago==0.0){
                                showMensaje("ingresar Detalles del pago. pendiente")
                            }else{
                                val intent = Intent(this@MainActivityMostrarSolicitudes,CheckoutActivity::class.java)
                                intent.putExtra("NombreCliente", nombreCompletoCliente)
                                intent.putExtra("pagoTotal", pago )
                                startActivity(intent)
                            }
                            //val uidKerkly = postList[recyclerview.getChildAdapterPosition(it)].uidKerkly
                          //  obtenerKerkly(uidKerkly, curp)
                          //  val intent = Intent(this@MainActivityMostrarSolicitudes, MainActivityChats::class.java)
                           // startActivity(intent)

                        }

                    }
                    recyclerview.adapter = adapterUrgente
                }
            }
            override fun onFailure(call: Call<List<OrdenPendienteUrgente?>?>, t: Throwable) {
                setProgress.dialog.dismiss()
                Log.d("error del retrofit", t.toString())
                Toast.makeText(this@MainActivityMostrarSolicitudes, t.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun obtenerKerkly(uidKerkly: String, curp: String) {
        val databaseUsu = instancias.referenciaInformacionDelKerkly(uidKerkly)
        databaseUsu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val u2 = snapshot.getValue(usuarios::class.java)
                val nombre = u2!!.nombre
                val correo = u2!!.correo
                val telefono = u2!!.telefono
                val token =  u2!!.token
                val urlfoto =  u2!!.foto
                val intent = Intent(this@MainActivityMostrarSolicitudes, MainActivityChats::class.java)
                b!!.putString("nombreCompletoK", nombre)
                b!!.putString("correoK", correo)
                b!!.putString("telefonok",telefono)
                b!!.putString("telefonoCliente", telefonoCliente)
                b!!.putString("tokenKerkly", token)
                b!!.putString("nombreCompletoCliente", nombre)
                b!!.putString("urlFotoKerkly",urlfoto)
                b!!.putString("idCliente",uidCliente)
                b!!.putString("idKerkly",uidKerkly)

                intent.putExtras(b!!)
                startActivity(intent)
            }
            override fun onCancelled(error: DatabaseError) {
                System.out.println("Firebase: $error")
                showMensaje("Firebase: $error")
            }
        })
    }
private  fun showMensaje(mensaje:String){
    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
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
        val call = presupuestoGET.ordenP(telefonoCliente)

        call?.enqueue(object : retrofit2.Callback<List<OrdenPendiente?>?> {

            @SuppressLint("SuspiciousIndentation")
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
                        val pagoTotal = postList[recyclerview.getChildAdapterPosition(it)].pago_total
                        val oficio = postList[recyclerview.getChildAdapterPosition(it)].nombreO
                        val uidKerkly = postList[recyclerview.getChildAdapterPosition(it)].uidKerkly

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

                            //val intent = Intent(this@MainActivityMostrarSolicitudes,CheckoutActivity::class.java)
                           // startActivity(intent)
                        }else{
                            //Toast.makeText(requireContext(), "ya hay presupuesto", Toast.LENGTH_SHORT).show()
                            if (aceptoCliente == "1"){
                                //Toast.makeText(requireContext(), "este presuepuesto ya sido aceptado", Toast.LENGTH_SHORT).show()
                               /* val intent  = Intent(this@MainActivityMostrarSolicitudes, FormaPagoExrpess::class.java)
                                b.putBoolean("Normal", true)
                                intent.putExtras(b)
                                startActivity(intent)*/
                                //showMensaje("pagooo ${pagoTotal.toString()}")
                                val intent = Intent(this@MainActivityMostrarSolicitudes,CheckoutActivity::class.java)
                                intent.putExtra("NombreCliente", nombreCompletoCliente)
                                intent.putExtra("tipoServicio", "Registrado")
                                intent.putExtra("Telefono", telefonoCliente)
                                intent.putExtra("Fecha", fecha)
                                intent.putExtra("Problema", problema)
                                intent.putExtra("Folio", id)
                                intent.putExtra("pagoTotal", pagoTotal.toString())
                                intent.putExtra("Oficio", oficio)
                                intent.putExtra("telefonoKerkly", telefonoKerkly)
                                intent.putExtra("nombreCompletoKerkly", nombre_completo_kerkly)
                                intent.putExtra("direccionKerkly", direccionKerkly)
                                intent.putExtra("correoKerkly", correoKerkly)
                                intent.putExtra("uidKerkly", uidKerkly)
                                startActivity(intent)
                            }else {
                             //  val pagoTotal2 = pagoTotal * 1.16
                               val i = Intent(this@MainActivityMostrarSolicitudes, MensajesExpress::class.java)
                                b.putString("NombreCliente", nombreCompletoCliente)
                                b.putString("tipoServicio", "Registrado")
                                b.putString("Telefono", telefonoCliente)
                                b.putString("Fecha", fecha)
                                b.putString("Problema", problema)
                                b.putInt("Folio", id)
                                b.putString("pagoTotal", pagoTotal.toString())
                                b.putString("Oficio", oficio)
                                b.putString("telefonoKerkly", telefonoKerkly)
                                b.putString("nombreCompletoKerkly", nombre_completo_kerkly)
                                b.putString("direccionKerkly", direccionKerkly)
                                b.putString("correoKerkly", correoKerkly)
                                b.putString("uidKerkly", uidKerkly)

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
   override fun onBackPressed() {
       if (Noti == "Noti"){
           val intent = Intent(this, SolicitarServicio::class.java)
           intent.putExtra("Telefono", telefonoCliente)
           startActivity(intent)
           finish()
       }else{
           finish()
       }

   }

}