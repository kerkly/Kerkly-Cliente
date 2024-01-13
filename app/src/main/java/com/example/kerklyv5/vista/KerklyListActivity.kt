package com.example.kerklyv5.vista

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.BaseDatosEspacial.Kerkly
import com.example.kerklyv5.BaseDatosEspacial.conexionPostgreSQL
import com.example.kerklyv5.R
import com.example.kerklyv5.SolicitarServicio
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.distancia_tiempo.CalcularTiempoDistancia
import com.example.kerklyv5.interfaces.IngresarPresupuestoClienteInterface
import com.example.kerklyv5.modelo.adapterUsuariosCercanos
import com.example.kerklyv5.modelo.serial.modeloSolicitudNormal
import com.example.kerklyv5.modelo.usuariosCercanosPerfil
import com.example.kerklyv5.modelo.usuariosKerkly
import com.example.kerklyv5.notificaciones.llamarTopico
import com.example.kerklyv5.notificaciones.obtenerKerklys_y_tokens
import com.example.kerklyv5.url.Instancias
import com.example.kerklyv5.url.Url
import com.google.firebase.database.*
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.Locale

class KerklyListActivity : AppCompatActivity(), CalcularTiempoDistancia.Geo {
    private lateinit var oficio: String
    private lateinit var problema: String
  //  private lateinit var curp: String
   private lateinit var telefono: String
    private lateinit var recyclerview: RecyclerView
   // private lateinit var adapter: AdapterKerkly
   private lateinit var Miadapter: adapterUsuariosCercanos
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
    private lateinit var uidCliente:String
    private lateinit var conexionPostgreSQL: conexionPostgreSQL
    private lateinit var direccion:String
    private lateinit var telefonoCliente:String
    private lateinit var Curp:String
    private var handler: Handler? = null
    private var fechaHoraSolicitud = ""
    private lateinit var nombreOficio:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kerkly_list)
        setProgress.setProgressDialog(this)
        b = intent.extras!!

        context = this
        instancias = Instancias()
        conexionPostgreSQL = conexionPostgreSQL()
        postlist = ArrayList<Kerkly>()
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
        uidCliente = b.getString("uid")!!
        direccion = b.getString("direccion").toString()
        telefonoCliente = b.getString("telefonoCliente").toString()
        Curp = b.getString("Curp").toString()
        nombreOficio = b.getString("nombreOficio").toString()

        Miadapter = adapterUsuariosCercanos(this)
        recyclerview = findViewById(R.id.recycler_UsuariosPerfil)
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = Miadapter

        Log.d("kerklylist ", "Ubicacion kerklylist, $latitud, $longitud")
        Miadapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                setScrollBar()
            }
        })
        telefono = b.get("Telefono").toString()
        oficio = b.getString("Oficio").toString()
        problema = b.get("Problema").toString()

        val handlerThread = HandlerThread("obtenerKerklyNormal")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
      /* handler?.post({
          //  ObtenerKerklyMasCercanos()
        })*/

        val kerklysCercanos = b.getParcelableArrayList<Kerkly>("kerklyCercanos")
        // postlist = kerklysCercanos.reversed() as ArrayList<Kerkly>
        postlist = ArrayList(kerklysCercanos!!.reversed())
        for (kerkly in kerklysCercanos.reversed()){
            //println("kerkly ----->id ${kerkly.idKerkly} ${kerkly.uidKerkly} ${kerkly.distancia}")
            val url2 = instancias.CalcularDistancia(latitud, longitud, kerkly.latitud, kerkly.longitud)
            CalcularTiempoDistancia(this@KerklyListActivity).execute(url2)
        }
        setProgress.dialog.dismiss()
    }
    private fun setScrollBar() {
        recyclerview.scrollToPosition(Miadapter.itemCount-1)
        // println("entro 217 "+ {Miadapter.itemCount-1 })
    }


    @SuppressLint("SuspiciousIndentation")
    override fun setDouble(min: String?) {
        val res = min!!.split(",").toTypedArray()
        val min = res[0].toDouble() / 60
        val dist = res[1].toInt() / 1000
        i2 = i2!! + 1
        val e = i2!! - 1
        System.out.println("valor de e : $e")
        // postlist!![e].hora = (min / 60).toInt().toString()
        // postlist!![e].minuto = (min % 60).toInt().toString()
        var hora = (min / 60).toInt().toString()
        var minuto = (min % 60).toInt().toString()
        if (hora.toInt() == 0){
            System.out.println("Kerkly a $minuto minutos de distancia $dist")
            postlist!![e].distancia = "Kerkly a $minuto minutos de distancia"
        }else{
            System.out.println("Kerkly a $hora hora  y $minuto minutos de distancia $dist")
            postlist!![e].distancia = "Kerkly a $hora hora  y $minuto minutos de distancia"

        }
        if (e == (postlist!!.size - 1)) {
            /* postlist!!.sortBy {
                 it.distancia
             }*/
            recorrerLista()
        }
        //System.out.println("Kerkly $e ${postlist!![e].distancia}")
    }


    fun recorrerLista (){
        for(i in 0 until postlist!!.size){
            //System.out.println(postlist!!.get(i).uidKerkly)
            //System.out.println("hora ${postlist!!.get(i).uidKerkly} ${postlist!!.get(i).latitud} ${postlist!!.get(i).longitud}  " + postlist!!.get(i).distancia)
            obtenerInfoKerkly(postlist!![i].uidKerkly,  postlist!!.get(i).distancia)
        }
        recyclerview.adapter = Miadapter
        setProgress.dialog.dismiss()
    }
