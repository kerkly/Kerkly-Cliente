package com.example.kerklyv5.vista.fragmentos

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.MainActivityChats
import com.example.kerklyv5.R
import com.example.kerklyv5.controlador.AdapterOrdenPendiente
import com.example.kerklyv5.controlador.AdapterOrdenPendienteUrgente
import com.example.kerklyv5.controlador.setProgressDialog
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
interface LoadMoreListener {
    fun onLoadMore()
}
class MainActivityMostrarSolicitudes : AppCompatActivity(), SearchView.OnQueryTextListener,
    LoadMoreListener {
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
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    var alertDialog: AlertDialog? = null
    private lateinit var direccionKerkly:String
    private lateinit var correoKerkly:String
    //datos de las solicitudes
    private lateinit var fechaDeSolicitud:String
    private lateinit var problemaDeSolictud:String
    private  var numFolio:Int =0
    private var pagoTotal:Double = 0.0
    private lateinit var oficio:String
    private lateinit var telefonoKerkly:String
    private lateinit var nombre_completo_kerkly:String
    private  var uidKerkly = ""
    private lateinit var searchView: SearchView
    var postList: ArrayList<OrdenPendiente> =ArrayList()
    var postListUrgente: ArrayList<OrdenPendienteUrgente> =ArrayList()

    // Definir variables de paginación
    private var currentPage = 1
    private val pageSize = 15 // Por ejemplo, 10 elementos por página
    private var isLoading = false
    private var shouldLoadMoreData = true


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
        mAuth = FirebaseAuth.getInstance()
         currentUser = mAuth!!.currentUser
        b = intent.extras!!
        TipoSolicitud = b!!.getString("TipoDeSolicitud").toString()
        telefonoCliente = b!!.getString("Telefono").toString()
        nombreCompletoCliente =  b!!.getString("nombreCompletoCliente").toString()
        uidCliente = b!!.getString("uidCliente")!!
        Noti = b!!.getString("Noti")!!

        println("tel $telefonoCliente uid $uidCliente Tipo $TipoSolicitud")
        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(this)


        if (TipoSolicitud == "normal"){
            getOrdenesNormal()
        }
        if (TipoSolicitud == "urgente"){
           getOrdenesUrgente()
            println("entrooo $telefonoCliente")
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
        val call = presupuestoGET.ordenUrgente(telefonoCliente, currentPage, pageSize)
        call?.enqueue(object : retrofit2.Callback<List<OrdenPendienteUrgente?>?> {
            override fun onResponse(
                call: Call<List<OrdenPendienteUrgente?>?>,
                response: retrofit2.Response<List<OrdenPendienteUrgente?>?>
            ) {
                 postListUrgente = response.body()
                        as ArrayList<OrdenPendienteUrgente>

                //Log.d("lista", postList.toString())
                // postList.clear()
                if (postListUrgente.size == 0) {
                    recyclerview.visibility = View.GONE
                    setProgress.dialog.dismiss()
                } else {
                    img.visibility = View.GONE
                    txt.visibility = View.GONE
                    adapterUrgente = AdapterOrdenPendienteUrgente(postListUrgente)
                    currentPage++
                    adapterUrgente.loadMoreListener = this@MainActivityMostrarSolicitudes

                    setProgress.dialog.dismiss()
                    adapterUrgente.setOnClickListener {
                        var oficio = postListUrgente[recyclerview.getChildAdapterPosition(it)].nombreO
                        val idKerklyAcepto = postListUrgente[recyclerview.getChildAdapterPosition(it)].idKerklyAcepto
                        var bandKerkly = false
                        if (idKerklyAcepto == null) {
                           // Toast.makeText(this@MainActivityMostrarSolicitudes, "Por favor espere, le notificaremos en un momento", Toast.LENGTH_SHORT).show()
                            showCustomAlertDialog(bandKerkly,"urgente")
                        }else{
                         /*  val pago = postList[recyclerview.getChildAdapterPosition(it)].pago_total
                            if (pago==0.0){
                                showMensaje("ingresar Detalles del pago. pendiente")
                            }*/
                            bandKerkly = true
                            showCustomAlertDialog(bandKerkly,"urgente")
                        }
                    }
                    recyclerview.adapter = adapterUrgente
                    recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                            val totalItemCount = layoutManager.itemCount

                            if (!isLoading && lastVisibleItemPosition == totalItemCount - 1) {
                                isLoading = true
                                onLoadMore()
                                println("Scroll detectado - lastVisibleItemPosition: $lastVisibleItemPosition, totalItemCount: $totalItemCount")
                            }
                        }
                    })
                }
            }
            override fun onFailure(call: Call<List<OrdenPendienteUrgente?>?>, t: Throwable) {
                setProgress.dialog.dismiss()
                Log.d("error del retrofit", t.toString())
                Toast.makeText(this@MainActivityMostrarSolicitudes, t.toString(), Toast.LENGTH_LONG).show()
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
        val call = presupuestoGET.ordenP(telefonoCliente,currentPage, pageSize)

        call?.enqueue(object : retrofit2.Callback<List<OrdenPendiente?>?> {

            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(
                call: Call<List<OrdenPendiente?>?>,
                response: retrofit2.Response<List<OrdenPendiente?>?>
            ) {
                 postList = response.body()
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

                    currentPage++
                    adapter.loadMoreListener = this@MainActivityMostrarSolicitudes

                    adapter.setOnClickListener {
                        val nombre_kerkly = postList[recyclerview.getChildAdapterPosition(it)].NombreK.trim()
                        val ap_kerkly = postList[recyclerview.getChildAdapterPosition(it)].Apellido_PaternoK.trim()
                        val ap_kerkly_M = postList[recyclerview.getChildAdapterPosition(it)].Apellido_MaternoK.trim()

                        numFolio = postList[recyclerview.getChildAdapterPosition(it)].idContrato
                        problemaDeSolictud = postList[recyclerview.getChildAdapterPosition(it)].problema
                         fechaDeSolicitud = postList[recyclerview.getChildAdapterPosition(it)].fechaP
                        val aceptoCliente = postList[recyclerview.getChildAdapterPosition(it)].aceptoCliente
                         telefonoKerkly = postList[recyclerview.getChildAdapterPosition(it)].Telefono
                         correoKerkly = postList[recyclerview.getChildAdapterPosition(it)].correo_electronico

                        val pais = postList[recyclerview.getChildAdapterPosition(it)].Pais
                        val ciudad = postList[recyclerview.getChildAdapterPosition(it)].Ciudad
                        val calle = postList[recyclerview.getChildAdapterPosition(it)].Colonia
                        val colonia = postList[recyclerview.getChildAdapterPosition(it)].Calle

                         nombre_completo_kerkly = "$nombre_kerkly $ap_kerkly $ap_kerkly_M"
                         direccionKerkly = "$pais $ciudad $colonia $calle"
                         pagoTotal = postList[recyclerview.getChildAdapterPosition(it)].pago_total
                         oficio = postList[recyclerview.getChildAdapterPosition(it)].nombreO
                         uidKerkly = postList[recyclerview.getChildAdapterPosition(it)].uidKerkly

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
                            Toast.makeText(this@MainActivityMostrarSolicitudes, "uid $uidKerkly", Toast.LENGTH_SHORT).show()
                            if (uidKerkly == ""){
                                val i = Intent(this@MainActivityMostrarSolicitudes, MensajesExpress::class.java)
                                b.putString("NombreCliente", nombreCompletoCliente)
                                b.putString("tipoServicio", "Registrado")
                                b.putString("Telefono", telefonoCliente)
                                b.putString("Fecha", fechaDeSolicitud)
                                b.putString("Problema", problemaDeSolictud)
                                b.putInt("Folio", numFolio)
                                b.putString("pagoTotal", pagoTotal.toString())
                                b.putString("Oficio", oficio)
                                b.putString("telefonoKerkly", telefonoKerkly)
                                b.putString("nombreCompletoKerkly", nombre_completo_kerkly)
                                b.putString("direccionKerkly", direccionKerkly)
                                b.putString("correoKerkly", correoKerkly)
                                b.putString("uidKerkly", uidKerkly)
                                i.putExtras(b)
                                startActivity(i)
                            }else {
                                showCustomAlertDialog(true,"normal")
                             //  val pagoTotal2 = pagoTotal * 1.16
                            }
                        }
                    }
                    recyclerview.adapter = adapter
                    recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                            val totalItemCount = layoutManager.itemCount

                            if (!isLoading && lastVisibleItemPosition == totalItemCount - 1) {
                                isLoading = true
                                onLoadMore()
                                println("Scroll detectado - lastVisibleItemPosition: $lastVisibleItemPosition, totalItemCount: $totalItemCount")
                            }
                        }
                    })
                }
            }
            override fun onFailure(call: Call<List<OrdenPendiente?>?>, t: Throwable) {
                setProgress.dialog.dismiss()
                Log.d("error del retrofit", t.toString())
                Toast.makeText(this@MainActivityMostrarSolicitudes, t.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

   /*override fun onBackPressed() {
      /* if (Noti == "Noti"){
           val intent = Intent(this, SolicitarServicio::class.java)
           intent.putExtra("Telefono", telefonoCliente)
           startActivity(intent)
           finish()
       }else{
          // finish()
       }*/
         /* val intent = Intent()
       intent.putExtra("Telefono", telefonoCliente)
       intent.putExtra("nombreCompletoCliente", currentUser!!.displayName)
       intent.putExtra("uid",uidCliente)
          setResult(Activity.RESULT_OK, intent)
          finish()*/

   }*/
    private fun showCustomAlertDialog(kerkyyAcepto: Boolean,TipoSolicitud:String) {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialogpersonalizadourg, null)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val btnOption1 = dialogView.findViewById<Button>(R.id.btnOption1)
        val btnOption2 = dialogView.findViewById<Button>(R.id.btnOption2)
        val btnOption3 = dialogView.findViewById<Button>(R.id.btnOption3)
        val btnOption4 = dialogView.findViewById<Button>(R.id.btnOption4)

        if (kerkyyAcepto == false){
            btnOption1.visibility = View.GONE
            btnOption2.text = "1. ${getString(R.string.btnDialog2)}"
            btnOption3.text = "2. ${getString(R.string.btnDialog3)}"
            btnOption4.text = "3. ${getString(R.string.btnDialog4)}"
        }else{
            btnOption1.text = "1. ${getString(R.string.btnDialog1)}"
            btnOption2.text = "2. ${getString(R.string.btnDialog2)}"
            btnOption3.text = "3. ${getString(R.string.btnDialog3)}"
            btnOption4.text = "4. ${getString(R.string.btnDialog4)}"
        }

        val dialog = Dialog(this)
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)
        dialog.show()

        btnOption1.setOnClickListener {
            // traslado del kerkly
            showMensaje("pendiente")
            dialog.dismiss()
        }
        btnOption2.setOnClickListener {
            // realizar pago
            if (TipoSolicitud == "urgente"){
                showMensaje("pendiente")
                dialog.dismiss()
            }else{
                val intent = Intent(this@MainActivityMostrarSolicitudes,CheckoutActivity::class.java)
                intent.putExtra("NombreCliente", nombreCompletoCliente)
                intent.putExtra("tipoServicio", "Registrado")
                intent.putExtra("Telefono", telefonoCliente)
                intent.putExtra("Fecha", fechaDeSolicitud)
                intent.putExtra("Problema", problemaDeSolictud)
                intent.putExtra("Folio", numFolio)
                intent.putExtra("pagoTotal", pagoTotal.toString())
                intent.putExtra("Oficio", oficio)
                intent.putExtra("telefonoKerkly", telefonoKerkly)
                intent.putExtra("nombreCompletoKerkly", nombre_completo_kerkly)
                intent.putExtra("direccionKerkly", direccionKerkly)
                intent.putExtra("correoKerkly", correoKerkly)
                intent.putExtra("uidKerkly", uidKerkly)
                startActivity(intent)
            }
        }
        btnOption3.setOnClickListener {
            // cancelar el servicio
            showMensaje("pendiente")
            dialog.dismiss()
        }

        btnOption4.setOnClickListener {
            // cancelar dialog
            dialog.dismiss()
        }
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        Log.d("MainActivity", "onQueryTextChange called with $newText")
        if (newText.isNullOrBlank()) {
            // El texto de búsqueda está vacío
            if (TipoSolicitud == "normal"){
                println("Usuario ha borrado todo el texto poslist ${postList.size}")
                adapter.showOriginalList()
            }
            if (TipoSolicitud == "urgente"){
                adapterUrgente.showOriginalList()
            }

        } else {
            println("texto ingresado $newText")
            if (TipoSolicitud == "normal"){
                println("tamaño de poslit ${postList.size}")
                adapter.filter.filter(newText)
            }
            if (TipoSolicitud == "urgente"){
                adapterUrgente.filter.filter(newText)
            }
        }
        return true
    }

    @SuppressLint("LongLogTag")
    override fun onLoadMore() {
        if (TipoSolicitud == "urgente"){
            isLoading = true
            cargarMasDatosUrgente()
        }

        if(TipoSolicitud == "normal"){
            isLoading = true
            cargarMasDatosNormal()
        }

    }

    private fun cargarMasDatosNormal() {
        if (!shouldLoadMoreData) {
            // Si no se deben cargar más datos, sal de la función
            return
        }
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
        val call = presupuestoGET.ordenP(telefonoCliente, currentPage, pageSize)

        call?.enqueue(object : retrofit2.Callback<List<OrdenPendiente?>?> {
            @SuppressLint("LongLogTag")
            override fun onResponse(
                call: Call<List<OrdenPendiente?>?>,
                response: retrofit2.Response<List<OrdenPendiente?>?>
            ) {
                val nuevosDatos = response.body() as ArrayList<OrdenPendiente>

                if (nuevosDatos.isNotEmpty()) {

                    // Agregar los nuevos datos a la lista existente
                    postList.addAll(nuevosDatos)
                    adapter.masDatos(nuevosDatos)
                    // Notificar al adaptador que los datos han cambiado
                    adapter.notifyDataSetChanged()

                    // Incrementar la página para la próxima carga
                    currentPage++
                    // Log de información para verificar el estado
                    Log.d("MainActivityMostrarSolicitudes", "onResponse - currentPage: $currentPage")
                    Log.d("MainActivityMostrarSolicitudes", "onResponse - postListUrgente size: ${postList.size}")
                } else {
                    // Si no hay más datos, puedes manejarlo según tus necesidades
                    Toast.makeText(
                        this@MainActivityMostrarSolicitudes,
                        "No hay más datos",
                        Toast.LENGTH_SHORT
                    ).show()
                    shouldLoadMoreData = false
                }

                isLoading = false // Restablecer el estado de carga
                setProgress.dialog.dismiss()
            }

            override fun onFailure(call: Call<List<OrdenPendiente?>?>, t: Throwable) {
                isLoading = false // Restablecer el estado de carga en caso de fallo
                setProgress.dialog.dismiss()
                Log.d("error del retrofit", t.toString())
                Toast.makeText(
                    this@MainActivityMostrarSolicitudes,
                    t.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun cargarMasDatosUrgente() {
        if (!shouldLoadMoreData) {
            // Si no se deben cargar más datos, sal de la función
            return
        }
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
        val call = presupuestoGET.ordenUrgente(telefonoCliente, currentPage, pageSize)

        call?.enqueue(object : retrofit2.Callback<List<OrdenPendienteUrgente?>?> {
            @SuppressLint("LongLogTag")
            override fun onResponse(
                call: Call<List<OrdenPendienteUrgente?>?>,
                response: retrofit2.Response<List<OrdenPendienteUrgente?>?>
            ) {
                val nuevosDatos = response.body() as ArrayList<OrdenPendienteUrgente>

                if (nuevosDatos.isNotEmpty()) {

                    // Agregar los nuevos datos a la lista existente
                    postListUrgente.addAll(nuevosDatos)
                    adapterUrgente.masDatos(nuevosDatos)
                    // Notificar al adaptador que los datos han cambiado
                    adapterUrgente.notifyDataSetChanged()

                    // Incrementar la página para la próxima carga
                    currentPage++
                    // Log de información para verificar el estado
                    Log.d("MainActivityMostrarSolicitudes", "onResponse - currentPage: $currentPage")
                    Log.d("MainActivityMostrarSolicitudes", "onResponse - postListUrgente size: ${postListUrgente.size}")
                } else {
                    shouldLoadMoreData =  false
                    // Si no hay más datos, puedes manejarlo según tus necesidades
                    Toast.makeText(
                        this@MainActivityMostrarSolicitudes,
                        "No hay más datos",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                isLoading = false // Restablecer el estado de carga
                setProgress.dialog.dismiss()
            }

            override fun onFailure(call: Call<List<OrdenPendienteUrgente?>?>, t: Throwable) {
                isLoading = false // Restablecer el estado de carga en caso de fallo
                setProgress.dialog.dismiss()
                Log.d("error del retrofit", t.toString())
                Toast.makeText(
                    this@MainActivityMostrarSolicitudes,
                    t.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }



}