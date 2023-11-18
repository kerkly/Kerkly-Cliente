package com.example.kerklyv5

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.kerklyv5.SQLite.DataManager
import com.example.kerklyv5.SQLite.MisOficios
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.databinding.ActivitySolicitarServicioBinding
import com.example.kerklyv5.interfaces.CerrarSesionInterface
import com.example.kerklyv5.interfaces.ObtenerClienteInterface
import com.example.kerklyv5.interfaces.ObtenerOficiosInterface
import com.example.kerklyv5.interfaces.SesionAbiertaInterface
import com.example.kerklyv5.modelo.serial.ClienteModelo
import com.example.kerklyv5.modelo.serial.Oficio
import com.example.kerklyv5.modelo.usuarios
import com.example.kerklyv5.modelo.usuariosSqlite
import com.example.kerklyv5.ui.home.HomeFragment
import com.example.kerklyv5.url.Instancias
import com.example.kerklyv5.url.Url
import com.example.kerklyv5.vista.MainActivity
import com.example.kerklyv5.vista.fragmentos.*
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader

import java.io.InputStreamReader
import java.io.*
import java.text.DateFormat
import java.util.*


class SolicitarServicio : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivitySolicitarServicioBinding
    var b: Bundle? = null
    private lateinit var txt_nombre: TextView
    private lateinit var txt_correo: TextView
    lateinit var telefono: String
    private var nombre: String? = null
    private lateinit var correo: String
    private lateinit var id: String
    private lateinit var drawerLayout: DrawerLayout
    private var presupuestoListo = false
    private lateinit var dialog2: Dialog
    var NombreF: String? = null

    //Autenticacion para saber la hora activo
    var providers: MutableList<AuthUI.IdpConfig?>? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private val MY_REQUEST_CODE = 200

    //subir foto de perfil
    lateinit var fotoPerfil: ImageView
    var bitmap: Bitmap? = null
    var PICK_IMAGE_REQUEST = 1
    var filePath: Uri? = null
    lateinit var array: ArrayList<String>
    var photoUrl: String? = null
    private lateinit var tokenCliente: String
    private lateinit var nombreCompletoCliente: String
    val setProgressDialog = setProgressDialog()
    private lateinit var dataManager: DataManager
    lateinit var referencia : Instancias

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitarServicioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog2 = Dialog(this)
        setSupportActionBar(binding.appBarSolicitarServicio.toolbar)
        setProgressDialog.setProgressDialog(this)
        b = intent.extras!!
        telefono = b!!.getString("Telefono").toString()
        id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        presupuestoListo = b!!.getBoolean("PresupuestoListo")
        dataManager = DataManager(this)
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_solicitar_servicio)

        val view = navView.getHeaderView(0)
        txt_nombre = view.findViewById(R.id.nombre_txt)
        txt_correo = view.findViewById(R.id.correo_txt)
        fotoPerfil = view.findViewById(R.id.ImagenDePerfil)
        fotoPerfil.setOnClickListener {
           // SeleecionarFoto()
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.ordenesPendientesFragment,
                //R.id.fragment_historial,
                R.id.nav_mensajes
            ), drawerLayout
        )
        //  sesion(correo)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                // R.id.nav_home -> setFragmentHome(nombre.toString())
                R.id.nav_notificaciones -> setNoficiaciontes()
                R.id.nav_mensajes -> setContactosPresupuesto()
                R.id.ordenesPendientesFragment -> setFragmentOrdenesPendientes()
                R.id.fragment_historial -> setFragmentHistorial()
                R.id.nav_cerrarSesion -> cerrarSesion()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        if (presupuestoListo) {
            dialog2.setContentView(R.layout.presupuesto_solicitud)
            dialog2.show()
        }

        //Firebase
        providers = Arrays.asList(
            // EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        mAuth = FirebaseAuth.getInstance()

    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
           // setFragmentHome(telefono)
        }
        finish()
    }

    private fun SeleecionarFoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    pickPhotoFromGallery()
                }

                else -> requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            pickPhotoFromGallery()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pickPhotoFromGallery()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun pickPhotoFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Seleciona imagen"), PICK_IMAGE_REQUEST)
    }

    fun getStringImagen(bmp: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    fun cerrarVentana(v: View) {
        dialog2.dismiss()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.solicitar_servicio, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_solicitar_servicio)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setFragmentHome(telefono: String) {
        val f = HomeFragment()
        b!!.putString("Telefono", telefono)
        // Toast.makeText(this,"Nombre ${nombre.toString()}", Toast.LENGTH_LONG).show()
        f.arguments = b
        var fm = supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_content_solicitar_servicio, f).commit()
        }
    }
    private fun setFragmentOrdenesPendientes() {
        val f = OrdenesPendientesFragment()
        val args = Bundle()
        args.putString("Telefono", telefono)
        args!!.putString("nombreCompletoCliente", currentUser!!.displayName)
        args!!.putString("uid",currentUser!!.uid)
        f.arguments = args
        var fm = supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_content_solicitar_servicio, f).commit()
        }
    }

    private fun setFragmentHistorial() {
        val f = HistorialFragment()
        // val args = Bundle()
        //args.putString("Tel", telefono)
        f.arguments = b
        var fm = supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_content_solicitar_servicio, f).commit()
        }
    }

    private fun setFragmentMensajes() {
        /*val f = ListaChatsFragment()
        // val args = Bundle()
        //args.putString("Tel", telefono)
        f.arguments = b
        var fm = supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_content_solicitar_servicio,f).commit()
        }*/
    }

    private fun setContactosPresupuesto() {
        //val f = MensajesPresupuestoFragment()
        val f = ContactosFragment()
        f.arguments = b
        // b!!.putSerializable("arrayUsuarios", array)
        b!!.putInt("IdContrato", 1)
        b!!.putString("telefonoCliente", telefono)
        b!!.putString("urlFotoCliente", photoUrl)
        b!!.putString("nombreCompletoCliente", currentUser!!.displayName)
        b!!.putString("uid",currentUser!!.uid)
        b!!.putString("fotoCliente", currentUser!!.photoUrl.toString())
        b!!.putString("tokenCliente", tokenCliente)
        var fm = supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_content_solicitar_servicio, f).commit()
        }
    }

    private fun setNoficiaciontes() {

    }

    private fun sesion(correo: String) {
        println("correo: $correo  id : $id")
        val ROOT_URL = Url().url
        val adapter = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api = adapter.create(SesionAbiertaInterface::class.java)
        api.sesionAbierta(correo, id, currentUser!!.uid.toString(),
            object : retrofit.Callback<retrofit.client.Response?> {
                override fun success(
                    t: retrofit.client.Response?,
                    response2: retrofit.client.Response?
                ) {
                    var reader: BufferedReader? = null
                    var output = ""
                    try {
                        reader = BufferedReader(InputStreamReader(t?.body?.`in`()))
                        output = reader.readLine()
                         //Toast.makeText(this@SolicitarServicio,"sesion abierta"+ output,Toast.LENGTH_SHORT).show()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                override fun failure(error: RetrofitError) {
                    println("error $error")
                }

            }
        )
    }

    private fun cerrarSesion() {
        val ROOT_URL = Url().url
        val adapter = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api = adapter.create(CerrarSesionInterface::class.java)
        api.cerrar(
            currentUser!!.email.toString(),
            object : retrofit.Callback<retrofit.client.Response?> {
                override fun success(
                    t: retrofit.client.Response?,
                    response: retrofit.client.Response?
                ) {
                    var reader: BufferedReader? = null
                    var output = ""
                    try {
                        reader = BufferedReader(InputStreamReader(t?.body?.`in`()))
                        output = reader.readLine()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    Log.e("output", output)

                    if (output == "1") {
                        metodoSalir()
                        Toast.makeText(applicationContext, "Sesión cerrada", Toast.LENGTH_SHORT)
                            .show()
                        val i = Intent(applicationContext, MainActivity::class.java)
                        startActivity(i)
                        finish()
                    }
                }

                override fun failure(error: RetrofitError?) {

                }

            })
    }

    /*private fun getJson2() {
      //  setProgressDialog.setProgressDialog(this)
        val ROOT_URL = Url().url
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("$ROOT_URL/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val presupuestoGET = retrofit.create(ObtenerClienteInterface::class.java)
        val call = presupuestoGET.getCliente(telefono)
        call?.enqueue(object : Callback<List<ClienteModelo?>?> {

            override fun onResponse(
                call: Call<List<ClienteModelo?>?>, response: Response<List<ClienteModelo?>?>) {
                val postList: ArrayList<ClienteModelo> = response.body() as ArrayList<ClienteModelo>
                if(postList.size == null){
                    System.out.println("no hay nada")
                   setProgressDialog.dialog.dismiss()
                    //carsModels = response.body() as ArrayList<presupuestok>
                    //    Log.d("Lista", postList[0].toString())
                }else{
                    NombreF = postList[0].Nombre
                    val ap = postList[0].Apellido_Paterno
                    val am = postList[0].Apellido_Materno
                    val foto = postList[0].fotoPerfil
                    //    Log.d("nombre", n)
                    nombre = "$NombreF $ap $am"
                    correo = postList[0].Correo
                    nombreCompletoCliente = nombre as String
                 //   txt_nombre.text = nombre
                //    txt_correo.text = correo
                    if (foto ==null){
                      //  Toast.makeText(this@SolicitarServicio, "No hay foto de perfil", Toast.LENGTH_SHORT).show()
                        //hay que poner una imagen por defecto
                        if (photoUrl == null){
                            setProgressDialog.dialog.dismiss()
                        }else{
                            val foto2 = photoUrl
                           // cargarImagen(foto2!!)
                            setProgressDialog.dialog.dismiss()
                        }
                    }else{
                        //cargarImagen(foto)
                        setProgressDialog.dialog.dismiss()
                    }
                    Glide.with(this@SolicitarServicio)
                        .asBitmap()
                        .load(photoUrl)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                                // Aquí tienes el objeto Bitmap de la foto
                                // Puedes continuar trabajando con el bitmap según tus necesidades
                                // Por ejemplo, puedes convertir el Bitmap en un ByteArray
                                val outputStream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                                val photoByteArray = outputStream.toByteArray()
                                val usuarios: usuariosSqlite
                                val tel = telefono.toLong()
                                usuarios = usuariosSqlite(tel,photoByteArray, nombre!!,ap,am,correo)
                                dataManager.verificarSiElUsarioExiste(this@SolicitarServicio,fotoPerfil,txt_nombre,txt_correo, photoByteArray,usuarios,telefono,nombre.toString(),ap,am,correo)
                                // Luego, puedes guardar el photoByteArray en la base de datos SQLite o realizar otras operaciones
                            }
                        })
                    setFragmentHome(nombre!!)
                    sesion(correo)
                }
            }
            override fun onFailure(call: Call<List<ClienteModelo?>?>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Codigo de respuesta de error: $t",
                    Toast.LENGTH_SHORT
                ).show();
                setProgressDialog.dialog.dismiss()
            }
        })
    }*/
    private fun EnviarFotoPerfil(fotoPerfil: String?, telefonoCliente: String, nombref: String) {
        val ROOT_URL = Url().url
        //Mostrar el diálogo de progreso
        val loading = ProgressDialog.show(this, "Subiendo...", "Espere por favor...", false, false)
        val stringRequest: StringRequest = object : StringRequest(
            Request.Method.POST, ROOT_URL + "/actualizacionFoto.php",
            object : com.android.volley.Response.Listener<String?> {
                override fun onResponse(s: String?) {
                    //Descartar el diálogo de progreso
                    loading.dismiss()
                    //Mostrando el mensaje de la respuesta
                    //Toast.makeText(this@SolicitarServicio, s, Toast.LENGTH_LONG).show()
                    // System.out.println("error aqui 1 $s")
                }
            },
            object : com.android.volley.Response.ErrorListener {
                override fun onErrorResponse(volleyError: VolleyError) {
                    //Descartar el diálogo de progreso
                    loading.dismiss()

                    //Showing toast
                    //Toast.makeText(this@SolicitarServicio, volleyError.message.toString(), Toast.LENGTH_LONG).show()
                    //   System.out.println("error aqui 2 ${volleyError.message}")
                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {

                //Creación de parámetros
                val params: MutableMap<String, String> = Hashtable<String, String>()

                //Agregando de parámetros
                if (fotoPerfil != null) {
                    params["fotoPerfil"] = fotoPerfil
                }
                params["telefonoCliente"] = telefonoCliente
                params["nombre"] = nombref
                //Parámetros de retorno
                return params
            }
        }
        //Creación de una cola de solicitudes
        val requestQueue = Volley.newRequestQueue(this)
        //Agregar solicitud a la cola
        requestQueue.add(stringRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data!!
            try {
                //Cómo obtener el mapa de bits de la Galería
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                if (bitmap!!.width <= 500) {
                    //  Toast.makeText(this, "Es mejor que 500", Toast.LENGTH_SHORT).show()
                    val imagen = getStringImagen(bitmap!!)!!
                    // val imagen = getStringImagen(bitmap!!)!!
                    if (bitmap!!.width > bitmap.height) {
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.height, bitmap.height)

                    } else if (bitmap.width < bitmap.height) {
                        bitmap = Bitmap.createBitmap(
                            bitmap, 0, 0, bitmap.width, bitmap.width
                        )
                    }
                    //Configuración del mapa de bits en ImageView
                    val roundedDrawable: RoundedBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(resources, bitmap)
                    roundedDrawable.cornerRadius = bitmap!!.getWidth().toFloat()
                    EnviarFotoPerfil(imagen, telefono, NombreF.toString())
                    fotoPerfil.setImageDrawable(roundedDrawable)
                } else {
                    // Toast.makeText(this, "Es mayor que de 500", Toast.LENGTH_SHORT).show()
                    val bmp = Bitmap.createScaledBitmap(bitmap, 500, 500, true)
                    val imagen = getStringImagen(bmp!!)!!
                    var originalBitmap = bmp

                    // val imagen = getStringImagen(bitmap!!)!!
                    if (originalBitmap!!.width > originalBitmap.height) {
                        originalBitmap = Bitmap.createBitmap(
                            originalBitmap,
                            0,
                            0,
                            originalBitmap.height,
                            originalBitmap.height
                        )

                    } else if (originalBitmap.width < originalBitmap.height) {
                        originalBitmap = Bitmap.createBitmap(
                            originalBitmap, 0, 0, originalBitmap.width, originalBitmap.width
                        )
                    }
                    //Configuración del mapa de bits en ImageView
                    val roundedDrawable: RoundedBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(resources, originalBitmap)
                    roundedDrawable.cornerRadius = originalBitmap!!.getWidth().toFloat()
                    EnviarFotoPerfil(imagen, telefono, NombreF.toString())
                    fotoPerfil.setImageDrawable(roundedDrawable)
                    // System.out.println("AQui la imagen" +filePath.toString())

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        //condicion para saber si se selecciono una cuenta
        if (requestCode == MY_REQUEST_CODE) {
            currentUser = mAuth!!.currentUser
            val ROOT_URL = Url().url
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
            val retrofit = Retrofit.Builder().baseUrl("$ROOT_URL/").client(client)
                .addConverterFactory(GsonConverterFactory.create()).build()
            val presupuestoGET = retrofit.create(ObtenerClienteInterface::class.java)
            val call = presupuestoGET.getCliente(telefono)
            call?.enqueue(object : Callback<List<ClienteModelo?>?> {
                override fun onResponse(
                    call: Call<List<ClienteModelo?>?>, response: Response<List<ClienteModelo?>?>
                ) {
                    val postList: ArrayList<ClienteModelo> =
                        response.body() as ArrayList<ClienteModelo>
                    if (postList.size == null) {
                        setProgressDialog.dialog.dismiss()
                        //carsModels = response.body() as ArrayList<presupuestok>
                        //    Log.d("Lista", postList[0].toString())
                    } else {
                        NombreF = postList[0].Nombre
                        val ap = postList[0].Apellido_Paterno
                        val am = postList[0].Apellido_Materno
                        val foto = postList[0].fotoPerfil
                        nombre = "$NombreF $ap $am"
                        correo = postList[0].Correo

                        if (correo == currentUser!!.email) {
                           // nombreCompletoCliente = nombre as String
                            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w(
                                        TAG,
                                        "Fetching FCM registration token failed",
                                        task.exception
                                    )
                                    return@OnCompleteListener
                                }
                                tokenCliente = task.result
                                referencia = Instancias()
                                val dataRefe = referencia.referenciaInformacionDelUsuario(currentUser!!.uid)
                                val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
                                val u = usuarios(telefono, currentUser!!.email.toString(), nombre.toString(), currentUser!!.photoUrl.toString(), currentDateTimeString, tokenCliente, currentUser!!.uid)
                               dataRefe.setValue(u) { error, ref ->
                                    setProgressDialog.dialog.dismiss()
                                }
                            })

                            photoUrl = currentUser!!.photoUrl.toString()
                            val telefono: Long = telefono.toLong()
                            Glide.with(this@SolicitarServicio)
                                .asBitmap()
                                .load(photoUrl)
                                .into(object : SimpleTarget<Bitmap>() {
                                    override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                                        // Aquí tienes el objeto Bitmap de la foto
                                        // Puedes continuar trabajando con el bitmap según tus necesidades
                                        // Por ejemplo, puedes convertir el Bitmap en un ByteArray
                                        val outputStream = ByteArrayOutputStream()
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                                        val photoByteArray = outputStream.toByteArray()
                                        val usuarios: usuariosSqlite
                                        usuarios = usuariosSqlite(currentUser!!.uid, telefono,
                                            photoByteArray,
                                            nombre.toString(),
                                            ap,
                                            am,
                                            correo
                                        )
                                     /*   dataManager.verificarSiElUsarioExiste(
                                            this@SolicitarServicio,
                                            fotoPerfil,
                                            txt_nombre,
                                            txt_correo,
                                            photoByteArray,
                                            usuarios,
                                            telefono.toString(),
                                            nombre.toString(),
                                            ap,
                                            am,
                                            correo
                                        )*/
                                        getOficios()
                                        setFragmentHome(telefono.toString())


                                    }
                                })
                            //  cargarImagen(foto2!!)
                            txt_correo.text =currentUser!!.email.toString()
                            txt_nombre.text = nombre
                            setProgressDialog.dialog.dismiss()
                            sesion(correo)
                        } else {
                            cerrarSesion()
                            Toast.makeText(
                                this@SolicitarServicio,
                                "Este Correo ${currentUser!!.email} no pertenece a esta cuenta",
                                Toast.LENGTH_LONG
                            ).show()
                            setProgressDialog.dialog.dismiss()
                        }
                    }
                }

                override fun onFailure(call: Call<List<ClienteModelo?>?>, t: Throwable) {
                    Toast.makeText(
                        applicationContext,
                        "Codigo de respuesta de error: $t",
                        Toast.LENGTH_SHORT
                    ).show();

                    setProgressDialog.dialog.dismiss()
                }
            })

        }
    }


    override fun onStart() {
        super.onStart()
        currentUser = mAuth!!.currentUser
        if (currentUser != null) {
           /* Glide.with(this@SolicitarServicio)
                .asBitmap()
                .load(currentUser!!.photoUrl)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(
                        bitmap: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        val photoByteArray = outputStream.toByteArray()

                    }
                })*/
            sesion(currentUser!!.email.toString())

        //    dataManager.mostrarInformacion(this, fotoPerfil,txt_nombre,txt_correo)
          //  sesionAbierta(currentUser!!.email.toString())
            var firebaseMessaging = FirebaseMessaging.getInstance().subscribeToTopic("EnviarNoti")
            firebaseMessaging.addOnCompleteListener {
                //Toast.makeText(this@MainActivityChats, "Registrado:", Toast.LENGTH_SHORT).show()
            }
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                   // Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                tokenCliente = task.result
                val name = currentUser!!.displayName
                val email = currentUser!!.email
                photoUrl = currentUser!!.photoUrl.toString()
                val uid = currentUser!!.uid
                val foto = photoUrl.toString()

                val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
                val u = usuarios(
                    telefono,
                    email.toString(),
                    name.toString(),
                    foto,
                    currentDateTimeString,
                    tokenCliente,
                    uid
                )
                referencia = Instancias()
                val dataRefe = referencia.referenciaInformacionDelUsuario(currentUser!!.uid)
                setFragmentHome(telefono)
                dataRefe.setValue(u) { error, ref ->
                    // Toast.makeText(this@SolicitarServicio, "Bienvenido $token", Toast.LENGTH_SHORT) .show()
                    setProgressDialog.dialog.dismiss()
                    //dataManager.getAllOficios()
                }
            })
            cargarImagen(currentUser!!.photoUrl.toString())
            txt_nombre.text = currentUser!!.displayName
            txt_correo.text = currentUser!!.email.toString()
        }else {
            muestraOpciones()
            setProgressDialog.dialog.dismiss()
        }
    }
    fun muestraOpciones() {
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(providers!!)
                .build(),MY_REQUEST_CODE
        )
    }
    fun metodoSalir() {
        AuthUI.getInstance()
            .signOut(applicationContext)
            .addOnCompleteListener {// muestraOpciones()
                }.addOnFailureListener { e ->
                Toast.makeText(
                    applicationContext, ""
                            + e.message, Toast.LENGTH_LONG
                ).show()
            }
        dataManager.deleteAllTablas()

        finish()
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
               val listaArrayOficios = response.body() as ArrayList<Oficio>
                if (listaArrayOficios== null){

                }else{
//                inicio = "(?i)(\\W|^)("
//                pal = ""
//                final ="\\smía|ostras)(\\W|\$)"
                var oficio = ""
                var oficios: MisOficios
               // dataManager.deleteAllOficios()
                for (i in 0 until listaArrayOficios!!.size){
                //    oficio = listaArrayOficios[i].nombreO
                  //  pal = pal+ oficio +"|"
                 //   println("oficio $oficio Palabras Claves: " + listaArrayOficios[i].PalabrasClaves)
                    oficios = MisOficios(i,listaArrayOficios[i].PalabrasClaves,listaArrayOficios[i].nombreO)
                    dataManager.insertOrUpdateOficio(oficios,listaArrayOficios[i].PalabrasClaves,listaArrayOficios[i].nombreO)
               setFragmentHome(telefono)
                }
                //expresion = "$inicio$pal"+"$final"
               // println("expresion armada $inicio"+pal+final)
             setProgressDialog.dialog.dismiss()
               // var lista= dataManager.getAllOficios()
               // val aa = AdapterSpinnercopia(requireActivity(), lista)
                //spinner.adapter = aa
             }
            }

            override fun onFailure(call: Call<List<Oficio?>?>, t: Throwable) {
                setProgressDialog.dialog.dismiss()
                Log.d("error del retrofit", t.toString())
                // Toast.makeText(requireActivity(), t.toString(), Toast.LENGTH_LONG).show()
            }

        })
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun cargarImagen(urlImagen: String) {
        val file: Uri
        file = Uri.parse(urlImagen)
        System.out.println("imagen aqui: "+ file)
        Picasso.get().load(urlImagen).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {

            }
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                val multi = MultiTransformation<Bitmap>(RoundedCornersTransformation(128, 0, RoundedCornersTransformation.CornerType.ALL))
                Glide.with(this@SolicitarServicio).load(file)
                    .apply(RequestOptions.bitmapTransform(multi))
                    .into(fotoPerfil)
                setProgressDialog.dialog.dismiss()
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                System.out.println("Respuesta error 3 "+ e.toString())
                setProgressDialog.dialog.dismiss()
                //Toast.makeText(this@SolicitarServicio, "si hay foto respuesta 3", Toast.LENGTH_SHORT).show()
            }

        })
    }


}