package com.example.kerklyv5

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
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
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
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
import com.example.kerklyv5.databinding.ActivitySolicitarServicioBinding
import com.example.kerklyv5.interfaces.CerrarSesionInterface
import com.example.kerklyv5.interfaces.ObtenerClienteInterface
import com.example.kerklyv5.interfaces.SesionAbiertaInterface
import com.example.kerklyv5.modelo.actualizarhora
import com.example.kerklyv5.modelo.serial.ClienteModelo
import com.example.kerklyv5.modelo.usuarios
import com.example.kerklyv5.ui.home.HomeFragment
import com.example.kerklyv5.url.Url
import com.example.kerklyv5.vista.MainActivity
import com.example.kerklyv5.vista.fragmentos.*
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
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
    private lateinit var dialog: Dialog
    var NombreF: String?=null

    //Autenticacion para saber la hora activo
    var providers: MutableList<AuthUI.IdpConfig?>? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private val MY_REQUEST_CODE = 200

    //subir foto
    //subir foto de perfil
    lateinit var fotoPerfil: ImageView
    var bitmap: Bitmap? = null
    var PICK_IMAGE_REQUEST = 1
    var filePath: Uri? = null
    lateinit var array: ArrayList<String>
    lateinit var photoUrl: String




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitarServicioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = Dialog(this)
        setSupportActionBar(binding.appBarSolicitarServicio.toolbar)

        b = intent.extras!!
        telefono = b!!.getString("Telefono")!!
        id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        presupuestoListo = b!!.getBoolean("PresupuestoListo")


        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_solicitar_servicio)

        val view = navView.getHeaderView(0)
        txt_nombre = view.findViewById(R.id.nombre_txt)
        txt_correo = view.findViewById(R.id.correo_txt)
        fotoPerfil = view.findViewById(R.id.ImagenDePerfil)
        fotoPerfil.setOnClickListener {
            SeleecionarFoto()
        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                //R.id.ordenesPendientesFragment,
                //  R.id.historialFragment,
                // R.id.nav_mensajes
            ), drawerLayout
        )


        // sesion(correo)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> setFragmentHome(nombre.toString())
                R.id.nav_notificaciones -> setNoficiaciontes()
                R.id.nav_mensajes -> setMensajesPresupuesto()
                R.id.ordenesPendientesFragment -> setFragmentOrdenesPendientes()
                R.id.fragment_historial -> setFragmentHistorial()
                R.id.nav_cerrarSesion -> cerrarSesion()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        if (presupuestoListo) {
            dialog.setContentView(R.layout.presupuesto_solicitud)
            dialog.show()
        }

        getJson()

        //Firebase
        providers = Arrays.asList(
            // EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        mAuth = FirebaseAuth.getInstance()
/*
        //Primero Obtendremos la lista de numeros de telefonos
        val firebaseDatabaseLista: FirebaseDatabase
        var databaseReferenceLista: Task<DataSnapshot>
        firebaseDatabaseLista = FirebaseDatabase.getInstance()
        array = ArrayList<String>()

        databaseReferenceLista = firebaseDatabaseLista.getReference("UsuariosR").child("7471503418")
            .child("Lista de Usuarios").get().addOnCompleteListener { task ->

                for (snapshot in task.result.children) {

                    //array = arrayListOf(snapshot.key!!)
                    //  println("claves" + snapshot.key)
                    // myAdapter.notifyDataSetChanged();
                    // println(" datos: " + snapshot.value)
                    println(" tel: " + snapshot.child("telefono").value)
                    array.add(snapshot.child("telefono").value.toString())


                }


            }*/
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
        }else {
            // Se llamará a la función para APIs 22 o inferior
            // Esto debido a que se aceptaron los permisos
            // al momento de instalar la aplicación
            pickPhotoFromGallery()
        }

    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ isGranted ->

        if (isGranted){
            pickPhotoFromGallery()
        }else{
            Toast.makeText(
                this,
                "Permission denied",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickPhotoFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Seleciona imagen"), PICK_IMAGE_REQUEST)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data!!
            try {
                //Cómo obtener el mapa de bits de la Galería
              var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                if (bitmap!!.width <= 500){
                  //  Toast.makeText(this, "Es mejor que 500", Toast.LENGTH_SHORT).show()
                    val imagen = getStringImagen( bitmap!!)!!
                    // val imagen = getStringImagen(bitmap!!)!!
                    if (bitmap!!.width > bitmap.height) {
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.height, bitmap.height)

                    } else if (bitmap.width < bitmap.height) {
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.width
                        )
                    }

                    //Configuración del mapa de bits en ImageView
                    val roundedDrawable: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)

                    roundedDrawable.cornerRadius = bitmap!!.getWidth().toFloat()
                    EnviarFotoPerfil(imagen, telefono, NombreF.toString())
                    fotoPerfil.setImageDrawable(roundedDrawable)

                }else{
                   // Toast.makeText(this, "Es mayor que de 500", Toast.LENGTH_SHORT).show()
                    val bmp= Bitmap.createScaledBitmap(bitmap, 500, 500,true)
                    val imagen = getStringImagen(bmp!!)!!
                    var originalBitmap = bmp

                    // val imagen = getStringImagen(bitmap!!)!!
                  if (originalBitmap!!.width > originalBitmap.height) {
                        originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.height, originalBitmap.height)

                    } else if (originalBitmap.width < originalBitmap.height) {
                        originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.width
                        )
                    }

                    //Configuración del mapa de bits en ImageView
                    val roundedDrawable: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, originalBitmap)

                    roundedDrawable.cornerRadius = originalBitmap!!.getWidth().toFloat()
                    EnviarFotoPerfil(imagen, telefono, NombreF.toString())
                    fotoPerfil.setImageDrawable(roundedDrawable)
                    System.out.println("AQui la imagen" +filePath.toString())



                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


        if (requestCode == MY_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (requestCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                //    Toast.makeText(this, "saludos" + user!!.email, Toast.LENGTH_SHORT).show()
                //correo = currentUser!!.getEmail()!!
                //verficar numero

            }


        }
    }

    fun getStringImagen(bmp: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }
    fun cerrarVentana(v: View) {
        dialog.dismiss()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val alert: AlertDialog.Builder = AlertDialog.Builder(this)
            alert.setTitle(getString(R.string.cerrar_app))
            alert.setMessage(getString(R.string.mensaje_alertaCerrarApp))
            alert.setCancelable(false)
            alert.setPositiveButton(getString(R.string.confirmar_alertCerrarApp)) { dialogo1, id -> finish() }
            alert.setNegativeButton(getString(R.string.cancelar_alertCerrarApp)) { dialogo1, id -> dialogo1.dismiss() }
            alert.show()
        }
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

    private fun setFragmentHome(nombre: String) {
        val f = HomeFragment()
        b!!.putString("Nombre", nombre.toString())

       // Toast.makeText(this,"Nombre ${nombre.toString()}", Toast.LENGTH_LONG).show()
        f.arguments = b
        var fm = supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_content_solicitar_servicio,f).commit()
        }
    }

    private fun setFragmentOrdenesPendientes() {
        val f = OrdenesPendientesFragment()
        // val args = Bundle()
        //args.putString("Tel", telefono)
        b!!.putString("Nombre", nombre.toString())
        f.arguments = b
        var fm = supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_content_solicitar_servicio,f).commit()
        }
    }

    private fun setFragmentHistorial() {
        val f = HistorialFragment()
        // val args = Bundle()
        //args.putString("Tel", telefono)
        f.arguments = b
        var fm = supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_content_solicitar_servicio,f).commit()
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

    private fun setMensajesPresupuesto() {
        //val f = MensajesPresupuestoFragment()
        val f = MensajesFragment()
        f.arguments = b
       // b!!.putSerializable("arrayUsuarios", array)
        b!!.putInt("IdContrato", 1)
        b!!.putString("telefonoCliente", telefono)
        b!!.putString("urlFotoCliente", photoUrl)
      //  println("tamaño array ${array.size}")
        var fm = supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_content_solicitar_servicio,f).commit()
        }
    }

    private fun setNoficiaciontes() {

    }



    private fun sesion(correo: String) {
        val ROOT_URL = Url().url
        val adapter = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api = adapter.create(SesionAbiertaInterface::class.java)
        api.sesionAbierta(
            correo,
            id,
            object : retrofit.Callback<retrofit.client.Response?> {
                override fun success(t: retrofit.client.Response?, response2: retrofit.client.Response?) {
                    var reader: BufferedReader? = null
                    var output = ""
                    try {
                        reader = BufferedReader(InputStreamReader(t?.body?.`in`()))
                        output = reader.readLine()


                        Toast.makeText(this@SolicitarServicio,output,Toast.LENGTH_SHORT).show()

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
        Log.e("correo", correo)
        val ROOT_URL = Url().url
        val adapter = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api = adapter.create(CerrarSesionInterface::class.java)
        api.cerrar(correo,
                    object: retrofit.Callback<retrofit.client.Response?> {
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
                                Toast.makeText(applicationContext, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                                val i = Intent(applicationContext, MainActivity::class.java)
                                startActivity(i)
                                finish()
                            }
                        }

                        override fun failure(error: RetrofitError?) {

                        }

                    })
    }

    private fun getJson() {
        System.out.println("entro en metodo JSON")
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
                call: Call<List<ClienteModelo?>?>,
                response: Response<List<ClienteModelo?>?>
            ) {

                val postList: ArrayList<ClienteModelo> = response.body() as ArrayList<ClienteModelo>

                if(postList.size == null){
                    System.out.println("no hay nada")
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

                    txt_nombre.text = nombre
                    txt_correo.text = correo
                    if (foto ==null){
                      //  Toast.makeText(this@SolicitarServicio, "No hay foto de perfil", Toast.LENGTH_SHORT).show()
                        //hay que poner una imagen por defecto
                    }else{
                        cargarImagen(foto)
                    }

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
            }

        })
    }

    private fun cargarImagen(urlImagen: String) {
        val file: Uri
        file = Uri.parse(urlImagen)
        System.out.println("imagen aqui: "+ file)

        Picasso.get().load(urlImagen).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                // loaded bitmap is here (bitmap)
               // fotoPerfil.setImageBitmap(bitmap)
                System.out.println("Respuesta 1 " )
                //Configuración del mapa de bits en ImageView
                // val imagen = getStringImagen(bitmap!!)!!
            /*   var bitmapO = bitmap
                if (bitmapO != null) {
                    if (bitmapO.width > bitmapO.height) {
                        bitmapO = Bitmap.createBitmap(bitmapO, 0, 0, bitmapO.height, bitmapO.height)

                    } else
                        if (bitmapO.width < bitmapO.height) {
                            bitmapO = Bitmap.createBitmap(bitmapO, 0, 0, bitmapO.width, bitmapO.width)
                        }
                    val roundedDrawable: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmapO)
                    roundedDrawable.cornerRadius = bitmapO!!.getWidth().toFloat()
                  //  fotoPerfil!!.setImageDrawable(roundedDrawable)
                  //  Toast.makeText(this@SolicitarServicio, "si hay foto respuesta 1", Toast.LENGTH_SHORT).show()

                }*/

              //  fotoPerfil.setImageBitmap(bitmap)
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                val multi = MultiTransformation<Bitmap>(RoundedCornersTransformation(128, 0, RoundedCornersTransformation.CornerType.ALL))

                Glide.with(this@SolicitarServicio).load(file)
                    .apply(RequestOptions.bitmapTransform(multi))
                    .into(fotoPerfil)

                /* Picasso.get().load(file)
                    .resize(50,50)
                    .into(fotoPerfil)*/
                // Toast.makeText(this@SolicitarServicio, "si hay foto respuesta 2", Toast.LENGTH_SHORT).show()

            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                System.out.println("Respuesta error 3 "+ e.toString())
                Toast.makeText(this@SolicitarServicio, "si hay foto respuesta 3", Toast.LENGTH_SHORT).show()
            }


        })
    }



    private fun EnviarFotoPerfil(fotoPerfil: String?, telefonoCliente: String, nombref: String) {
        val ROOT_URL = Url().url
        //Mostrar el diálogo de progreso
        val loading = ProgressDialog.show(this, "Subiendo...", "Espere por favor...", false, false)
        val stringRequest: StringRequest = object : StringRequest(
            Request.Method.POST, ROOT_URL+"/actualizacionFoto.php",
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
                    System.out.println("error aqui 2 ${volleyError.message}")
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



    override fun onStart() {
        super.onStart()
        currentUser = mAuth!!.currentUser
        if(currentUser != null){
           // muestraOpciones()
            val name = currentUser!!.displayName
            val email = currentUser!!.email
             photoUrl = currentUser!!.photoUrl.toString()
            val uid = currentUser!!.uid
            val foto = photoUrl.toString()
            val database = FirebaseDatabase.getInstance()
            val databaseReference = database.getReference("UsuariosR").child(telefono)
            val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
            val usuario = usuarios()
            val u = usuarios(telefono, email.toString(), name.toString(), foto, currentDateTimeString)
            databaseReference.child("MisDatos").setValue(u) { error, ref ->
              //  Toast.makeText(this@SolicitarServicio, "Bienvenido $name", Toast.LENGTH_SHORT) .show()
            }
        }else {
            muestraOpciones()
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
            .addOnCompleteListener { muestraOpciones() }.addOnFailureListener { e ->
                Toast.makeText(
                    applicationContext, ""
                            + e.message, Toast.LENGTH_LONG
                ).show()
            }
    }
}