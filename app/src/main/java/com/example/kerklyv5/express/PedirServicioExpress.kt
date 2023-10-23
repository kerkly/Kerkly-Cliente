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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.example.kerklyv5.BaseDatosEspacial.conexionPostgreSQL
import com.example.kerklyv5.MensajesActivity
import com.example.kerklyv5.R
import com.example.kerklyv5.controlador.AdapterSpinner
import com.example.kerklyv5.distancia_tiempo.CalcularTiempoDistancia
import com.example.kerklyv5.interfaces.*
import com.example.kerklyv5.modelo.serial.ModeloRutas
import com.example.kerklyv5.modelo.serial.NombreNoR
import com.example.kerklyv5.modelo.serial.Oficio
import com.example.kerklyv5.modelo.usuarios
import com.example.kerklyv5.notificaciones.llamarTopico
import com.example.kerklyv5.url.Instancias
import com.example.kerklyv5.url.Url
import com.example.kerklyv5.vista.MainActivity
import com.example.kerklyv5.vista.Registro
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
import java.util.*
import kotlin.collections.ArrayList



class PedirServicioExpress : AppCompatActivity(), CalcularTiempoDistancia.Geo {
    private lateinit var editProblematica: TextInputEditText
    private lateinit var layoutProblematica: TextInputLayout
    private lateinit var spinner: Spinner
    private var problematica: String = ""
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    private lateinit var ciudad: String
    private lateinit var estado: String
    private lateinit var pais: String
    private var codigoPostal = ""
    private var calle  =""
    private var colonia =""
    private var numExterior =""
    private lateinit var oficio: String
    private lateinit var telefonoR: String
    private lateinit var dialog: Dialog
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
    lateinit var poslist: ArrayList<ModeloRutas>
   lateinit var layoutNombre: TextInputLayout
   lateinit var editNombre: TextInputEditText
   lateinit var layoutAP: TextInputLayout
   lateinit var editAp: TextInputEditText
   lateinit  var layoutAM: TextInputLayout
   lateinit var editAM: TextInputEditText
   private lateinit var  numIntentos: String
    var i2: Int? = 0
    lateinit var arraylistUsuarios: ArrayList<usuarios>
    lateinit var inputProblematicaExpres: TextInputEditText
    private lateinit var listaTextos: ArrayList<String>
    private var palabrasClave = mutableMapOf<String, String>()
    private lateinit var lista: ArrayList<Oficio>
    private lateinit var conexionPostgreSQL: conexionPostgreSQL
    private lateinit var instancias: Instancias

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedir_servicio_express)
        arraylistUsuarios = ArrayList<usuarios>()

        editProblematica = findViewById(R.id.inputProblematicaExpres)
        layoutProblematica = findViewById(R.id.layoutProblematicaExpress)
        inputProblematicaExpres = findViewById(R.id.inputProblematicaExpres)
        spinner = findViewById(R.id.spinnerExpress)

        dialog = Dialog(this)
        dialog2 = Dialog(this)
        conexionPostgreSQL = conexionPostgreSQL()
        instancias = Instancias()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000
            )
        }
        b = intent.extras!!
        telefonoR = b.getString("Teléfono No Registrado").toString()
        numIntentos = b.getString("numIntentos").toString()
        lista =ArrayList()
        getOficios()

        listaTextos = ArrayList()
        inputProblematicaExpres.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                print("$p0")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                print("$p0")
            }

            override fun afterTextChanged(p0: Editable?) {
                val p = p0.toString()
                val parts: List<String> = p.split(" ")
                listaTextos.clear()
                for (i in 0 until parts.size){
                    val pa = parts[i]
                    var palabraAsociada: String? = null
                    for ((clave, palabra) in palabrasClave){
                        if (clave == pa){
                            palabraAsociada = palabra
                            break
                        }
                    }
                    if (palabraAsociada != null){
                        listaTextos.add(palabraAsociada)
                        spinner.setAdapter(ArrayAdapter<String>(this@PedirServicioExpress, android.R.layout.simple_spinner_dropdown_item, listaTextos))
                    }
                }

                if (p0.toString() == ""){
                    SpinerADapter(lista)
                    listaTextos.clear()
                }

            }

        })
    }
    fun SpinerADapter(lista: ArrayList<Oficio>){
        val aa = AdapterSpinner(this, lista)
        spinner.adapter = aa
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

            override fun onResponse(call: Call<List<Oficio?>?>, response: retrofit2.Response<List<Oficio?>?>) {
                val postList: ArrayList<Oficio> = response.body()
                        as ArrayList<Oficio>
                val aa = AdapterSpinner(applicationContext, postList)
             //   spinner.adapter = aa
                lista = ArrayList()
                lista = postList
                spinner.adapter = aa
                Diccionario()
            }
            override fun onFailure(call: Call<List<Oficio?>?>, t: Throwable) {
                Log.d("error del retrofit", t.toString())
                Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun Diccionario() {
       var palClaves = ""
        var oficio = ""
        for (i in 0 until lista.size){
            palClaves = lista[i].PalabrasClaves
            oficio = lista[i].nombreO
            val parts: List<String> = palClaves.split(", ")
            for (i in 0 until parts.size){
                var pa = parts[i]
                palabrasClave[pa] = oficio
            }
        }
    }

    fun getNombreNoR(view: View) {
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
        val call = presupuestoGET.nombres(telefonoR)

        call?.enqueue(object : retrofit2.Callback<List<NombreNoR?>?> {

            override fun onResponse(
                call: Call<List<NombreNoR?>?>,
                response: retrofit2.Response<List<NombreNoR?>?>
            ) {
                val postList: ArrayList<NombreNoR> = response.body() as ArrayList<NombreNoR>

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
                    oficio = spinner.getSelectedItem().toString()
                    println("oficio --- $oficio")
                    direccionExpress()
                }
            }
            override fun onFailure(call: Call<List<NombreNoR?>?>, t: Throwable) {
                Log.d("error del retrofit", t.toString())
                ShowMensaje(t.toString())
            }
        })
    }

    private fun ShowMensaje(mensaje:String){
        Toast.makeText(this, mensaje,Toast.LENGTH_LONG).show()
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
        dialog.dismiss()
        oficio = spinner.getSelectedItem().toString()
        confirmarDireccionBD()
    }

    fun mensajes() {
        //Toast.makeText(this, "Hola", Toast.LENGTH_SHORT).show()
        val i = Intent(this, MensajesActivity::class.java)
        i.putExtras(b)
        startActivity(i)
    }

    fun confirmarDireccionBD () {
        ShowMensaje("Por favor espere un momento...")
        println("oficio --- $oficio, $problematica, $latitud, $longitud, ${calle_edit.text.toString()}, ${colonia_edit.text.toString()}")
        println("argumrntos ---${numero_extEdit.text.toString()}, $ciudad, $estado, $pais, ${edit_cp.text.toString()}, $referencia, $telefonoR, $nombre, $apellidoP, $apellidoM")
            val intentos: Int = numIntentos.toInt() + 1
            val ROOT_URL = Url().url
            val adapter = RestAdapter.Builder()
                .setEndpoint(ROOT_URL)
                .build()
            val api: direccionCoordenadasInterface =
                adapter.create(direccionCoordenadasInterface::class.java)

            api.sinRegistro(
                oficio.toString(),
                problematica.toString(),
                latitud.toString(),
                longitud.toString(),
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
                intentos.toString(),
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
                        if (entrada == R) {
                            Toast.makeText(applicationContext, "$entrada", Toast.LENGTH_LONG).show()
                            //   ObetenerCoordenadas()

                        } else {
                            val r = "Se acabo las pruebas sin registro"
                            if (entrada == r) {
                                val intent = Intent(this@PedirServicioExpress, Registro::class.java)
                                startActivity(intent)
                                finish()
                                Toast.makeText(applicationContext, "$entrada", Toast.LENGTH_LONG)
                                    .show()
                            } else {
                                KerklyCercanos(latitud, longitud, oficio)
                            }

                        }
                    }

                    override fun failure(error: RetrofitError?) {
                        println("error 395 ${error!!.message}")
                        ShowMensaje("error 395" + error.toString())
                    }
                }
            )

    }

    private fun KerklyCercanos(latitud: Double, longitud: Double, oficio: String){
        val conexion  = conexionPostgreSQL.obtenerConexion(this)
        if (conexion != null){
            val secciones =  conexionPostgreSQL.poligonoCircular(latitud, longitud, 3000.0)
            val kerklysCercanos = conexionPostgreSQL.Los5KerklyMasCercanos(secciones, longitud, latitud, oficio)
            if (kerklysCercanos.isEmpty()){
                ShowMensaje("Lo Sentimos pero en esta área no se encuentran kerklys cercanos")
            }else{
                for(kerkly in kerklysCercanos.reversed()){
                    conexionPostgreSQL.cerrarConexion()
                    MandarNoti(kerkly.uidKerkly, problematica, "$nombre $apellidoP $apellidoM")
                }
                ShowMensaje("En un momento recibira Respuesta")
            }
        }
    }

    private fun MandarNoti(uidKerkly: String, problematica: String, s: String) {
       val databaseUsu = instancias.referenciaInformacionDelKerkly(uidKerkly)
        databaseUsu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    System.out.println("---->${snapshot.value}")
                    val u2 = snapshot.getValue(usuarios::class.java)
                    if (u2 != null) {
                        arraylistUsuarios.add(u2)
                        if (arraylistUsuarios.size != null) {
                            for (i in 0 until arraylistUsuarios.size) {
                                // ingresarPresupuesto()
                                val llamarTopico = llamarTopico()
                                llamarTopico.llamartopico(this@PedirServicioExpress, arraylistUsuarios[i].token, "(Servicio Urgente) $problematica", "Usuario Sin Registro-> $nombre " + apellidoP + " $apellidoM")
                               println("${arraylistUsuarios[i].telefono} ${arraylistUsuarios[i].uid} ${arraylistUsuarios[i].token}")
                                inputProblematicaExpres.text = null
                            }
                        } else {
                            System.out.println("-------------> Sin token")
                        }
                    }
                } else {
                    System.out.println("-------------> Sin datos")
                    //  ingresarPresupuesto()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                ShowMensaje(error.message)
            }

        })
    }


    fun actualizar() {
        locationStart()
    }


    @SuppressLint("SuspiciousIndentation")
    fun aceptarNoR(view: View) {
        nombre = editNombre.text.toString()
        apellidoP = editAp.text.toString()
        apellidoM =  editAM.text.toString()
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
                InsertarNombreClienteNR(nombre, apellidoP, apellidoM)
                dialog2.dismiss()
                direccionExpress()
            }
    }
    fun cancelar(view: View){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun InsertarNombreClienteNR(nombre: String, apellidoP: String, apellidoM: String) {
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
                ShowMensaje(error.toString())
            }

        })
    }

    @SuppressLint("SetTextI18n")
    fun direccionExpress() {
        problematica = editProblematica.text.toString()
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
                referencia = edit_referecia.text.toString()
                btn_direccion = dialog.findViewById(R.id.btn_direccion_exrpess)
                btn_direccion.setOnClickListener {
                    if (referencia == "") {
                        referencia = "SN"
                        aceptarDireccion()
                    } else {
                        aceptarDireccion()
                    }

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
        if (loc.latitude !== 0.0 && loc.longitude !== 0.0) {
            try {
                val geocoder: Geocoder
                val direccion: List<Address>
                geocoder = Geocoder(this, Locale.getDefault())

                direccion = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)!! // 1 representa la cantidad de resultados a obtener
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
                   latitud = loc.latitude
                   longitud = loc.longitude
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
                   latitud = loc.latitude
                   longitud = loc.longitude
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

            } //setProgress.dialog.dismiss()
        }
    }
}