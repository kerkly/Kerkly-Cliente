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
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.distancia_tiempo.CalcularTiempoDistancia
import com.example.kerklyv5.interfaces.IngresarPresupuestoClienteInterface
import com.example.kerklyv5.interfaces.ObtenerKerklyInterface
import com.example.kerklyv5.modelo.modeloSolicituUrgente
import com.example.kerklyv5.modelo.serial.Kerkly
import com.example.kerklyv5.modelo.serial.modeloSolicitudNormal
import com.example.kerklyv5.modelo.usuarios
import com.example.kerklyv5.notificaciones.llamarTopico
import com.example.kerklyv5.notificaciones.obtenerKerklys_y_tokens
import com.example.kerklyv5.url.Instancias
import com.example.kerklyv5.url.Url
import com.google.firebase.database.*
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
import java.text.DateFormat
import java.util.ArrayList
import java.util.Date

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
    private lateinit var ciudad: String
    private lateinit var calle: String
    private lateinit var cp: String
    private lateinit var colonia: String
    private lateinit var num_ext: String
  //  private lateinit var referencia: String
    private lateinit var estado: String
    private lateinit var pais: String

    private lateinit var nombreCliente: String
    private  val setProgress = setProgressDialog()
    private val obtenerToken = obtenerKerklys_y_tokens()
    private lateinit var correoCliente:String
    private lateinit var instancias: Instancias
    private lateinit var uid:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kerkly_list)
        setProgress.setProgressDialog(this)
        b = intent.extras!!

        context = this
        instancias = Instancias()

        latitud = b.getDouble("Latitud")
        longitud = b.getDouble("Longitud")
        ciudad = b.getString("Ciudad").toString()
        calle = b.getString("Calle").toString()
        estado = b.getString("Estado").toString()
        cp = b.getString("Código Postal").toString()
        colonia = b.getString("Colonia").toString()
        num_ext = b.getString("Exterior").toString()
     //   referencia = b.getString("Referencia").toString()
        pais = b.getString("Pais").toString()
        nombreCliente = b.getString("nombreCliente")!!
        correoCliente =  b.getString("correo")!!
        uid = b.getString("uid")!!

        recyclerview = findViewById(R.id.recycler_kerkly)
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager= LinearLayoutManager(applicationContext)

        telefono = b.get("Telefono").toString()
        oficio = b.getString("Oficio").toString()
        problema = b.get("Problema").toString()

        getKerklysCercanos()
    }

    private fun getKerklysCercanos () {
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

            override fun onResponse(call: Call<List<Kerkly?>?>, response: retrofit2.Response<List<Kerkly?>?>) {

                postlist = response.body() as ArrayList<Kerkly>
                var size = 0

                if (postlist!!.size > 0) {
                   // if (postlist!!.size >= 4) {
                     //   size = 4
                    //} else {
                      //  size = postlist!!.size
                    //}


                    for (i in 0 until postlist!!.size) {
                        var latitudFinal = postlist!![i].latitud
                        var longitudFinal = postlist!![i].longitud

                        val url2 = instancias.CalcularDistancia(latitud,longitud,latitudFinal,longitudFinal)
                        CalcularTiempoDistancia(context).execute(url2)
                    }
                }

                adapter = AdapterKerkly(postlist!!)

                adapter.setOnClickListener {
                   // Log.d("curp", "Hora: " + postlist!![recyclerview.getChildAdapterPosition(it)].minutos)

                    curp = postlist!![recyclerview.getChildAdapterPosition(it)].Curp

                    //primero Mandamos la solicitud a un kerkly
                    val telefoK =  postlist!![recyclerview.getChildAdapterPosition(it)].Telefonok
                    System.out.println("el telefo del kerkly $telefoK")
                    setProgress.setProgressDialog(this@KerklyListActivity)
                    obtenerToken.obtenerTokenKerkly(uid, problema, nombreCliente, this@KerklyListActivity)
                    enviarSolicitud()
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
        System.out.println("valor de e : $e")


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
        setProgress.dialog.dismiss()
    }

    private fun enviarSolicitud() {
        val fechaHora = DateFormat.getDateTimeInstance().format(Date())
        insertarSolicitudFirebaseNormal("",problema,correoCliente, oficio,curp,false,fechaHora,latitud,longitud,false)
        //println("uid ......_:: $uid" )
        val ROOT_URL = Url().url
        val adapter = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api = adapter.create(IngresarPresupuestoClienteInterface::class.java)
        api.presupuesto(curp,
            problema,
            telefono,
            oficio,
            latitud,
            longitud,
            ciudad,
            estado,
            pais,
            calle,
            colonia,
            num_ext,
            cp,
            correoCliente,
            uid,
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
                   // Toast.makeText(applicationContext, "Entre por aqui", Toast.LENGTH_LONG).show()
                    val cadena = "Datos enviados"
                    if (cadena.equals(entrada)){
                        setProgress.dialog.dismiss()
                        intent = Intent(applicationContext, SolicitarServicio::class.java)
                        b.putBoolean("PresupuestoListo", true)
                        intent.putExtras(b)
                        startActivity(intent)
                        Toast.makeText(applicationContext,"Solicitud en proceso", Toast.LENGTH_LONG).show()
                    }else{
                        setProgress.dialog.dismiss()
                        Toast.makeText(applicationContext,"$entrada", Toast.LENGTH_LONG).show()
                    }
                }

                override fun failure(error: RetrofitError?) {
                    println("error$error")
                    Toast.makeText(applicationContext, "error $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    fun insertarSolicitudFirebaseNormal(
        pago_total: String,
        problema: String,
        correo: String,
        TipoServicio: String,
        idkerkly: String,
        clienteAcepta: Boolean,
        fechaHora: String,
        latitud: Double,
        longitud: Double,
        trabajoTerminado: Boolean,
    ) {
        val reference = instancias.referenciaSolicitudNormal(uid)
        val query = reference.orderByKey().limitToLast(1)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val lastEntry = dataSnapshot.children.first()
                    val lastId = lastEntry.key
                    if (lastId != null) {
                        // Utiliza el último ID aquí
                        println("Último ID agregado: $lastId")
                        val id= lastId.toInt()+1
                        val modelo = modeloSolicitudNormal(
                            id,
                            pago_total,
                            problema,
                            correo,
                            TipoServicio,
                            idkerkly,
                            clienteAcepta,
                            fechaHora,
                            latitud,
                            longitud,
                            trabajoTerminado
                        )
                        val countersRef2 = instancias.referenciaSolicitudNormal(uid).child(id.toString())
                        countersRef2.setValue(modelo) { error, _ ->
                            if (error == null) {
                                showMessaje("Solicitud Enviada")
                                val intent = Intent(applicationContext, SolicitarServicio::class.java)
                                b.putBoolean("PresupuestoListo", true)
                                intent.putExtras(b)
                                startActivity(intent)
                                finish()
                            } else {
                                // Manejar el error en caso de que ocurra
                                showMessaje("hubo un error ")
                            }
                        }

                    } else {
                        // Manejar el caso si lastId es null
                        println("Último ID agregado: $lastId")
                        val modelo = modeloSolicitudNormal(
                            1,
                            pago_total,
                            problema,
                            correo,
                            TipoServicio,
                            idkerkly,
                            clienteAcepta,
                            fechaHora,
                            latitud,
                            longitud,
                            trabajoTerminado
                        )
                        val countersRef2 = instancias.referenciaSolicitudNormal(uid).child("1")
                        countersRef2.setValue(modelo) { error, _ ->
                            if (error == null) {
                                showMessaje("Solicitud Enviada")
                                val intent = Intent(applicationContext, SolicitarServicio::class.java)
                                b.putBoolean("PresupuestoListo", true)
                                intent.putExtras(b)
                                startActivity(intent)
                                finish()
                            } else {
                                // Manejar el error en caso de que ocurra
                                showMessaje("hubo un error ")
                            }
                        }

                    }
                } else {
                    // Manejar el caso si no hay entradas en la referencia
                    println(" no hay entradas en la referencia")
                   // showMessaje("hubo un error ")
                    val modelo = modeloSolicitudNormal(
                        1,
                        pago_total,
                        problema,
                        correo,
                        TipoServicio,
                        idkerkly,
                        clienteAcepta,
                        fechaHora,
                        latitud,
                        longitud,
                        trabajoTerminado
                    )
                    val countersRef2 = instancias.referenciaSolicitudNormal(uid).child("1")
                    countersRef2.setValue(modelo) { error, _ ->
                        if (error == null) {
                            showMessaje("Solicitud Enviada")
                            val intent = Intent(applicationContext, SolicitarServicio::class.java)
                            b.putBoolean("PresupuestoListo", true)
                            intent.putExtras(b)
                            startActivity(intent)
                            finish()
                        } else {
                            // Manejar el error en caso de que ocurra
                            showMessaje("hubo un error ")
                        }
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar el error si ocurre
                showMessaje(databaseError.toString())
            }
        })

    }
    fun showMessaje(mensaje:String){
        Toast.makeText(this,mensaje,Toast.LENGTH_SHORT).show()
    }
}