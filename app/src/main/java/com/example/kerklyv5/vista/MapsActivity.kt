package com.example.kerklyv5.vista

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kerklyv5.R
import com.example.kerklyv5.SolicitarServicio
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.distancia_tiempo.CalcularTiempoDistancia
import com.example.kerklyv5.interfaces.IngresarPresupuestoUrgente
import com.example.kerklyv5.interfaces.ObtenerKerklyInterface
import com.example.kerklyv5.modelo.modeloSolicituUrgente
import com.example.kerklyv5.modelo.modelokerklyCercanos
import com.example.kerklyv5.modelo.serial.Kerkly
import com.example.kerklyv5.modelo.usuarios
import com.example.kerklyv5.notificaciones.llamarTopico
import com.example.kerklyv5.notificaciones.obtenerKerklys_y_tokens
import com.example.kerklyv5.url.Instancias
import com.example.kerklyv5.url.Url

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.google.gson.GsonBuilder
//import kotlinx.android.synthetic.main.confirmar_direccion.*
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
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, CalcularTiempoDistancia.Geo {

    private lateinit var mMap: GoogleMap
    private lateinit var marcador: Marker
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    lateinit var BotonT: Button
    lateinit var BotonEnviarU: Button
    private var tipo = 1
    lateinit var mapa: String
    private lateinit var b: Bundle
    private lateinit var curp: String
    private lateinit var problema: String
    private lateinit var telefono: String
    private lateinit var oficio: String
    private lateinit var dialog: Dialog
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
    private var locationManager: LocationManager? = null
   // val setProgress = setProgressDialog()

    private var postlist: java.util.ArrayList<Kerkly>? =null
    private lateinit var context: Context
    private var i2: Int? = 0
    private lateinit var token: String
    private lateinit var arrayListTiempo: ArrayList<modelokerklyCercanos>
    private lateinit var arraylistUsuarios: ArrayList<usuarios>
   private lateinit var correoCliente:String
   private lateinit var instancias: Instancias
    private lateinit var uid: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        b = intent.extras!!
       // setProgress.setProgressDialog(this)
       // dialog = Dialog(this)
        context = this
        instancias = Instancias()
        arrayListTiempo = ArrayList<modelokerklyCercanos>()
        arraylistUsuarios = ArrayList<usuarios>()
        /* curp = b.getString("Curp Kerkly").toString()
         problema = b.getString("Problema").toString()
         telefono = b.getString("Telefono").toString()
         oficio = b.getString("Oficio").toString()*/

        band = b.getBoolean("Express")
        nombreCliente = b.getString("Nombre")!!
        uid = b.getString("uid")!!
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // val bandK = b.getBoolean("Ker")
            getLocalizacion()
        telefono = b.get("Telefono").toString()
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

      locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!gpsEnabled) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
            //setProgress.dialog.dismiss()
            getLocalizacion()
        }else {
            setLocation(latitud, longitud)
            if (!band) {
                val i = Intent(applicationContext, KerklyListActivity::class.java)
                correoCliente =  b.get("correoCliente").toString()
                b.putString("correoCliente",correoCliente)
                b.putString("Calle", calle)
                    b.putString("Colonia", colonia)
                    b.putString("Código Postal", cp)
                    b.putString("Exterior", num_ext)
                    // b.putString("Referencia", referencia)
                    b.putDouble("Latitud", latitud)
                    b.putDouble("Longitud", longitud)
                    b.putString("Ciudad", ciudad)
                    b.putString("Estado", estado)
                    b.putString("Pais", pais)
                    b.putString("nombreCliente", nombreCliente)
                    b.putString("uid",uid)
                    i.putExtras(b)
                    startActivity(i)
                    finish()

            } else {
                setLocation(latitud, longitud)
                //Obetener los kerlys cercanos
                if (latitud == 0.0 || longitud == 0.0){
                    Toast.makeText(this, "Por favor Actualice su Ubicacion",Toast.LENGTH_SHORT).show()
                }else {
                    recorrerLista()
                }
                // System.out.println("el token del kerkly " + u2!!.token[i])
                // llamartopico.llamartopico(context, token, "(Servicio Normal) $problema", "Usuario Nuevo-> $nombreCliente")

            }

        }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

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
                    marcador = googleMap.addMarker(
                        MarkerOptions().position(miUbicacion).draggable(true)
                            .title(nombreCliente.toString()).icon(
                            BitmapDescriptorFactory.fromResource(
                                R.drawable.miubicacion4
                            )
                        )
                    )!!

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 20F))
                    latitud = location.latitude
                    longitud = location.longitude
                   // setProgress.dialog.dismiss()
                    getKerklys()

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
            locationManager!!.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0f,
                locationListener
            )
            //seccion donde se encuentran los mappas
            //los marcadores
            // Utils_k.Marcador(mMap, applicationContext, telefono)
            //  mMap!!.setOnMapLongClickListener(this)
            googleMap.setOnMarkerClickListener(this)
            googleMap.setOnMarkerDragListener(this)
            // mMap!!.setOnMarkerClickListener(this)
        }
    }

    private fun getLocalizacion() {
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permiso == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
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
    override fun onMarkerClick(p0: Marker): Boolean {
        if (p0!!.equals(marcador!!)) {
          //  System.out.println("entro")
            latitud = p0.position.latitude
            longitud = p0.position.longitude
        }
        return false
    }

    private fun ingresarPresupuesto() {
            val ROOT_URL = Url().url
            val adapter = RestAdapter.Builder()
                .setEndpoint(ROOT_URL)
                .build()
            val api = adapter.create(IngresarPresupuestoUrgente::class.java)
            api.presupuesto_urgente(problema,
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

                        val cadena = "1"
                        if (cadena.equals(entrada)) {
                            Toast.makeText(applicationContext, "Peticion enviada, por favor espere un momento.....", Toast.LENGTH_LONG) .show()
                            val intent = Intent(applicationContext, SolicitarServicio::class.java)
                            b.putBoolean("PresupuestoListo", true)
                            intent.putExtras(b)
                            startActivity(intent)
                            finish()

                        }else{
                            Toast.makeText(applicationContext, "Peticion no  enviada, $entrada-->", Toast.LENGTH_LONG) .show()
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
            // Toast.makeText(context, "finish", Toast.LENGTH_SHORT).show()
            latitud = p0.position.latitude
            longitud = p0.position.longitude

        }
    }

    override fun onMarkerDragStart(p0: Marker) {
        if (p0.equals(marcador)) {
            //Toast.makeText(context, "start", Toast.LENGTH_SHORT).show()
            latitud = p0.position.latitude
            longitud = p0.position.longitude

        }
    }
    override fun onMarkerDrag(p0: Marker) {
        if (p0!!.equals(marcador!!)){
           // val nuevoTitulo  = String.format(Locale.getDefault(),getString(R.string.marker_detail), marcador.position.latitude, marcador.position.longitude)
          //setTitle(nuevoTitulo)
            latitud = p0.position.latitude
            longitud = p0.position.longitude
            //setLocation(latitud, longitud)

        }
    }

    fun setLocation(latitud: Double, longitud: Double) {
        if (latitud != 0.0 && longitud != 0.0) {
            try {
                val geocoder: Geocoder
                val direccion: List<Address>
                geocoder = Geocoder(this, Locale.getDefault())

                direccion =
                    geocoder.getFromLocation(latitud, longitud, 1)!! // 1 representa la cantidad de resultados a obtener
                //val address = direccion[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                //Toast.makeText(this, "Entroooo ${direccion[0].locality}", Toast.LENGTH_SHORT).show()
                if (direccion[0].locality == null || direccion[0].subLocality == null || direccion[0].thoroughfare == null){
                  //  Toast.makeText(this, "Entroooo ${direccion[0].postalCode}", Toast.LENGTH_SHORT).show()
                    pais = direccion[0].countryName
                    estado = direccion[0].adminArea
                    ciudad = "Sin nombre"
                    colonia = "Sin nombre"
                    calle = "Sin Nombre"
                    cp = "NULL"
                    num_ext = "Sin número"
                }else{
                   ciudad = direccion[0].locality // ciudad
                     estado = direccion[0].adminArea //estado
               pais = direccion[0].countryName // pais
               cp = direccion[0].postalCode //codigo Postal
               calle = direccion[0].thoroughfare // la calle
               colonia =  direccion[0].subLocality// colonia
               num_ext = direccion[0].subThoroughfare
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun getKerklys () {
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
        val presupuestoGET = retrofit.create(ObtenerKerklyInterface::class.java)
        val call = presupuestoGET.kerklys(oficio)

        call?.enqueue(object : retrofit2.Callback<List<Kerkly?>?> {

            override fun onResponse(call: Call<List<Kerkly?>?>, response: retrofit2.Response<List<Kerkly?>?>) {

                postlist = response.body() as java.util.ArrayList<Kerkly>
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
                        //primero Mandamos la solicitud a un kerkly
                        val telefoK =  postlist!![i].Telefonok
                       //System.out.println("el telefo del kerkly $telefoK")
                       // System.out.println("latitutinicial: $latitud longitudinicial $longitud latitudFinal $latitudFinal longitudFinal $longitudFinal")

                    }
                   // setProgress.dialog.dismiss()
                }

            }

            override fun onFailure(call: Call<List<Kerkly?>?>, t: Throwable) {
                System.out.println("error469:  $t")
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


        }
    }

    fun recorrerLista (){
        if(postlist!!.size == 0){
          //  print("____> vacio")
        }else{
            for(i in 0 until postlist!!.size){
              //  System.out.println(postlist!![i].Telefonok)
                //System.out.println("hora " + postlist!!.get(i).hora + ":" + postlist!!.get(i).minutos)
                //obtenerTokenKerkly(postlist!![i].Telefonok!!)
                val databaseUsu = instancias.referenciaInformacionDelUsuario(uid)
                databaseUsu.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //System.out.println("---->${snapshot.value}")
                        if (snapshot.value != null){
                            System.out.println("---->${snapshot.value}")
                            val u2 = snapshot.getValue(usuarios::class.java)
                            if (u2 != null) {
                                arraylistUsuarios.add(u2)
                                if (arraylistUsuarios.size != null){
                                  //  System.out.println("------------->con token tamño ${arraylistUsuarios[0].token}")
                                    for (i in 0 until arraylistUsuarios.size) {
                                       // System.out.println("------------->con token+ ${arraylistUsuarios[i].token}")
                                        val llamarTopico = llamarTopico()
                                        llamarTopico.llamartopico(this@MapsActivity, arraylistUsuarios[i].token, "(Servicio Urgente) $problema", "Usuario Nuevo-> $nombreCliente")
                                    }
                                    ingresarPresupuesto()
                                }else{
                                    System.out.println("-------------> Sin token")
                                }
                            }
                        }else{
                            System.out.println("-------------> Sin datos")
                            ingresarPresupuesto()
                        }


                    }

                    override fun onCancelled(error: DatabaseError) {
                        System.out.println("Firebase: $error")
                    }

                })
            } //setProgress.dialog.dismiss()
        }
    }

    fun insertarSolicitudFirebaseUrgente(idPresupuesto: Int, pago_total: String, problema:String, correo: String, TipoServicio:String, idKerklyAcepto: String, fechaHora: String, latitud: Double,longitud: Double){
        val reference = instancias.referenciaSolicitudUrgente(uid)
        val modelo: modeloSolicituUrgente
        modelo = modeloSolicituUrgente(idPresupuesto,pago_total,problema,correo,TipoServicio,idKerklyAcepto,fechaHora,latitud,longitud)
        reference.setValue(modelo) { error, ref ->


        }
    }

}