package com.example.kerklyv5.vista

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import android.util.Log
import android.widget.Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mAuth = FirebaseAuth.getInstance()
        b = intent.extras!!
        context = this
        Url = Url()
        instancias = Instancias()
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

                    }else{
                    if (!band) {
                        //val latitud = 17.536212
                        //val longitud = -99.495486

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
                    } else {
                        showMessaje("Por vafor espere un momento, No salga de la app..")
                        println("marcador debde ser $latitud $longitud")
                      //  setLocation(latitud, longitud)
                        //PoligonosColindantes(latitud, longitud)
                        val handlerThread = HandlerThread("obtenerKerklyNormal")
                        handlerThread.start()
                        handler = Handler(handlerThread.looper)
                        handler?.post({
                            ingresarPresupuesto()
                        })

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
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
                    //  val miUbicacion = LatLng(17.5551109, -99.5042626)

                    locationManager!!.removeUpdates(this)
                    marcador = googleMap.addMarker(MarkerOptions().position(miUbicacion).draggable(true).title(nombreCliente.toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.miubicacion4)))!!

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 20F))
                    latitud = location.latitude
                    longitud = location.longitude
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
                            PoligonosColindantes(latitud, longitud)

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
            setLocation(latitud,longitud)

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
           // setLocation(latitud,longitud)
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
        if (latitud != 0.0 && longitud != 0.0) {
            try {
                val geocoder: Geocoder
                val direccion: List<Address>
                geocoder = Geocoder(this, Locale.getDefault())
                direccion = geocoder.getFromLocation(latitud, longitud, 1)!!

                // Verificar si hay al menos un resultado
                if (direccion.isNotEmpty()) {
                    val address = direccion[0]

                    if (address.locality == null || address.subLocality == null || address.thoroughfare == null) {
                        pais = address.countryName
                        estado = address.adminArea
                        ciudad = "Sin nombre"
                        colonia = "Sin nombre"
                        calle = "Sin Nombre"
                        cp = "NULL"
                        num_ext = "Sin número"
                        Direccion = "$calle $colonia $num_ext $cp"
                    } else {
                        ciudad = address.locality // ciudad
                        estado = address.adminArea //estado
                        pais = address.countryName // pais
                        cp = address.postalCode //codigo Postal
                        calle = address.thoroughfare // la calle
                        colonia =  address.subLocality// colonia
                        num_ext = address.subThoroughfare
                        Direccion = "$calle $colonia $num_ext $cp"
                    }
                } else {
                    // Manejar el caso cuando no hay resultados de la geolocalización
                    // ...
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


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

    private fun PoligonosColindantes(latitud: Double, longitud: Double) {
        try {
            // Muestra un ProgressDialog para indicar que se está cargando
            var arrayListPoligonoColindantes: ArrayList<geom> = ArrayList()

            val conexion = conexionPostgreSQL.obtenerConexion(this)
            conexion.use { // Esto garantiza que la conexión se cierre correctamente al salir del bloque
                if (conexion != null) {
                    val secciones = conexionPostgreSQL.poligonoCircular(latitud, longitud, 1000.0)
                    val kerklysCercanos = conexionPostgreSQL.Los5KerklyMasCercanos(secciones, longitud, latitud, oficio)

                    if (kerklysCercanos == null || kerklysCercanos.isEmpty()) {
                        showMessage("Lo sentimos, pero en esta área no se encuentran kerklys cercanos")
                        conexionPostgreSQL.cerrarConexion()
                        handler?.looper?.quitSafely()
                    } else {
                       // println("foliooo en PoligonosColindantes ------> $folio")
                        for (kerkly in kerklysCercanos.reversed()) {
                         //   println("CURP: ${kerkly.idKerkly}, UID: ${kerkly.uidKerkly}, Distancia: ${kerkly.distancia}")
                         //   println("Coordenadas: Latitud ${kerkly.latitud}, Longitud ${kerkly.longitud}")
                            MandarNoti(kerkly.uidKerkly, problema, nombreCliente)
                        }
                        // insertarSolicitudFirebaseUrgente("0",problema,correoCliente,oficio,"",cur,,,)
                        conexionPostgreSQL.cerrarConexion()
                        val fechaHora = DateFormat.getDateTimeInstance().format(Date())
                        insertarSolicitudFirebaseUrgente(folio,"",problema,correoCliente,oficio,"",fechaHora,latitud,longitud)

                    }
                } else {
                    // Maneja el caso en el que la conexión no se pudo establecer
                    showMessage("Problemas de conexión")
                    handler?.looper?.quitSafely()
                }
            }
        } catch (e: Exception) {
            // Maneja excepciones específicas según tu lógica de manejo de errores
            e.printStackTrace()
            showMessage("Error: ${e.message}")
            handler?.looper?.quitSafely()

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



}



