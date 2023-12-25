package com.example.kerklyv5.vista

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kerklyv5.BaseDatosEspacial.conexionPostgreSQL
import com.example.kerklyv5.BaseDatosEspacial.geom
import com.example.kerklyv5.R
import com.example.kerklyv5.SolicitarServicio
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.interfaces.IngresarPresupuestoUrgente
import com.example.kerklyv5.modelo.modeloSolicituUrgente
import com.example.kerklyv5.modelo.modelokerklyCercanos
import com.example.kerklyv5.modelo.serial.Kerkly
import com.example.kerklyv5.modelo.usuarios
import com.example.kerklyv5.notificaciones.obtenerKerklys_y_tokens
import com.example.kerklyv5.url.Instancias
import com.example.kerklyv5.url.Url

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.BufferedReader
//import kotlinx.android.synthetic.main.confirmar_direccion.*
import java.io.IOException
import java.io.InputStreamReader
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener{
    private lateinit var mMap: GoogleMap
    private lateinit var marcador: Marker
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    lateinit var BotonEnviarU: Button
    private var tipo = 1
    lateinit var mapa: String
    private lateinit var b: Bundle
    private lateinit var problema: String
    private lateinit var telefonoCliente: String
    private lateinit var oficio: String
    private lateinit var edit_referecia: TextInputEditText

    private   var ciudad: String = ""
    private lateinit var calle: String
    private lateinit var cp: String
    private lateinit var colonia: String
    private lateinit var num_ext: String
    private lateinit var referencia: String
    private  var estado: String = ""
    private lateinit var pais: String
    private var band = false
    private lateinit var nombreCliente: String
    var folio =""
    private var locationManager: LocationManager? = null
    private lateinit var context: Context
    private lateinit var arrayListTiempo: ArrayList<modelokerklyCercanos>
    private lateinit var arraylistUsuarios: ArrayList<usuarios>
    private lateinit var correoCliente:String
    private lateinit var instancias: Instancias
    private lateinit var uid: String
    private lateinit var conexionPostgreSQL: conexionPostgreSQL
    private lateinit var  obtenerkerklysYTokens: obtenerKerklys_y_tokens
    private lateinit var Url: Url
    private lateinit var Direccion:String

    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var handler: Handler? = null
   // private lateinit var Noti:String

  /*  private lateinit var progressDialog: ProgressDialog
    private lateinit var progressBar: ProgressBar*/
  private var poligonoCircular: Polygon? = null
    private  var nuevoRadio= 0.0
    private  var kerklysCercanos: MutableList<com.example.kerklyv5.BaseDatosEspacial.Kerkly>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mAuth = FirebaseAuth.getInstance()
        b = intent.extras!!
        context = this
        Url = Url()
        instancias = Instancias()
        // Inicializar ProgressDialog y ProgressBar
      /*  progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Cargando...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setCancelable(false)*/

        /*progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        progressDialog.max = 100*/
        arrayListTiempo = ArrayList<modelokerklyCercanos>()
        arraylistUsuarios = ArrayList<usuarios>()
        obtenerkerklysYTokens =  obtenerKerklys_y_tokens()
        conexionPostgreSQL = conexionPostgreSQL()
        band = b.getBoolean("Express")
        nombreCliente = b.getString("Nombre")!!
        uid = b.getString("uid")!!
        correoCliente = b.getString("correo")!!
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getLocalizacion()
        telefonoCliente = b.get("Telefono").toString()
        oficio = b.getString("Oficio").toString()
        problema = b.get("Problema").toString()
       // BotonT = findViewById(R.id.button2)
/*        BotonT.setOnClickListener {
            when(tipo) {
                1 -> Hibrido()
                2 -> satelite()
                3 -> terreno()
                4 -> terreno2()
                else-> print(" ")
            }
            //Toast.makeText(applicationContext, mapa, Toast.LENGTH_LONG).show()

        }*/
        BotonEnviarU = findViewById(R.id.buttonEnviarUbicacion)
        BotonEnviarU.setOnClickListener {
            //    setProgress.setProgressDialog(this@MapsActivity)
                locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (!gpsEnabled) {
                    val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(settingsIntent)
                    // setProgress.dialog.dismiss()
                    getLocalizacion()
                } else {
                    if (latitud == 0.0 && longitud ==0.0){
                        handler?.looper?.quitSafely()
                    }else{
                  /*  if (!band) {
                        //val latitud = 17.536212
                        //val longitud = -99.495486
                        setLocation(latitud,longitud)
                     /*   val i = Intent(applicationContext, KerklyListActivity::class.java)
                        correoCliente = b.getString("correo").toString()
                        b.putString("correo", correoCliente)
                        b.putString("Calle", calle)
                        b.putString("Colonia", colonia)
                        b.putString("Código Postal", cp)
                        b.putString("Exterior", num_ext)
                        b.putDouble("Latitud", latitud)
                        b.putDouble("Longitud", longitud)
                        b.putString("Ciudad", ciudad)
                        b.putString("Estado", estado)
                        b.putString("Pais", pais)
                        b.putString("nombreCliente", nombreCliente)
                        b.putString("uid", uid)
                        b.putString("direccion",Direccion)
                        b.putString("telefonoCliente",telefonoCliente)
                        i.putExtras(b)
                        startActivity(i)
                        finish()*/
                    } else {
                     //   val handlerThread = HandlerThread("obtenerKerklyUrgente")
                     //   handlerThread.start()
                      //  handler = Handler(handlerThread.looper)
                       // handler?.post({
                           // ingresarPresupuesto()

                    //    })

                    }*/
                        showMessaje("Por favor espere un momento....")
                        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        val networkInfo = connectivityManager.activeNetworkInfo

                        if (networkInfo != null && networkInfo.isConnected) {
                            // Realizar operaciones que requieren conexión
                            PoligonosColindantes(latitud,longitud,1000.0)
                        } else {
                            // No hay conexión disponible, muestra un mensaje al usuario
                            showMessage("No hay conexión a Internet")
                        }

                    }
                }
        }
    }
    private fun showMessage(s: String) {
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!gpsEnabled) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
            //setProgress.dialog.dismiss()
        }else {
            // mMap.isMyLocationEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true

            locationManager = this@MapsActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationListener: LocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                   val miUbicacion = LatLng(location.getLatitude(), location.getLongitude())
                     //val miUbicacion = LatLng(  17.536558 ,-99.495811)

                    locationManager!!.removeUpdates(this)
                    marcador = googleMap.addMarker(MarkerOptions().position(miUbicacion).draggable(true).title(nombreCliente.toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.miubicacion4)))!!

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 20F))
                    latitud = location.latitude
                    longitud = location.longitude
                    CrearPoligonoCircular(1000.0,latitud,longitud)
                    setLocation(latitud,longitud)
                    Log.d("linea 201 ", "Ubicacion actual, $latitud, $longitud")
                }
                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                    when (status) {
                        LocationProvider.AVAILABLE -> Log.d("debug", "LocationProvider.AVAILABLE")
                        LocationProvider.OUT_OF_SERVICE -> Log.d(
                            "debug",
                            "LocationProvider.OUT_OF_SERVICE"
                        )
                        LocationProvider.TEMPORARILY_UNAVAILABLE -> Log.d(
                            "debug",
                            "LocationProvider.TEMPORARILY_UNAVAILABLE"
                        )
                    }
                }
                override fun onProviderEnabled(provider: String) {
                    Toast.makeText(this@MapsActivity, "GPS activado", Toast.LENGTH_SHORT).show()
                   // setProgress.dialog.dismiss()
                }
                override fun onProviderDisabled(provider: String) {
                   // setProgress.dialog.dismiss()
                    Toast.makeText(this@MapsActivity, "GPS Desactivado", Toast.LENGTH_SHORT).show()
                    locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val gpsEnabled =
                        locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    if (!gpsEnabled) {
                        val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(settingsIntent)
                    }
                }
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)

                googleMap.setOnMarkerClickListener(this)
                googleMap.setOnMarkerDragListener(this)


        }
    }

    private fun getLocalizacion() {
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permiso == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                finish()
            }
        }
    }

    fun aceptarDireccion() {
        referencia = edit_referecia.text.toString()
        //dialog.dismiss()
    }

    private fun Hibrido() {
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mapa = "HÍBRIDO";
        tipo = 2;
    }
    private fun satelite() {
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mapa = "SATÉLITE";
        tipo =3;
    }
    private fun terreno() {
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mapa = "TERRENO";
        tipo = 4;
    }
    private fun terreno2() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapa = "TERRENO";
        tipo = 1;
    }



    private fun ingresarPresupuesto(){
            val ROOT_URL = Url().url
            val adapter = RestAdapter.Builder()
                .setEndpoint(ROOT_URL)
                .build()
            val api = adapter.create(IngresarPresupuestoUrgente::class.java)
            api.presupuesto_urgente(problema,
                telefonoCliente,
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
                        //Toast.makeText(applicationContext, "Entre por aqui", Toast.LENGTH_LONG).show()
                        // Toast.makeText(applicationContext, entrada, Toast.LENGTH_LONG).show()
                        val cadena = "error"
                        if (cadena.equals(entrada)) {
                            Toast.makeText(applicationContext, "Peticion no  enviada, $entrada", Toast.LENGTH_LONG) .show()
                        }else{
                            folio = entrada
                           // PoligonosColindantes(latitud, longitud)
                            val fechaHora = DateFormat.getDateTimeInstance().format(Date())
                            insertarSolicitudFirebaseUrgente(folio,"",problema,correoCliente,oficio,"",fechaHora,latitud,longitud)
                            for (kerkly in kerklysCercanos!!.reversed()) {
                                println("CURP: ${kerkly.idKerkly}, UID: ${kerkly.uidKerkly}, Distancia: ${kerkly.distancia}")
                                println("Coordenadas: Latitud ${kerkly.latitud}, Longitud ${kerkly.longitud}")
                                /*  val marker = mMap.addMarker(
                                      MarkerOptions()
                                          .position(LatLng(kerkly.latitud, kerkly.longitud))
                                          .title("kerkly ")
                                          .snippet("Distancia: ${kerkly.distancia}")
                                          .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                  )*/
                                /*   val miUbicacion = LatLng(kerkly.latitud, kerkly.longitud)
                                   marcador = mMap.addMarker(MarkerOptions().position(miUbicacion).draggable(true).title("Kerklys").icon(BitmapDescriptorFactory.fromResource(R.drawable.iconocliente)))!!
                                   mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 10F))*/
                                MandarNoti(kerkly.uidKerkly, problema, nombreCliente)
                                //showMessaje("CURP: ${kerkly.idKerkly}, UID: ${kerkly.uidKerkly}, Distancia: ${kerkly.distancia}")
                            }

                        }
                    }
                    override fun failure(error: RetrofitError?) {
                        println("error $error")
                        Toast.makeText(applicationContext, "error $error", Toast.LENGTH_LONG).show()
                    }
                }
            )
    }
    override fun onMarkerDragEnd(p0: Marker) {
        if (p0.equals(marcador)) {

            latitud = p0.position.latitude
            longitud = p0.position.longitude
           // setLocation(latitud,longitud)
            CrearPoligonoCircular(1000.0,latitud,longitud)
            Log.d("onMarkerDragEnd ", "Ubicacion onMarkerDragEnd, $latitud, $longitud")
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        if (p0!!.equals(marcador!!)) {
            //  System.out.println("entro")
            latitud = p0.position.latitude
            longitud = p0.position.longitude
           // setLocation(latitud,longitud)
            Log.d("onMarkerClick ", "Ubicacion onMarkerClick, $latitud, $longitud")
        }
        return false
    }

    override fun onMarkerDragStart(p0: Marker) {
        if (p0.equals(marcador)) {
            //Toast.makeText(context, "start", Toast.LENGTH_SHORT).show()
            latitud = p0.position.latitude
            longitud = p0.position.longitude
            setLocation(latitud,longitud)
            eliminarPoligonoAnterior()
            Log.d("onMarkerDragStart ", "Ubicacion onMarkerDragStart, $latitud, $longitud")

        }
    }
    override fun onMarkerDrag(p0: Marker) {
        if (p0!!.equals(marcador!!)){
            latitud = p0.position.latitude
            longitud = p0.position.longitude
            Log.d("onMarkerDrag ", "Ubicacion onMarkerDrag, $latitud, $longitud")
        }
    }

    fun setLocation(latitud: Double, longitud: Double) {
        try {
            val geocoder: Geocoder
            val direccion: List<Address>
            geocoder = Geocoder(this, Locale.getDefault())

            direccion = geocoder.getFromLocation(latitud, longitud, 1)!! // 1 representa la cantidad de resultados a obtener
                ciudad = direccion[0].locality ?: "Sin nombre"
                estado = direccion[0].adminArea ?: "Sin nombre"
                pais = direccion[0].countryName ?: "Sin Nombre"
                cp = direccion[0].postalCode ?: "NULL"
                calle = direccion[0].thoroughfare ?: "Sin Nombre"
                colonia = direccion[0].subLocality ?: "Sin Nombre"
                num_ext = direccion[0].subThoroughfare ?: "Sin número"

            Direccion= "$pais $estado $ciudad $cp $colonia $calle $num_ext"

        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("direccion","No se pudo Obtener la dirección ${e.message}")
        }

}



  /*  private fun handleAddressResult(address: Address) {
        ciudad = address.locality ?: "Sin nombre"
        estado = address.adminArea ?: "Sin nombre"
        pais = address.countryName ?: "Sin nombre"
        cp = address.postalCode ?: "NULL"
        calle = address.thoroughfare ?: "Sin Nombre"
        colonia = address.subLocality ?: "Sin nombre"
        num_ext = address.subThoroughfare ?: "Sin número"
        Direccion = "$calle $colonia $num_ext $cp"
        if (!band){
            val i = Intent(applicationContext, KerklyListActivity::class.java)
            correoCliente = b.getString("correo").toString()
            b.putString("correo", correoCliente)
            b.putString("Calle", calle)
            b.putString("Colonia", colonia)
            b.putString("Código Postal", cp)
            b.putString("Exterior", num_ext)
            b.putDouble("Latitud", latitud)
            b.putDouble("Longitud", longitud)
            b.putString("Ciudad", ciudad)
            b.putString("Estado", estado)
            b.putString("Pais", pais)
            b.putString("nombreCliente", nombreCliente)
            b.putString("uid", uid)
            b.putString("direccion",Direccion)
            b.putString("telefonoCliente",telefonoCliente)
            i.putExtras(b)
            startActivity(i)
            finish()
        }else{
            println("entroooo")
            ingresarPresupuesto()
        }


    }*/


    fun insertarSolicitudFirebaseUrgente(idgenerado:String,
        pago_total: String,
        problema: String,
        correo: String,
        TipoServicio: String,
        idKerklyAcepto: String,
        fechaHora: String,
        latitud: Double,
        longitud: Double
    ) {
        //val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
        val reference = instancias.referenciaSolicitudUrgente(uid)
                         val modelo = modeloSolicituUrgente(
                             idgenerado.toInt(),
                                               pago_total,
                                               problema,
                                               correo,
                                               TipoServicio,
                                               idKerklyAcepto,
                                                fechaHora,
                                               latitud,
                                               longitud,false
                                           )
                        val countersRef2 = instancias.referenciaSolicitudUrgente(uid).child(idgenerado.toString())
                        countersRef2.setValue(modelo) { error, _ ->
                                               if (error == null) {
                                                   handler?.looper?.quitSafely()
                                                   showMessaje("Solicitud Enviada")
                                                   val intent = Intent(applicationContext, SolicitarServicio::class.java)
                                                   b.putBoolean("PresupuestoListo", true)
                                                   intent.putExtras(b)
                                                   startActivity(intent)
                                                   finish()
                                               } else {
                                                   // Manejar el error en caso de que ocurra
                                                   showMessaje("hubo un error ")
                                                   handler?.looper?.quitSafely()
                                               }
                                           }

    }


    fun showMessaje(mensaje:String){
        Toast.makeText(this,mensaje,Toast.LENGTH_SHORT).show()
    }

 var alertDialog: AlertDialog? = null
    private fun PoligonosColindantes(latitud: Double, longitud: Double,radio:Double){
        // Deshabilitar el botón
        runOnUiThread {
            BotonEnviarU.isEnabled = false
            BotonEnviarU.invalidate()  // O BotonEnviarU.requestLayout()
        }

        try {
            val conexion = conexionPostgreSQL.obtenerConexion(this)
            conexion.use { // Esto garantiza que la conexión se cierre correctamente al salir del bloque
                if (conexion != null) {
                    val startTime = System.currentTimeMillis()
                    // Simula tiempo de consulta
                    Thread.sleep(2000) // 2000 milisegundos (2 segundos) como ejemplo, ajusta según necesites
                    val endTime = System.currentTimeMillis()
                    val elapsedTime = endTime - startTime
                    try {
                        try {
                            val secciones = conexionPostgreSQL.poligonoCircular(latitud, longitud, radio)
                             kerklysCercanos = conexionPostgreSQL.Los5KerklyMasCercanos(secciones, longitud, latitud, oficio,this)

                    if (kerklysCercanos == null || kerklysCercanos!!.isEmpty()) {

                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Sin resultados")
                        builder.setCancelable(false)
                        builder.setMessage("No se encontraron kerklys cercanos en esta área. ¿Deseas aumentar el radio de búsqueda?")
                         alertDialog = builder.create()
                        builder.setPositiveButton("Sí") { _, _ ->
                            BotonEnviarU.isEnabled = true
                             nuevoRadio = radio + 2000.0
                            println("nuevo radio $nuevoRadio")
                            if (nuevoRadio > 0) {
                                alertDialog?.dismiss()
                                CrearPoligonoCircular(nuevoRadio,latitud,longitud)

                                // Agregar un retraso de 2 segundos (ajusta según sea necesario)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    showMessaje("Por favor espere")
                                    PoligonosColindantes(latitud, longitud, nuevoRadio)
                                }, 5000)
                            } else {
                                // Muestra un mensaje si el nuevo radio no es válido
                                //showMessage("Por favor, ingresa un radio válido mayor que cero.")
                            }
                        }
                        builder.setNegativeButton("No") { dialog, _ ->
                            // El usuario ha seleccionado no aumentar el radio de búsqueda, puedes realizar las acciones correspondientes aquí
                            dialog.dismiss()
                            BotonEnviarU.isEnabled = true
                        }
                        builder.show()

                      //  conexionPostgreSQL.cerrarConexion()
                      //  handler?.looper?.quitSafely()
                      //  progressDialog.dismiss()
                    } else {
                        BotonEnviarU.isEnabled = true

                        alertDialog?.dismiss()
                        if (!band){
                            val i = Intent(applicationContext, KerklyListActivity::class.java)
                            correoCliente = b.getString("correo").toString()
                            b.putString("correo", correoCliente)
                            b.putString("Calle", calle)
                            b.putString("Colonia", colonia)
                            b.putString("Código Postal", cp)
                            b.putString("Exterior", num_ext)
                            b.putDouble("Latitud", latitud)
                            b.putDouble("Longitud", longitud)
                            b.putString("Ciudad", ciudad)
                            b.putString("Estado", estado)
                            b.putString("Pais", pais)
                            b.putString("nombreCliente", nombreCliente)
                            b.putString("uid", uid)
                            b.putString("direccion",Direccion)
                            b.putString("telefonoCliente",telefonoCliente)
                            b.putParcelableArrayList("kerklyCercanos",ArrayList(kerklysCercanos))
                            i.putExtras(b)
                            startActivity(i)
                            finish()
                        }else{
                       // println("foliooo en PoligonosColindantes ------> $folio")
                        conexionPostgreSQL.cerrarConexion()
                            ingresarPresupuesto()
                          //  showMessage("todo bien")
                        }
                    }
                        } catch (e: Exception) {
                            // Aquí manejas la excepción
                            showMessage("Se produjo una excepción: ${e.message}")
                        }
                    } finally {
                        conexionPostgreSQL.cerrarConexion()
                    }
                } else {
                    // Maneja el caso en el que la conexión no se pudo establecer
                    showMessage("Problemas de conexión")
                   // handler?.looper?.quitSafely()
                   // progressDialog.dismiss()
                }
            }
        } catch (e: Exception) {
                showMessage("Error: ${e.message}")
            println("Error: ${e.message}")
        }finally {
               // progressDialog.dismiss()
        }

    }


    private fun MandarNoti(uidKerkly: String, problema: String, nombreCliente:String){
        currentUser = mAuth!!.currentUser
        obtenerkerklysYTokens.obtenerTokenKerklySolicitudUrgente(uidKerkly,this, problema,nombreCliente,
            latitud.toString(),longitud.toString(),folio, Direccion, telefonoCliente, correoCliente, currentUser!!.uid)
        //val llamarTopico  = llamarTopico()
       // llamarTopico.llamarTopicEnviarSolicitudUrgente(this, tokenKerkly, problema,nombreCliente, folio)
       // showMessaje("todo bien CURP: $uid")
    }


    fun CrearPoligonoCircular(diametro: Double,latitud: Double,longitud: Double){
        eliminarPoligonoAnterior()
        val marcadorSeleccionado = LatLng(latitud, longitud)
        // Número de puntos para formar el círculo
        val numPuntos = 360
        // Lista para almacenar las coordenadas del círculo
        val circlePoints = mutableListOf<LatLng>()
        // Calcular y agregar las coordenadas del círculo
        for (i in 0 until numPuntos) {
            val angle = 360.0 / numPuntos * i
            val x = diametro / 2 * Math.cos(Math.toRadians(angle))
            val y = diametro / 2 * Math.sin(Math.toRadians(angle))
            val lat = marcadorSeleccionado.latitude + y / 111111.0 // Conversión aproximada de grados a metros
            val lng = marcadorSeleccionado.longitude + x / (111111.0 * Math.cos(Math.toRadians(marcadorSeleccionado.latitude)))
            circlePoints.add(LatLng(lat, lng))
        }
        // Crear y añadir el polígono al mapa
        val polygonOptions = PolygonOptions()
        polygonOptions.addAll(circlePoints)
         poligonoCircular = mMap.addPolygon(polygonOptions)
    }

    private fun eliminarPoligonoAnterior() {
        if (poligonoCircular != null) {
            poligonoCircular?.remove()
            poligonoCircular = null
        }
    }
}



