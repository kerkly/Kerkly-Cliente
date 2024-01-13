package com.example.kerklyv5.vista

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.*
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.example.kerklyv5.modelo.usuariosKerkly
import com.example.kerklyv5.notificaciones.llamarTopico
import com.example.kerklyv5.notificaciones.obtenerKerklys_y_tokens
import com.example.kerklyv5.url.Instancias
import com.example.kerklyv5.url.Url
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.BufferedReader
//import kotlinx.android.synthetic.main.confirmar_direccion.*
import java.io.IOException
import java.io.InputStreamReader
import java.sql.Date
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
   private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
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
    private lateinit var dialog: Dialog


    private lateinit var editCiudad: TextInputEditText
    private lateinit var edit_cp: TextInputEditText
    private lateinit var calle_edit: TextInputEditText
    private lateinit var colonia_edit: TextInputEditText
    private lateinit var numero_extEdit: TextInputEditText
    private lateinit var lyout_referencia: TextInputLayout
    private lateinit var boton: MaterialButton
    private lateinit var btn_direccion: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var uidKerkly:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser
        b = intent.extras!!
        context = this
        Url = Url()
        instancias = Instancias()
        dialog = Dialog(this)
        BotonEnviarU = findViewById(R.id.buttonEnviarUbicacion)
        progressBar = findViewById(R.id.progressBarMap)
        progressBar.visibility = View.VISIBLE
        BotonEnviarU.visibility = View.GONE
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

        //dialog
        dialog.setContentView(R.layout.confirmar_direccion)
        boton = dialog.findViewById(R.id.actualizar_btn)
        editCiudad = dialog.findViewById<TextInputEditText>(R.id.edit_ciudad)
        edit_cp = dialog.findViewById<TextInputEditText>(R.id.edit_codigoP)
        calle_edit = dialog.findViewById<TextInputEditText>(R.id.edit_calle)
        colonia_edit = dialog.findViewById<TextInputEditText>(R.id.edit_colonia)
        numero_extEdit = dialog.findViewById<TextInputEditText>(R.id.edit_numeroExt)
        edit_referecia = dialog.findViewById(R.id.edit_referencia)
        lyout_referencia = dialog.findViewById(R.id.layout_referencia)
        referencia = edit_referecia.text.toString()
        btn_direccion = dialog.findViewById(R.id.btn_direccion_exrpess)
        //termina

        BotonEnviarU.setOnClickListener {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            val gpsEnabled = locationManager!!. isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!gpsEnabled) {
                val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(settingsIntent)
                getLocalizacion()
            } else {
                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = connectivityManager.activeNetworkInfo

                if (networkInfo != null && networkInfo.isConnected) {
                    progressBar.visibility = View.VISIBLE
                    showMessaje("Por favor espere un momento....")
                    PoligonosColindantes(latitud, longitud, 1000.0)
                  //  MandarNoti("KJxpY59aB8gtXX96n4wdr7C9TBw1",problema,currentUser!!.displayName.toString(),)
               } else {
                    showMessage("No hay conexión a Internet")
                    progressBar.visibility = View.GONE
                }
            }
        }

        }


   /* private fun actualizar() {
        dialog.dismiss()
        showMessaje("Por favor espere un momento....")
        ciudad = editCiudad.text.toString()
       // estado = direccion[0].adminArea ?: "Sin nombre"
       // pais = direccion[0].countryName ?: "Sin Nombre"
        cp = edit_cp.text.toString()
        calle = calle_edit.text.toString()
        colonia = colonia_edit.text.toString()
        num_ext = numero_extEdit.text.toString()

       Direccion= "$pais $estado $ciudad $cp $colonia $calle $num_ext"
        PoligonosColindantes(latitud,longitud,1000.0)
        //calle_edit.setText(calle)
       ///colonia_edit.setText(colonia)
       // numero_extEdit.setText(num_ext)
        //edit_cp.setText(cp)
        //var ubicacion: String = "$ciudad, $estado, $pais"
      //  editCiudad.setText(ubicacion)
        //calle_edit.setText(calle)
        //colonia_edit.setText(colonia)
    }*/

    private fun showMessage(s: String) {
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!gpsEnabled) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
            //setProgress.dialog.dismiss()
        }else {
            // mMap.isMyLocationEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true

          /*  locationManager = this@MapsActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
            locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)*/

            locationCallback = object : LocationCallback(){
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation.let {
                        val miUbicacion = LatLng(it!!.getLatitude(), it.getLongitude())
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                        marcador = googleMap.addMarker(MarkerOptions().position(miUbicacion).draggable(true).title(nombreCliente.toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.miubicacion4)))!!

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 20F))
                        latitud = it.latitude
                        longitud = it.longitude
                        CrearPoligonoCircular(1000.0,latitud,longitud)
                        setLocation(latitud,longitud)
                    }
                }
            }

            // Solicitar actualizaciones de ubicación
            val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000) // Intervalo en milisegundos para recibir actualizaciones (5 segundos)
                .setFastestInterval(1000) // Intervalo mínimo en milisegundos entre actualizaciones ( segundos)

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
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
                    @RequiresApi(Build.VERSION_CODES.N)
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
                            val fechaHora = obtenerFechaFormateada(System.currentTimeMillis())
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
                                MandarNoti(kerkly.uidKerkly, problema, nombreCliente,fechaHora)
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
    @RequiresApi(Build.VERSION_CODES.N)
    private fun obtenerFechaFormateada(timestampMillis: Long): String {
        try {
            // Crea un objeto Date a partir del timestamp
            val fecha = Date(timestampMillis)

            // Formato deseado para la fecha y hora
            val formato = SimpleDateFormat("hh:mm a dd 'de' MMMM yyyy", Locale.getDefault())

            // Formatea la fecha y devuelve la cadena resultante
            return formato.format(fecha)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
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
            calle_edit.setText(calle)
            colonia_edit.setText(colonia)
            numero_extEdit.setText(num_ext)
            edit_cp.setText(cp)
            var ubicacion: String = "$ciudad, $estado, $pais"
            editCiudad.setText(ubicacion)
            calle_edit.setText(calle)
            colonia_edit.setText(colonia)


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
    private fun PoligonosColindantes(latitud: Double, longitud: Double, radio: Double) {
        try {
            val conexion = conexionPostgreSQL.obtenerConexion(this)
            conexion.use {
                if (conexion != null) {
                    val secciones = conexionPostgreSQL.poligonoCircular(latitud, longitud, radio)
                    kerklysCercanos = conexionPostgreSQL.Los5KerklyMasCercanos(secciones, longitud, latitud, oficio, this)

                    if (kerklysCercanos == null || kerklysCercanos!!.isEmpty()) {
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            BotonEnviarU.visibility = View.VISIBLE
                        }
                       showAlertDialog("Sin resultados", "No se encontraron kerklys cercanos en esta área. ¿Deseas aumentar el radio de búsqueda?",radio)
                    } else {
                        runOnUiThread {
                            BotonEnviarU.isEnabled = true
                            progressBar.visibility = View.GONE
                        }
                        if (!band) {
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
                            b.putString("direccion", Direccion)
                            b.putString("telefonoCliente", telefonoCliente)
                            b.putParcelableArrayList("kerklyCercanos", ArrayList(kerklysCercanos))
                            b.putString("nombreOficio", oficio)
                            i.putExtras(b)
                            startActivity(i)
                            finish()
                        } else {
                            conexionPostgreSQL.cerrarConexion()
                            ingresarPresupuesto()
                        }
                    }
                } else {
                    showMessage("Problemas de conexión")
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                    }
                }
            }
        } catch (e: Exception) {
            showMessage("Se produjo una excepción: ${e.message}")
            runOnUiThread {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showAlertDialog(title: String, message: String,radio: Double) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(title)
            builder.setCancelable(false)
            builder.setMessage(message)
            alertDialog = builder.create()
            builder.setPositiveButton("Sí") { _, _ ->
                progressBar.visibility = View.VISIBLE
                BotonEnviarU.isEnabled = true
                nuevoRadio = radio + 2000.0
                println("nuevo radio $nuevoRadio")
                if (nuevoRadio > 0) {
                    alertDialog?.dismiss()
                    CrearPoligonoCircular(nuevoRadio, latitud, longitud)
                    Handler(Looper.getMainLooper()).postDelayed({
                        showMessaje("Por favor espere")
                        PoligonosColindantes(latitud, longitud, nuevoRadio)
                    }, 5000)
                }
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                BotonEnviarU.isEnabled = true
                progressBar.visibility = View.GONE
            }
            builder.show()

    }



    private fun MandarNoti(uidKerkly: String, problema: String, nombreCliente:String,fechaHora: String){
        if (band == true){
            val llamartopico = llamarTopico()
            instancias = Instancias()
            val databaseUsu = instancias.referenciaInformacionDelKerkly(uidKerkly)
            databaseUsu.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null){
                        println("error null al obtener token")
                        progressBar.visibility = View.GONE
                    }else{
                        val u2 = snapshot.getValue(usuariosKerkly::class.java)
                        val curp = u2!!.curp
                       val tokenKerkly = u2!!.token
                        val telefonoKerkly = u2!!.telefono
                        val nombreK = u2!!.nombre
                        val correok = u2!!.correo
                        llamartopico.llamarTopicEnviarSolicitudUrgente(this@MapsActivity, tokenKerkly, "(Solicitud Urgente) $problema", "Mensaje de $nombreCliente",
                            latitud.toString(),longitud.toString(), folio, Direccion, telefonoCliente, curp,telefonoKerkly, correoCliente,correok, nombreK, currentUser!!.uid.toString(),problema,fechaHora, oficio)
                        progressBar.visibility = View.GONE
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    System.out.println("Firebase: $error")
                }

            })
        }else{
            showMessage("solo urgente ")
            progressBar.visibility  = View.GONE
        }


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
        progressBar.visibility  = View.GONE
        BotonEnviarU.visibility = View.VISIBLE
    }

    private fun eliminarPoligonoAnterior() {
        if (poligonoCircular != null) {
            poligonoCircular?.remove()
            poligonoCircular = null
        }
    }
}



