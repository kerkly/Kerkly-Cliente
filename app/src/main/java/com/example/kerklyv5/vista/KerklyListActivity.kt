package com.example.kerklyv5.vista

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.BaseDatosEspacial.Kerkly
import com.example.kerklyv5.BaseDatosEspacial.conexionPostgreSQL
import com.example.kerklyv5.BaseDatosEspacial.geom
import com.example.kerklyv5.R
import com.example.kerklyv5.SolicitarServicio
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.distancia_tiempo.CalcularTiempoDistancia
import com.example.kerklyv5.interfaces.IngresarPresupuestoClienteInterface
import com.example.kerklyv5.modelo.adapterUsuariosCercanos
import com.example.kerklyv5.modelo.serial.modeloSolicitudNormal
import com.example.kerklyv5.modelo.usuarios
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
import java.text.DateFormat
import java.util.ArrayList
import java.util.Date

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
        Miadapter = adapterUsuariosCercanos(this)
        recyclerview = findViewById(R.id.recycler_UsuariosPerfil)
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = Miadapter

        Miadapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                setScrollBar()
            }
        })

        telefono = b.get("Telefono").toString()
        oficio = b.getString("Oficio").toString()
        problema = b.get("Problema").toString()

        //modificacion
       // getKerklysCercanos()
        //metodo para obtener a los kerklys mas cercanos usando la base de datos espacial
        ObtenerKerklyMasCercanos()
    }
    private fun setScrollBar() {
        recyclerview.scrollToPosition(Miadapter.itemCount-1)
        // println("entro 217 "+ {Miadapter.itemCount-1 })
    }

   /* private fun getKerklysCercanos () {
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
    }*/


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
    private fun ObtenerKerklyMasCercanos(){
            // Muestra un ProgressDialog para indicar que se está cargando
            var arrayListPoligonoColindantes: ArrayList<geom> = ArrayList()
            //conexionPostgreSQL = conexionPostgreSQL()
            val conexion = conexionPostgreSQL.obtenerConexion(this)
            if (conexion != null) {
                // Llamada al método poligonoCircular con el callback
                val secciones=  conexionPostgreSQL.poligonoCircular(latitud, longitud, 3000.0, )
                val kerklysCercanos =  conexionPostgreSQL.Los5KerklyMasCercanos(secciones,longitud,latitud,oficio)
                // Ahora kerklysCercanos contiene la lista de los 5 Kerklys más cercanos
                if (kerklysCercanos.isEmpty()){
                    showMessaje("Lo sentimos pero en esta área no se encuentran kerklys cercanos")
                    setProgress.dialog.dismiss()
                    val intent = Intent(applicationContext, SolicitarServicio::class.java)
                    b.putBoolean("PresupuestoListo", true)
                    intent.putExtras(b)
                    startActivity(intent)
                    finish()
                }else {
                    postlist = kerklysCercanos.reversed() as ArrayList<Kerkly>
                   // var cont = 0
                    for (kerkly in kerklysCercanos.reversed()) {
                       // println("CURP: ${kerkly.idKerkly}, UID: ${kerkly.uidKerkly}, Distancia: ${kerkly.distancia}")
                        //println("Coordenadas: Latitud ${kerkly.latitud}, Longitud ${kerkly.longitud}")
                        conexionPostgreSQL.cerrarConexion()
                       //  latitud = 17.520514
                         //longitud = -99.463207
                        val url2 = instancias.CalcularDistancia(latitud, longitud, kerkly.latitud, kerkly.longitud)
                        CalcularTiempoDistancia(this@KerklyListActivity).execute(url2)
                      //  cont++
                        // MandarNoti(kerkly.uidKerkly, problema, nombreCliente)
                    }

                    //   insertarSolicitudFirebaseUrgente("0",problema,correoCliente,oficio,"",cur,,,)
                    //ingresarPresupuesto()
                 //   setProgress.dialog.dismiss()

                }

            }else {
                // Maneja el caso en el que la conexión no se pudo establecer
                showMessaje("problemas de conexión  ")
                setProgress.dialog.dismiss()
            }


    }

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
                                enviarSolicitud(curp,token)
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


    private fun enviarSolicitud(curp:String,tokenKerkly:String) {
        val fechaHora = DateFormat.getDateTimeInstance().format(Date())
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
                        insertarSolicitudFirebaseNormal(entrada.toInt(),"",problema,correoCliente, oficio,curp,false,fechaHora,latitud,longitud,false,tokenKerkly)
                    }
                }

                override fun failure(error: RetrofitError?) {
                    println("error$error")
                    Toast.makeText(applicationContext, "error $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    fun insertarSolicitudFirebaseNormal(idGenerados: Int,
        pago_total: String,
        problema: String,
        correo: String,
        TipoServicio: String,
        idkerkly: String,
        clienteAcepta: Boolean,
        fechaHora: String,
        latitud: Double,
        longitud: Double,
        trabajoTerminado: Boolean, tokenKerkly:String) {

                        val modelo = modeloSolicitudNormal(
                            idGenerados,
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
                        val countersRef2 = instancias.referenciaSolicitudNormal(uidCliente).child(idGenerados.toString())
                        countersRef2.setValue(modelo) { error, _ ->
                            if (error == null) {
                                showMessaje("Solicitud Enviada")
                                val intent = Intent(applicationContext, SolicitarServicio::class.java)
                                b.putBoolean("PresupuestoListo", true)
                                intent.putExtras(b)
                                startActivity(intent)
                                val llamartopico = llamarTopico()
                                llamartopico.llamartopico(context, tokenKerkly,  "$problema", "Tienes una Solicitud de $nombreCliente")
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
}