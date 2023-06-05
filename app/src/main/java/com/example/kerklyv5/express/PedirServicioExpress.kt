package com.example.kerklyv5.express

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.example.kerklyv5.MensajesActivity
import com.example.kerklyv5.controlador.Notificacion
import com.example.kerklyv5.R
import com.example.kerklyv5.controlador.AdapterSpinner
import com.example.kerklyv5.distancia_tiempo.CalcularTiempoDistancia
import com.example.kerklyv5.interfaces.*
import com.example.kerklyv5.modelo.serial.ModeloRutas
import com.example.kerklyv5.modelo.serial.NombreNoR
import com.example.kerklyv5.modelo.serial.Oficio
import com.example.kerklyv5.modelo.usuarios
import com.example.kerklyv5.notificaciones.llamarTopico
import com.example.kerklyv5.url.Url
import com.example.kerklyv5.vista.MainActivity
import com.example.kerklyv5.vista.Registro
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_pedir_servicio_express.inputProblematicaExpres
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

/*
* Obtener datos (servicio y problematica)  -listo
* Obtener dirección                        -listo
* Mostrar el activity de pago y calcularlo
*/

class PedirServicioExpress : AppCompatActivity(), CalcularTiempoDistancia.Geo {
    private lateinit var editProblematica: TextInputEditText
    private lateinit var layoutProblematica: TextInputLayout
    private lateinit var spinner: Spinner
    private var problematica: String = ""
    private var servicio: String = ""
    private var latitud: String = ""
    private var altitud: String = ""
    private lateinit var ciudad: String
    private lateinit var ciudadk: String
    private lateinit var estado: String
    private lateinit var pais: String
    var codigoPostal = ""
    var calle  =""
    var colonia =""
    var numExterior =""
    private lateinit var oficio: String
    private lateinit var Curp: String
    private lateinit var telefonokerkly: String
    private lateinit var telefonoR: String
    private var pago: Double = 0.0
    private lateinit var dialog: Dialog
    private var id = 0
    private var locationManager: LocationManager? = null
    private lateinit var editCiudad: TextInputEditText
    private lateinit var edit_cp: TextInputEditText
    private lateinit var calle_edit: TextInputEditText
    private lateinit var colonia_edit: TextInputEditText
    private lateinit var numero_extEdit: TextInputEditText
    private lateinit var edit_referecia: TextInputEditText
    private lateinit var lyout_referencia: TextInputLayout
    private lateinit var referencia:String
    private lateinit var boton: MaterialButton
    private lateinit var btn_direccion: MaterialButton
    private lateinit var toolbar: Toolbar
    private lateinit var nombre: String
    private lateinit var apellidoP: String
    private lateinit var apellidoM: String
    private lateinit var b: Bundle
    private lateinit var dialog2: Dialog
    private var intentos = 1
    private var band = false
    lateinit var poslist: ArrayList<ModeloRutas>
   lateinit var layoutNombre: TextInputLayout
   lateinit var editNombre: TextInputEditText
   lateinit var layoutAP: TextInputLayout
   lateinit var editAp: TextInputEditText
   lateinit  var layoutAM: TextInputLayout
   lateinit var editAM: TextInputEditText
   private lateinit var  numIntentos: String
    var i2: Int? = 0
    private lateinit var firebaseDatabaseUsu: FirebaseDatabase
    private lateinit var databaseUsu: DatabaseReference
    lateinit var arraylistUsuarios: ArrayList<usuarios>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedir_servicio_express)
        arraylistUsuarios = ArrayList<usuarios>()

        editProblematica = findViewById(R.id.inputProblematicaExpres)
        layoutProblematica = findViewById(R.id.layoutProblematicaExpress)
        spinner = findViewById(R.id.spinnerExpress)

        dialog = Dialog(this)
        dialog2 = Dialog(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000
            )
        }

        b = intent.extras!!

        telefonoR = b.getString("Teléfono No Registrado").toString()
        numIntentos = b.getString("numIntentos").toString()
        Log.d("telefono", telefonoR)
        getNombreNoR(telefonoR)
        getOficios()
    }

    private fun getOficios () {
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
        val presupuestoGET = retrofit.create(ObtenerOficiosInterface::class.java)
        val call = presupuestoGET.oficios()

        call?.enqueue(object : retrofit2.Callback<List<Oficio?>?> {

            override fun onResponse(
                call: Call<List<Oficio?>?>,
                response: retrofit2.Response<List<Oficio?>?>
            ) {
                val postList: ArrayList<Oficio> = response.body()
                        as ArrayList<Oficio>

                val aa = AdapterSpinner(applicationContext, postList)
                spinner.adapter = aa

            }

            override fun onFailure(call: Call<List<Oficio?>?>, t: Throwable) {

                Log.d("error del retrofit", t.toString())
                Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_LONG).show()
            }

        })
    }

   fun getNombreNoR(numero: String) {
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
        val presupuestoGET = retrofit.create(ObtenerNombresNoRInterface::class.java)
        val call = presupuestoGET.nombres(numero)

        call?.enqueue(object : retrofit2.Callback<List<NombreNoR?>?> {

            override fun onResponse(
                call: Call<List<NombreNoR?>?>,
                response: retrofit2.Response<List<NombreNoR?>?>
            ) {
                val postList: ArrayList<NombreNoR> = response.body()
                        as ArrayList<NombreNoR>

                if (postList[0].nombre_noR == null || postList[0].apellidoP_noR == null || postList[0].apellidoM_noR == null) {
                    dialog2.setContentView(R.layout.confirmar_nomrbre_no_registrado)
                    layoutNombre = dialog2.findViewById<TextInputLayout>(R.id.layoutNombre_NoRegistrado)
                    editNombre = dialog2.findViewById<TextInputEditText>(R.id.edit_Nombre_NoRegistrado)

                    layoutAP = dialog2.findViewById<TextInputLayout>(R.id.layoutApellidoP_NoRegistrado)
                    editAp = dialog2.findViewById<TextInputEditText>(R.id.edit_ApellidoP_NoRegistrado)

                    layoutAM = dialog2.findViewById<TextInputLayout>(R.id.layoutApellidoM_NoRegistrado)
                    editAM = dialog2.findViewById<TextInputEditText>(R.id.edit_ApellidoM_NoRegistrado)
                    dialog2.show()

                } else {
                    // b = Bundle()
                    var n = postList[0].nombre_noR
                    nombre = n
                    // Log.d("nosee", nombre)
                    apellidoP = postList[0].apellidoP_noR
                    apellidoM = postList[0].apellidoM_noR
                    intentos = postList[0].numIntentos
                    b.putInt("Intentos", intentos)
                }
            }

            override fun onFailure(call: Call<List<NombreNoR?>?>, t: Throwable) {

                Log.d("error del retrofit", t.toString())
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_pedir_servicio_express, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mensajes_settings -> {
                mensajes()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun aceptarDireccion() {
        referencia = edit_referecia.text.toString()
        dialog.dismiss()
        //ObetenerCoordenadas()
        confirmarDireccionBD()

    }

    fun mensajes() {
        //Toast.makeText(this, "Hola", Toast.LENGTH_SHORT).show()
        val i = Intent(this, MensajesActivity::class.java)
        i.putExtras(b)
        startActivity(i)
    }

    fun confirmarDireccionBD () {
        val id2 = spinner.selectedItemPosition +1
        oficio = id2.toString()
        val inten: Int = numIntentos.toInt() +1
      println("----------> 285 $inten")
        val ROOT_URL = Url().url
        val adapter = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api: direccionCoordenadasInterface = adapter.create(direccionCoordenadasInterface::class.java)

        api.sinRegistro(
            oficio,
            problematica,
            latitud,
            altitud,
            calle_edit.text.toString(),
            colonia_edit.text.toString(),
            "S/N",
            numero_extEdit.text.toString(),
            ciudad,
            estado,
            pais,
            edit_cp.text.toString(),
            referencia,
            telefonoR,
            nombre,
            apellidoP,
            apellidoM,
            inten.toString(),
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
                    val R = "Solicitud Enviada";
                    if(entrada == R){
                        Toast.makeText(applicationContext,"$entrada", Toast.LENGTH_LONG).show()
                        ObetenerCoordenadas()

                    }else{
                        val r = "Se acabo las pruebas sin registro"
                        if (entrada == r) {
                            val intent = Intent(this@PedirServicioExpress, Registro::class.java)
                            startActivity(intent)
                            finish()
                            Toast.makeText(applicationContext, "$entrada", Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(applicationContext, "$entrada", Toast.LENGTH_LONG).show()
                        }

                    }
                }
                override fun failure(error: RetrofitError?) {
                    println("error$error")
                }
            }
        )
    }

    private fun ObetenerCoordenadas(){
        val ofi =  spinner.selectedItem.toString()
        val ROOT_URL = Url().url
        val retrofit = Retrofit.Builder()
            .baseUrl(ROOT_URL+"/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val get = retrofit.create(InterfaceMejorRuta::class.java)
        val call = get.ObtenerC(ofi)
        call?.enqueue(object : retrofit2.Callback<List<ModeloRutas?>?> {
            override fun onResponse(call: Call<List<ModeloRutas?>?>,
                                    response: retrofit2.Response<List<ModeloRutas?>?>) {

                 poslist = response.body() as ArrayList<ModeloRutas>
                if (poslist.size != 0) {

                    for (i in 0 until poslist!!.size) {
                        val latA = latitud.toDouble()
                        val lngA = altitud.toDouble()
                        var latitudFinal = poslist!![i].latitud
                        var longitudFinal = poslist!![i].longitud
                        //primero Mandamos la solicitud a un kerkly
                        val telefoK =  poslist!![i].Telefono

                        val url2 = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$latA,$lngA&destinations=$latitudFinal,$longitudFinal&mode=driving&language=fr-FR&avoid=tolls&key=AIzaSyAp-2jznuGLfRJ_en09y1sp6A-467zrXm0"
                        CalcularTiempoDistancia(this@PedirServicioExpress).execute(url2)
                        System.out.println("el telefo del kerkly $telefoK")
                        System.out.println("latitutinicial: $latA longitudinicial $lngA latitudFinal $latitudFinal longitudFinal $longitudFinal")

                    }
                    recorrerLista()

                    /*
                    //16.944851, -98.238820
                    val locationA = Location("punto De origen")
                    val latA = latitud.toDouble()
                    val lngA = altitud.toDouble()
                   // locationA.latitude = latA
                    //locationA.longitude = lngA

                    locationA.latitude = 16.944851
                    locationA.longitude = -98.238820

                    val locationB = Location("punto destino")
                    Curp = poslist.get(0).Curp
                    telefonokerkly = poslist.get(0).Telefono
                    ciudadk = poslist.get(0).Ciudad
                    locationB.latitude = poslist.get(0).latitud
                    locationB.longitude = poslist.get(0).longitud
                    var menorDistancia = locationA.distanceTo(locationB).toDouble()/1000
                   Log.e(" 373", "ciudad $ciudadk Telefono: $telefonokerkly Curp $Curp  Distancia $menorDistancia km")

                    for(i in 1 until poslist.size){
                        val locationC = Location("punto c")
                        Curp = poslist.get(i).Curp
                        ciudadk = poslist.get(i).Ciudad
                        telefonokerkly = poslist.get(i).Telefono
                        locationC.latitude = poslist.get(i).latitud
                        locationC.longitude = poslist.get(i).longitud
                        var distance = locationA.distanceTo(locationC).toDouble()/1000

                        Log.e(" ","ciudad $ciudadk telefono: $telefonokerkly Curp $Curp distancia $i  distancia $distance")
                        if (distance < menorDistancia){
                           // menorDistancia = distance
                            Curp = poslist.get(i-1).Curp
                            ciudadk = poslist.get(i-1).Ciudad
                            telefonokerkly = poslist.get(i-1).Telefono
                            println("-------->$i menor distancia 385 ciudad $ciudadk  Telefono: $telefonokerkly Curp $Curp  $menorDistancia")
                            //txtKcercano.setText("Curp $Curp menor distancia $menorDistancia" )
                        }else{
                            var lamejorRuta =0.0
                            if(menorDistancia < distance) {
                                lamejorRuta = distance
                            }
                            if (distance > lamejorRuta){
                                lamejorRuta = distance
                            }
                            Curp = poslist.get(i-1).Curp
                            ciudadk = poslist.get(i-1).Ciudad
                            telefonokerkly = poslist.get(i-1).Telefono
                            println("-------->menor distancia 394 ciudad $ciudadk  Telefono: $telefonokerkly Curp $Curp  $lamejorRuta")
                        }
                    }

                     */
                } else {
                    Toast.makeText(applicationContext, "No hay Kerklys disponibles", Toast.LENGTH_SHORT).show()
                }
                //txtKcercano.setText("Curp $Curp menor distancia $menorDistancia")
                // Log.e("Curp $Curp distancia ", "menor distancia $menorDistancia")
            }

            override fun onFailure(call: Call<List<ModeloRutas?>?>, t: Throwable) {
                Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_LONG).show()
                System.out.println("el error es: ${t.toString()}")

            }
        })

    }

    fun actualizar() {
        locationStart()
    }

    /*fun ServicioExpressNR(view: View) {
        problematica = editProblematica.text.toString()

        if (latitud.isEmpty() && altitud.isEmpty()) {
            Toast.makeText(this, "Ingrese su dirección", Toast.LENGTH_SHORT).show()
            return
        }

        if (problematica.isEmpty()) {
            layoutProblematica.error = getText(R.string.campo_requerido)
        } else {
            layoutProblematica.error = null
        }

        if (!(latitud.isEmpty() && altitud.isEmpty() && problematica.isEmpty())) {

        } else {
            Toast.makeText(this,
                "",
                Toast.LENGTH_SHORT
            ).show()
        }
    }*/

    fun aceptarNoR(view: View) {
        nombre = editNombre.text.toString()
        apellidoP = editAp.text.toString()
        apellidoM =  editAM.text.toString().trim()
            if (nombre.isEmpty()){
                layoutNombre.error = getString(R.string.campo_requerido)
            } else {
                layoutNombre.error = null
            }

            if (apellidoP.isEmpty()) {
                layoutAP.error = getString(R.string.campo_requerido)
            } else {
                layoutAP.error = null
            }

            if (apellidoM.isEmpty()) {
                layoutAM.error = getString(R.string.campo_requerido)
            } else {
                layoutAM.error = null
            }
            if (!nombre.isEmpty() && !apellidoP.isEmpty() && !apellidoM.isEmpty()) {
                nombre = editNombre.text.toString()
                apellidoP = editAp.text.toString()
                apellidoM =  editAM.text.toString()
                //confirmarDireccionBD()
                InsertarNombreClienteNR(telefonoR,nombre, apellidoP, apellidoM)
                dialog2.dismiss()
            }

    }
    fun cancelar(view: View){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun InsertarNombreClienteNR(telefono: String, nombre: String, apellidoP: String, apellidoM: String) {
        val ROOT_URL = Url().url
        val adapter = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api = adapter.create(EntrarSinRegistroInterface::class.java)
        api.insertarNombreNR(telefonoR, nombre, apellidoP, apellidoM,
        object : Callback<Response?> {
            override fun success(t: Response?, response: Response?) {
                var salida: BufferedReader? = null
                var entrada = ""
                try {
                    salida = BufferedReader(InputStreamReader(t?.body?.`in`()))
                    entrada = salida.readLine()
                }catch (e: IOException){
                    e.printStackTrace()
                }
                Toast.makeText(this@PedirServicioExpress, entrada, Toast.LENGTH_SHORT).show()
            }

            override fun failure(error: RetrofitError?) {
                TODO("Not yet implemented")
            }

        })
    }

    @SuppressLint("SetTextI18n")
    fun direccionExpress(view: View) {
        problematica = editProblematica.text.toString()
        servicio = spinner.selectedItem.toString()
        var band = false
        if (problematica.isEmpty()) {
            band = false
            layoutProblematica.error = getText(R.string.campo_requerido)
        } else {
            band = true
            layoutProblematica.error = null
        }

        if (band) {
            if (nombre == "" || apellidoP == "" || apellidoM == ""){
                dialog2.setContentView(R.layout.confirmar_nomrbre_no_registrado)
                layoutNombre = dialog2.findViewById<TextInputLayout>(R.id.layoutNombre_NoRegistrado)
                editNombre = dialog2.findViewById<TextInputEditText>(R.id.edit_Nombre_NoRegistrado)
                layoutAP = dialog2.findViewById<TextInputLayout>(R.id.layoutApellidoP_NoRegistrado)
                editAp = dialog2.findViewById<TextInputEditText>(R.id.edit_ApellidoP_NoRegistrado)

                layoutAM = dialog2.findViewById<TextInputLayout>(R.id.layoutApellidoM_NoRegistrado)
                editAM = dialog2.findViewById<TextInputEditText>(R.id.edit_ApellidoM_NoRegistrado)
                dialog2.show()
            }else {
                dialog.setContentView(R.layout.confirmar_direccion)
                boton = dialog.findViewById(R.id.actualizar_btn)
                editCiudad = dialog.findViewById<TextInputEditText>(R.id.edit_ciudad)
                edit_cp = dialog.findViewById<TextInputEditText>(R.id.edit_codigoP)
                calle_edit = dialog.findViewById<TextInputEditText>(R.id.edit_calle)
                colonia_edit = dialog.findViewById<TextInputEditText>(R.id.edit_colonia)
                numero_extEdit = dialog.findViewById<TextInputEditText>(R.id.edit_numeroExt)
                edit_referecia = dialog.findViewById(R.id.edit_referencia)
                lyout_referencia = dialog.findViewById(R.id.layout_referencia)
                btn_direccion = dialog.findViewById(R.id.btn_direccion_exrpess)

                btn_direccion.setOnClickListener {
                    aceptarDireccion()
                }

                boton.setOnClickListener {
                    actualizar()
                }

                boton.isEnabled = true
                locationStart()

                dialog.show()
            }
        }
    }

    private fun locationStart() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val Local = Localizacion()
        Local.mainActivityConsultaSinRegistro = this
        val gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!gpsEnabled) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1000
            )
            return
        }
        locationManager!!.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0,
            0f,
            (Local as LocationListener)!!
        )
        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            (Local as LocationListener)!!
        )

    }

    fun setLocation(loc: Location) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        /*if (loc.latitude !== 0.0 && loc.longitude !== 0.0) {
            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val list = geocoder.getFromLocation(
                        loc.latitude, loc.longitude, 1)
                if (!list.isEmpty()) {
                    val DirCalle = list[0]

                    txtDireccion.setText(DirCalle.getAddressLine(0))
                    editexDirec.setText(DirCalle.getAddressLine(0))
                    //locationManager!!.removeUpdates((locationManager as LocationListener?)!!)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }*/
        if (loc.latitude !== 0.0 && loc.longitude !== 0.0) {
            try {
                val geocoder: Geocoder
                val direccion: List<Address>
                geocoder = Geocoder(this, Locale.getDefault())

                direccion = geocoder.getFromLocation(
                    loc.latitude,
                    loc.longitude,
                    1
                ) // 1 representa la cantidad de resultados a obtener
                val address = direccion[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()


                if(direccion[0].locality ==null) {
                   ciudad = "SN"
               }else{
                    ciudad = direccion[0].locality // ciudad
                }
                if(direccion[0].adminArea ==null) {
                    estado = "SN"
                }else{
                    estado = direccion[0].adminArea //estado
                }
                if( direccion[0].countryName==null) {
                    pais = "SN"
                }else{
                    pais = direccion[0].countryName // pais
                }
               if( direccion[0].postalCode == null) {
                    codigoPostal = "SN" //codigo Postal
               }else{
                   codigoPostal = direccion[0].postalCode //codigo Postal
               }
               if ( direccion[0].thoroughfare == null ) {
                    calle =  "SN" // la calle
               }else{
                   calle = direccion[0].thoroughfare // la calle
               }
               if (direccion[0].subLocality== null) {
                    colonia =  "SN"// colonia
               }else{
                   colonia = direccion[0].subLocality// colonia
               }
              if (direccion[0].subThoroughfare == null) {
                   numExterior = "SN"
              }else{
                  numExterior = direccion[0].subThoroughfare
              }

                  // var ubicacion: String = "$ciudad, $estado, $pais"
                  // editCiudad.setText(ubicacion)
                   latitud = loc.latitude.toString()
                   altitud = loc.longitude.toString()
                   calle_edit.setText(calle)
                   colonia_edit.setText(colonia)
                   numero_extEdit.setText(numExterior)
                   edit_cp.setText(codigoPostal)


                   // txtDireccion.setText(address)
                   //   txtCiudad.setText(ciudad)
                   //  txtEstado.setText(estado)
                   //   txtPais.setText(pais)
                   var ubicacion: String = "$ciudad, $estado, $pais"
                   editCiudad.setText(ubicacion)
                   latitud = loc.latitude.toString()
                   altitud = loc.longitude.toString()
                   calle_edit.setText(calle)
                   colonia_edit.setText(colonia)
                   numero_extEdit.setText(numExterior)
                   edit_cp.setText(codigoPostal)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        //  editexDirec.setText("ciudad $city \n Estado  $state \n pais $country \n codigo Postal $postalCode \n calle $calle \n colonia $colonia")
    }
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart()
                return
            }
        }
    }
    inner class Localizacion : LocationListener {
        var mainActivityConsultaSinRegistro: PedirServicioExpress? = null
        override fun onLocationChanged(loc: Location) {
            // if(loc != null){
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            loc.latitude
            loc.longitude
            val sLatitud = java.lang.String.valueOf(loc.latitude)
            val sLongitud = java.lang.String.valueOf(loc.longitude)
            mainActivityConsultaSinRegistro?.setLocation(loc)
            mainActivityConsultaSinRegistro?.locationManager?.removeUpdates(this)
            //  }
            //mainActivityConsultaSinRegistro?.locationManager!!.removeUpdates((mainActivityConsultaSinRegistro?.locationManager as LocationListener?)!!)
        }
        override fun onProviderDisabled(provider: String) {
            //mainActivityConsultaSinRegistro?.txt.setText("GPS Desactivado")
            Toast.makeText(mainActivityConsultaSinRegistro, "GPS Desactivado", Toast.LENGTH_SHORT)
                .show()
        }
        override fun onProviderEnabled(provider: String) {

            Toast.makeText(mainActivityConsultaSinRegistro, "GPS activado", Toast.LENGTH_SHORT)
                .show()
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            when (status) {
                LocationProvider.AVAILABLE -> Log.d("debug", "LocationProvider.AVAILABLE")
                LocationProvider.OUT_OF_SERVICE -> Log.d("debug", "LocationProvider.OUT_OF_SERVICE")
                LocationProvider.TEMPORARILY_UNAVAILABLE -> Log.d(
                    "debug",
                    "LocationProvider.TEMPORARILY_UNAVAILABLE"
                )
            }
        }
    }

    override fun setDouble(min: String?) {
        val res = min!!.split(",").toTypedArray()
        val min = res[0].toDouble() / 60
        val dist = res[1].toInt() / 1000
        i2 = i2!! +1
        val e = i2!!-1
        System.out.println("valor de e : $e")

        //poslist!![e].hora = (min / 60).toInt()
        //poslist!![e].minutos = (min % 60).toInt()
       // poslist!![e].horaMin = poslist!![e].hora + poslist!![e].minutos


        if (e == (poslist!!.size-1)) {
            poslist!!.sortBy {
                it.horaMin
            }
         //   System.out.println("Kerkly $e ${poslist!![e].horaMin}")

        }
    }

    fun recorrerLista (){
        if(poslist!!.size == 0){
            print("____> vacio")
        }else{
            for(i in 0 until poslist!!.size){
                System.out.println(poslist!![i].Telefono)
                System.out.println("hora " + poslist!!.get(i).hora + ":" + poslist!!.get(i).minutos)
                //obtenerTokenKerkly(postlist!![i].Telefonok!!)
                firebaseDatabaseUsu = FirebaseDatabase.getInstance()
                databaseUsu = firebaseDatabaseUsu.getReference("UsuariosR").child(poslist!![i].Telefono).child("MisDatos")
                databaseUsu.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //System.out.println("---->${snapshot.value}")
                        if (snapshot.value != null){
                            System.out.println("---->${snapshot.value}")
                            val u2 = snapshot.getValue(usuarios::class.java)
                            if (u2 != null) {
                                arraylistUsuarios.add(u2)
                                if (arraylistUsuarios.size != null){
                                    System.out.println("------------->con token tamño ${arraylistUsuarios[0].token}")
                                    for (i in 0 until arraylistUsuarios.size) {
                                        System.out.println("------------->con token+ ${arraylistUsuarios[i].token}")
                                       // ingresarPresupuesto()
                                        val llamarTopico = llamarTopico()
                                        llamarTopico.llamartopico(this@PedirServicioExpress, arraylistUsuarios[i].token, "(Servicio Urgente) $problematica", "Usuario Sin Registro-> $nombre " + apellidoP + " $apellidoM")
                                        inputProblematicaExpres.text = null
                                    }
                                }else{
                                    System.out.println("-------------> Sin token")
                                }
                            }
                        }else{
                            System.out.println("-------------> Sin datos")
                          //  ingresarPresupuesto()
                        }


                    }

                    override fun onCancelled(error: DatabaseError) {
                        System.out.println("Firebase: $error")
                    }

                })

            } //setProgress.dialog.dismiss()
        }

    }
}