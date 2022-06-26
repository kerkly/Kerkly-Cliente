package com.example.kerklyv5

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
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
import com.example.kerklyv5.databinding.ActivitySolicitarServicioBinding
import com.example.kerklyv5.interfaces.CerrarSesionInterface
import com.example.kerklyv5.interfaces.ObtenerClienteInterface
import com.example.kerklyv5.interfaces.SesionAbiertaInterface
import com.example.kerklyv5.modelo.serial.ClienteModelo
import com.example.kerklyv5.ui.home.HomeFragment
import com.example.kerklyv5.url.Url
import com.example.kerklyv5.vista.MainActivity
import com.example.kerklyv5.vista.fragmentos.*
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
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
import java.io.IOException
import java.io.InputStreamReader


class SolicitarServicio : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivitySolicitarServicioBinding
    private lateinit var b: Bundle
    private lateinit var txt_nombre: TextView
   private lateinit var txt_correo: TextView
    lateinit var telefono: String
    private lateinit var nombre: String
    private lateinit var correo: String
    private lateinit var id: String
    private lateinit var drawerLayout: DrawerLayout
    private var presupuestoListo = false
    private lateinit var dialog: Dialog

    //subir foto
    //subir foto de perfil
    lateinit var fotoPerfil: ImageView
    var bitmap: Bitmap? = null
    var PICK_IMAGE_REQUEST = 1
    var filePath: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitarServicioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = Dialog(this)
        setSupportActionBar(binding.appBarSolicitarServicio.toolbar)

        b = intent.extras!!
        telefono = b.getString("Telefono")!!
        id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        presupuestoListo = b.getBoolean("PresupuestoListo")


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
                R.id.nav_home -> setFragmentHome()
                R.id.nav_notificaciones -> setNoficiaciontes()
                R.id.ordenesPendientesFragment -> setFragmentOrdenesPendientes()
                R.id.fragment_historial -> setFragmentHistorial()
                R.id.nav_mensajes -> setMensajesPresupuesto()
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
        setFragmentHome()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data!!
            try {
                //Cómo obtener el mapa de bits de la Galería
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                // imagen = getStringImagen(bitmap!!)!!
                var originalBitmap = bitmap
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
                fotoPerfil!!.setImageDrawable(roundedDrawable)

                //  enviarTodoFoto(imagen,"goku@gmail.com","Luis","salazar","Luis","7471503417","h","Alfredo@0599","0","dssdfad")
                System.out.println("AQui la imagen" +filePath.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
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

    private fun setFragmentHome() {
        val f = HomeFragment()
       // val args = Bundle()
        //args.putString("Tel", telefono)
        f.arguments = b
        var fm = supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_content_solicitar_servicio,f).commit()
        }
    }

    private fun setFragmentOrdenesPendientes() {
        val f = OrdenesPendientesFragment()
        // val args = Bundle()
        //args.putString("Tel", telefono)
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
        val f = ListaChatsFragment()
        // val args = Bundle()
        //args.putString("Tel", telefono)
        f.arguments = b
        var fm = supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_content_solicitar_servicio,f).commit()
        }
    }

    private fun setMensajesPresupuesto() {
        val f = MensajesPresupuestoFragment()
        f.arguments = b
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
                                Toast.makeText(applicationContext, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                                val i = Intent(applicationContext, MainActivity::class.java)
                                startActivity(i)
                            }
                        }

                        override fun failure(error: RetrofitError?) {
                            TODO("Not yet implemented")
                        }

                    })
    }

    private fun getJson() {
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

                //carsModels = response.body() as ArrayList<presupuestok>
            //    Log.d("Lista", postList[0].toString())
                val n = postList[0].Nombre
                val ap = postList[0].Apellido_Paterno
                val am = postList[0].Apellido_Materno
                val foto = postList[0].fotoPerfil

            //    Log.d("nombre", n)


                nombre = "$n $ap $am"

                correo = postList[0].Correo

                txt_nombre.text = nombre
                txt_correo.text = correo
                if (foto ==null){
                    //Toast.makeText(this@SolicitarServicio, "No hay foto de perfil", Toast.LENGTH_SHORT).show()
                    //hay que poner una imagen por defecto
                }else{
                    cargarImagen(foto);
                }

                sesion(correo)

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
                val roundedDrawable: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
                roundedDrawable.cornerRadius = bitmap!!.getWidth().toFloat()
                fotoPerfil!!.setImageDrawable(roundedDrawable)
              //  fotoPerfil.setImageBitmap(bitmap)
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
               // fotoPerfil.setImageDrawable(placeHolderDrawable)
                System.out.println("Respuesta 2 " )
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                System.out.println("Respuesta error 3 "+ e.toString())
            }
        })
    }
}