/*    private fun ObtenerKerklyMasCercanos2() {
       // latitud = 17.540419
       // longitud = -99.495576
        try {
        val conexion = conexionPostgreSQL.obtenerConexion(this)
        conexion.use {
            // Llamada al método poligonoCircular con el callback
            if (conexion != null) {
                val secciones = conexionPostgreSQL.poligonoCircular(latitud, longitud, 1000.0,)
                if (secciones != null) {
                    val kerklysCercanos = conexionPostgreSQL.Los5KerklyMasCercanos(secciones, longitud, latitud, oficio)
                    if (kerklysCercanos == null || kerklysCercanos.isEmpty()) {
                        conexionPostgreSQL.cerrarConexion()
                        // Si no se encontraron Kerklys, imprimir un mensaje
                        showMessaje("Lo sentimos, pero en esta área no se encuentran kerklys cercanos")
                        setProgress.dialog.dismiss()
                       /* val intent = Intent(applicationContext, SolicitarServicio::class.java)
                        b.putBoolean("PresupuestoListo", false)
                        intent.putExtras(b)
                        startActivity(intent)
                        finish()*/
                        handler?.looper?.quitSafely()
                    } else {
                       // postlist = kerklysCercanos.reversed() as ArrayList<Kerkly>
                        postlist = ArrayList(kerklysCercanos.reversed())
                        for (kerkly in kerklysCercanos.reversed()){
                            //println("kerkly ----->id ${kerkly.idKerkly} ${kerkly.uidKerkly} ${kerkly.distancia}")
                            conexionPostgreSQL.cerrarConexion()
                            val url2 = instancias.CalcularDistancia(latitud, longitud, kerkly.latitud, kerkly.longitud)
                            CalcularTiempoDistancia(this@KerklyListActivity).execute(url2)
                        }
                        setProgress.dialog.dismiss()
                        handler?.looper?.quitSafely()
                    }

                } else {
                    // El objeto es null, manejarlo según sea necesario
                    showMessaje("La lista de secciones es null")
                    conexionPostgreSQL.cerrarConexion()
                    setProgress.dialog.dismiss()
                    handler?.looper?.quitSafely()
                }
            }else{
                showMessaje("Problemas de conexión")
                setProgress.dialog.dismiss()
                handler?.looper?.quitSafely()
            }

        }

        } catch (e: Exception) {
            // Maneja excepciones específicas según tu lógica de manejo de errores
            e.printStackTrace()
            showMessaje("Error: ${e.message}")
            setProgress.dialog.dismiss()
            handler?.looper?.quitSafely()
        }


    }*/

    private fun obtenerInfoKerkly(uid:String,hora:String){
        val databaseUsu = instancias.referenciaInformacionDelKerkly(uid)
        databaseUsu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //println(" datos101: " + snapshot.getValue())
                val u2 = snapshot.getValue(usuariosKerkly::class.java)
                if (u2 ==null){
                    //Toast.makeText(requireContext(), "No Tienes Ningun Cotacto", Toast.LENGTH_SHORT).show()
                   // showMessaje("todo mal")
                }else{
                   // showMessaje("entro todo bien ${u2.nombre}")
                    var usuarios: usuariosCercanosPerfil
                    usuarios = usuariosCercanosPerfil()
                    usuarios.telefono= u2.telefono
                    usuarios.correo = u2.correo
                    usuarios.fechaHora = u2.fechaHora
                    usuarios.nombre = u2.nombre
                    usuarios.token = u2.token
                    usuarios.foto = u2.foto
                     usuarios.hora = hora
                    usuarios.curp = u2.curp

                    Miadapter.agregarUsuario(usuarios)
                }
                val mGestureDetector = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                    GestureDetector(this@KerklyListActivity, object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapUp(e: MotionEvent): Boolean {
                            return true
                        }
                    })
                } else {
                    TODO("VERSION.SDK_INT < CUPCAKE")
                }
                recyclerview.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                    override fun onRequestDisallowInterceptTouchEvent(b: Boolean) {}
                    @RequiresApi(Build.VERSION_CODES.N)
                    override fun onInterceptTouchEvent(
                        recyclerView: RecyclerView, motionEvent: MotionEvent
                    ): Boolean {
                        try {
                            val child = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
                            if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                                val position = recyclerView.getChildAdapterPosition(child)
                                val correo = Miadapter.lista[position].correo
                                val nombre = Miadapter.lista[position].nombre
                                val telefono = Miadapter.lista[position].telefono
                                val urlfoto = Miadapter.lista[position].foto
                                val token =Miadapter.lista[position].token
                                val uid = Miadapter.lista[position].uid
                                val curp = Miadapter.lista[position].curp
                                //showMessaje("clik en $nombre")
                                enviarSolicitud(curp,token,nombre,telefono)
                                /*val intent = Intent(applicationContext, SolicitarServicio::class.java)
                                b.putBoolean("PresupuestoListo", true)
                                intent.putExtras(b)
                                startActivity(intent)
                                finish()*/
                                return true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return false
                    }
                    override fun onTouchEvent(
                        recyclerView: RecyclerView,
                        motionEvent: MotionEvent
                    ) {
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                System.out.println("Firebase: $error")
            }


        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun obtenerFechaFormateada(timestampMillis: Long): String {
        try {
            // Crea un objeto Date a partir del timestamp
            val fecha = java.sql.Date(timestampMillis)

            // Formato deseado para la fecha y hora
            val formato = SimpleDateFormat("hh:mm a dd 'de' MMMM yyyy", Locale.getDefault())

            // Formatea la fecha y devuelve la cadena resultante
            return formato.format(fecha)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun enviarSolicitud(curp:String, tokenKerkly:String, nombreKerkly: String,telefonoKerkly:String) {
         fechaHoraSolicitud = obtenerFechaFormateada(System.currentTimeMillis())
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
                    val cadena = "error"
                    if (cadena.equals(entrada)){
                       // setProgress.dialog.dismiss()
                        Toast.makeText(applicationContext,"$entrada", Toast.LENGTH_LONG).show()
                    }else{
                        insertarSolicitudFirebaseNormal(entrada.toInt(),"",problema,correoCliente, oficio,curp,false,fechaHoraSolicitud,latitud,longitud,false,tokenKerkly, nombreKerkly,telefonoKerkly)
                    }
                }

                override fun failure(error: RetrofitError?) {
                    println("error$error")
                    Toast.makeText(applicationContext, "error $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    fun insertarSolicitudFirebaseNormal(idGenerados: Int, pago_total: String, problema: String,
        correo: String, TipoServicio: String, idkerkly: String, clienteAcepta: Boolean, fechaHora: String,
        latitud: Double, longitud: Double, trabajoTerminado: Boolean, tokenKerkly:String, nombreKerkly:String,telefonoKerkly:String) {
        val llamartopico = llamarTopico()
                        val modelo = modeloSolicitudNormal(idGenerados, pago_total, problema,
                            correo, TipoServicio, idkerkly, clienteAcepta, fechaHora, latitud, longitud, trabajoTerminado)
                        val countersRef2 = instancias.referenciaSolicitudNormal(uidCliente).child(idGenerados.toString())
                        countersRef2.setValue(modelo) { error, _ ->
                            if (error == null) {
                                showMessaje("Solicitud Enviada")
                                val intent = Intent(applicationContext, SolicitarServicio::class.java)
                                b.putBoolean("PresupuestoListo", true)
                                intent.putExtras(b)
                                startActivity(intent)
                                llamartopico.llamarTopicEnviarSolicitudNormal(context, tokenKerkly,
                                    "Tienes una Solicitud de $nombreCliente, Problematica: $problema","$nombreCliente",
                                    latitud.toString(),longitud.toString(),idGenerados.toString(),direccion,telefonoCliente,Curp
                                    ,telefonoKerkly ,correoCliente,nombreKerkly,uidCliente,fechaHora,nombreOficio,problema)
                                finish()
                            } else {
                                // Manejar el error en caso de que ocurra
                                showMessaje("hubo un error ")
                            }
                        }

    }
    fun showMessaje(mensaje:String){
        Toast.makeText(this,mensaje,Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        finish()
    }


}