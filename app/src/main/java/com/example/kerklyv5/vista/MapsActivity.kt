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
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kerklyv5.R
import com.example.kerklyv5.SolicitarServicio
import com.example.kerklyv5.express.PedirServicioExpress
import com.example.kerklyv5.interfaces.IngresarPresupuestoClienteInterface
import com.example.kerklyv5.interfaces.IngresarPresupuestoInterface
import com.example.kerklyv5.interfaces.IngresarPresupuestoUrgente
import com.example.kerklyv5.url.Url

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.confirmar_direccion.*
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private lateinit var mMap: GoogleMap
    private lateinit var marcador: Marker
    var latitud: Double = 0.0
    var longitud: Double = 0.0
    lateinit var BotonT: Button
    lateinit var BotonEnviarU: Button
    var tipo = 1
    lateinit var mapa: String
    private lateinit var b: Bundle
    private lateinit var curp: String
    private lateinit var problema: String
    private lateinit var telefono: String
    private lateinit var oficio: String
    private lateinit var dialog: Dialog
    private lateinit var boton_confirmar: MaterialButton
    private lateinit var boton_actualizar: MaterialButton
    private lateinit var editCiudad: TextInputEditText
    private lateinit var edit_cp: TextInputEditText
    private lateinit var calle_edit: TextInputEditText
    private lateinit var colonia_edit: TextInputEditText
    private lateinit var numero_extEdit: TextInputEditText
    private lateinit var edit_referecia: TextInputEditText
    private lateinit var lyout_referencia: TextInputLayout
      var ciudad: String = ""
    private lateinit var calle: String
    private lateinit var cp: String
    private lateinit var colonia: String
    private lateinit var num_ext: String
    private lateinit var referencia: String
    private  var estado: String = ""
    private lateinit var pais: String
    private var band = false
    var NombreUbi: String? = null
    private var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        b = intent.extras!!

        dialog = Dialog(this)

        /* curp = b.getString("Curp Kerkly").toString()
         problema = b.getString("Problema").toString()
         telefono = b.getString("Telefono").toString()
         oficio = b.getString("Oficio").toString()*/

        band = b.getBoolean("Express")
        NombreUbi = b.getString("Nombre")
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

            //setLocation(latitud, longitud)
           // ingresarPresupuesto()
           /* intent = Intent(applicationContext, SolicitarServicio::class.java)
           // aceptarDireccion()
            ingresarPresupuesto()

            b.putBoolean("PresupuestoListo", true)
            intent.putExtras(b)
            startActivity(intent)*/
            /*val la = java.lang.Double.toString(latitud)
            val lo = java.lang.Double.toString(longitud)

            //if (bandK!!) {
                Toast.makeText(this, "Mi latitud ${la}", Toast.LENGTH_SHORT).show()
            //} else {

                lateinit var intent: Intent
                //   intent.putExtra("dato1", la)
                // intent.putExtra("dato2", lo)

                if (band == true) {
                    intent = Intent(this, PedirServicioExpress::class.java)
                } else {
                    intent = Intent(this, SolicitarServicio::class.java)
                    ingresarPresupuesto()
                }

                //codigo que va antes de llamar al activity

                b.putString("Latitud", la)
                b.putString("Longitud", lo)
                b.putBoolean("PresupuestoListo", true)
                intent.putExtras(b)
                startActivity(intent)
          //  }*/
      locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!gpsEnabled) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
        }else {
            setLocation(latitud, longitud)
            if (!band) {
                val i = Intent(applicationContext, KerklyListActivity::class.java)
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
                i.putExtras(b)
                startActivity(i)
                finish()
            } else {
                val intent = Intent(applicationContext, SolicitarServicio::class.java)
                Toast.makeText(this, "referencia", Toast.LENGTH_SHORT).show()
                //aceptarDireccion()v
                ingresarPresupuesto()
                b.putBoolean("PresupuestoListo", true)
                intent.putExtras(b)
                startActivity(intent)
                finish()
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
        // mMap.isMyLocationEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        val locationManager = this@MapsActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val miUbicacion = LatLng(location.getLatitude(), location.getLongitude())
             //  val miUbicacion = LatLng(17.5551109, -99.5042626)

                locationManager.removeUpdates(this)
                marcador = googleMap.addMarker(MarkerOptions().position(miUbicacion).draggable(true).title(NombreUbi.toString()).icon(
                    BitmapDescriptorFactory.fromResource(
                        R.drawable.miubicacion4
                    )))!!

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 20F))
                latitud = location.latitude
                longitud = location.longitude

            }
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                when (status) {
                    LocationProvider.AVAILABLE -> Log.d("debug", "LocationProvider.AVAILABLE")
                    LocationProvider.OUT_OF_SERVICE -> Log.d("debug", "LocationProvider.OUT_OF_SERVICE")
                    LocationProvider.TEMPORARILY_UNAVAILABLE -> Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE")
                }
            }
            override fun onProviderEnabled(provider: String) {
                  Toast.makeText(this@MapsActivity, "GPS activado", Toast.LENGTH_SHORT).show()

            }
            override fun onProviderDisabled(provider: String) {
                  Toast.makeText(this@MapsActivity, "GPS Desactivado", Toast.LENGTH_SHORT).show()
                //val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                //startActivity(settingsIntent)
              /*  val miUbicacion = LatLng(17.5551109, -99.5042626)
               // val miUbicacion = LatLng(16.94508, -98.23878833333335)
                marcador = googleMap.addMarker(MarkerOptions().position(miUbicacion).draggable(true).title(NombreUbi.toString()).icon(
                    BitmapDescriptorFactory.fromResource(
                        R.drawable.miubicacion4
                    )))!!

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 20F))*/
            }
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
            //seccion donde se encuentran los mappas

        //los marcadores
        // Utils_k.Marcador(mMap, applicationContext, telefono)
        //  mMap!!.setOnMapLongClickListener(this)
        googleMap.setOnMarkerClickListener(this)
        googleMap.setOnMarkerDragListener(this)

       // mMap!!.setOnMarkerClickListener(this)

    }

    private fun getLocalizacion() {
        val permiso =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permiso == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
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
        dialog.dismiss()
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
            //  Toast.makeText(context, "latitud ${latitud.toString()} longitud $longitud", Toast.LENGTH_LONG).show()


           /* val builder = AlertDialog.Builder(this)
            builder.setMessage("Por favor, confirme su ubicación")
            builder.setTitle(NombreUbi.toString())
            builder.setCancelable(false)
            builder.setPositiveButton("Si") { d, which ->
                //Toast.makeText(this, "elegido", Toast.LENGTH_LONG).show()
                //TrazarLineas(latLng)
                dialog.setContentView(R.layout.confirmar_direccion)
                dialog.show()
                boton_confirmar = dialog.findViewById(R.id.btn_direccion_exrpess)
                boton_actualizar = dialog.findViewById(R.id.actualizar_btn)
                boton_actualizar.visibility = View.GONE
                editCiudad = dialog.findViewById<TextInputEditText>(R.id.edit_ciudad)
                edit_cp = dialog.findViewById<TextInputEditText>(R.id.edit_codigoP)
                calle_edit = dialog.findViewById<TextInputEditText>(R.id.edit_calle)
                colonia_edit = dialog.findViewById<TextInputEditText>(R.id.edit_colonia)
                numero_extEdit = dialog.findViewById<TextInputEditText>(R.id.edit_numeroExt)
                edit_referecia = dialog.findViewById(R.id.edit_referencia)
                lyout_referencia = dialog.findViewById(R.id.layout_referencia)

                setLocation(latitud, longitud)

                boton_confirmar.setOnClickListener {
                    aceptarDireccion()
                    if(!band) {
                        val i = Intent(applicationContext, KerklyListActivity::class.java)
                        b.putString("Calle", calle)
                        b.putString("Colonia", colonia)
                        b.putString("Código Postal", cp)
                        b.putString("Exterior", num_ext)
                        b.putString("Referencia", referencia)
                        b.putDouble("Latitud", latitud)
                        b.putDouble("Longitud", longitud)
                        b.putString("Ciudad", ciudad)
                        b.putString("Estado", estado)
                        b.putString("Pais", pais)
                        i.putExtras(b)
                        startActivity(i)
                    } else {
                        val intent = Intent(applicationContext, SolicitarServicio::class.java)
                           //Toast.makeText(this, referencia, Toast.LENGTH_SHORT).show()
                        aceptarDireccion()
                        ingresarPresupuesto()
                        b.putBoolean("PresupuestoListo", true)
                        intent.putExtras(b)
                        startActivity(intent)
                    }
                }
            }
            builder.setNegativeButton("No") { dialog, which -> dialog.cancel() }
            val alertDialog = builder.create()
            alertDialog.show()*/

            /*boton_actualizar.setOnClickListener {

            }*/


           // dialog.show()
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

                        val cadena = "Datos enviados"
                        if (cadena.equals(entrada)) {
                            Toast.makeText(applicationContext, "Datos enviados", Toast.LENGTH_LONG)
                                .show()
                        }
                    }

                    override fun failure(error: RetrofitError?) {
                        println("error$error")
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
        if (latitud !== 0.0 && longitud !== 0.0) {

            try {
                val geocoder: Geocoder
                val direccion: List<Address>
                geocoder = Geocoder(this, Locale.getDefault())

                direccion = geocoder.getFromLocation(latitud, longitud, 1) // 1 representa la cantidad de resultados a obtener
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


}