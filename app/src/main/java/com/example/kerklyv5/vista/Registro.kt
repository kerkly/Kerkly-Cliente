package com.example.kerklyv5.vista

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.example.kerklyv5.R
import com.example.kerklyv5.modelo.usuarios
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_registro.*
import java.io.IOException
import java.text.DateFormat
import java.util.*
import java.util.regex.Pattern


/*
* Obtener y validar valores
* Mandarlos al siguiente activity
* */
class Registro : AppCompatActivity() {
    private lateinit var editNombre: TextInputEditText
    private lateinit var editApellidoP: TextInputEditText
    private lateinit var editApellidoM: TextInputEditText
    private lateinit var editTelefono: TextInputEditText
    private lateinit var spinner: Spinner
    private lateinit var layout_nombre: TextInputLayout
    private lateinit var layout_Ap: TextInputLayout
    private lateinit var layout_Am: TextInputLayout
    private lateinit var layout_telefono: TextInputLayout

    //Contraseña
    private lateinit var contra1_contendor: TextInputLayout
    private lateinit var contra2_layot: TextInputLayout
    private lateinit var editContra1: TextInputEditText
    private lateinit var editContra2: TextInputEditText
    private lateinit var contra1: String
    private lateinit var contra2: String
    private val PASSWORD_PATTERN: String = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{6,}\$"

    //subir foto de perfil
    var iv: ImageView? = null
    var bitmap: Bitmap? = null
    var PICK_IMAGE_REQUEST = 1
    var filePath: Uri? = null

    var nombre: String = ""
    var apellidoP: String =""
    var apellidoM: String = ""
    var telefono: String = ""
    var genero: String = ""
    var id: String =  ""
    lateinit var bundle: Bundle
    var correo: String= ""

    //Trabajando con firebase
    var providers: MutableList<AuthUI.IdpConfig?>? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private val MY_REQUEST_CODE = 200
    private var name = null
    private var email = null
    private var photoUrl = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        editNombre = findViewById(R.id.edit_nombre)
        editApellidoP = findViewById(R.id.edit_apellidoP)
        editApellidoM = findViewById(R.id.edit_apellidpM)
        editTelefono = findViewById(R.id.edit_telefono)
        spinner = findViewById(R.id.spinnerRecuperarcuenta)
        layout_nombre = findViewById(R.id.layoutNombre)
        layout_Ap = findViewById(R.id.layoutAp)
        layout_Am = findViewById(R.id.layoutAm)
        layout_telefono = findViewById(R.id.layoutTelefono)

        editContra1 = findViewById(R.id.edit_contra1R)
        editContra2 = findViewById(R.id.edit_contra2R)
        contra1_contendor = findViewById(R.id.layout_contra1R)
        contra2_layot = findViewById(R.id.layout_contra2R)

        iv = findViewById(R.id.Animacion)
        subirFotoPerfil.setOnClickListener {
            showFileChooser()

        }

        id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        bundle = Bundle()
        providers = Arrays.asList(
            // EmailBuilder().build(),
            GoogleBuilder().build())
        mAuth = FirebaseAuth.getInstance()
    }

    private fun showFileChooser() {
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

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->

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
                //escalamos la imagen para que no pese tanto

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
                iv!!.setImageDrawable(roundedDrawable)

             //  enviarTodoFoto(imagen,"goku@gmail.com","Luis","salazar","Luis","7471503417","h","Alfredo@0599","0","dssdfad")
                System.out.println("AQui la imagen" +filePath.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        if (requestCode == MY_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (requestCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
            //    Toast.makeText(this, "saludos" + user!!.email, Toast.LENGTH_SHORT).show()
                correo = currentUser!!.getEmail()!!
                //verficar numero

            }


        }
    }
 /*   fun getStringImagen(bmp: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }*/


    fun clickContinuarRegistro (view: View) {
        var band = false
        nombre = editNombre.text.toString()
        apellidoP = editApellidoP.text.toString()
        apellidoM = editApellidoM.text.toString()
        telefono = editTelefono.text.toString()
        genero = spinner.selectedItem.toString()
        contra1 = editContra1.text.toString()
        contra2 = editContra2.text.toString()

        if (nombre.isEmpty()) {
            layout_nombre.error = getText(R.string.campo_requerido)
            band = true
        } else {
            band = false
            layout_nombre.error = null
        }

        if (apellidoP.isEmpty()) {
            layout_Ap.error = getText(R.string.campo_requerido)
            band = true
        } else {
            band = false
            layout_Ap.error = null
        }

        if (apellidoM.isEmpty()) {
            layout_Am.error = getText(R.string.campo_requerido)
            band = true
        } else {
            band = false
            layout_Am.error = null
        }

        if (telefono.isEmpty()) {
            layout_telefono.error = getText(R.string.campo_requerido)
            band = true
        } else {
            band = false
            layout_telefono.error = null
            if (telefono.length != 10) {
                layout_telefono.error = getText(R.string.telefono_error)

                band = true
            } else {
                band = false
                layout_telefono.error = null

            }

        }
        if (contra1.isEmpty()) {
            band = false
            contra1_contendor.error = getString(R.string.campo_requerido)
        } else {
            band = true
            contra1_contendor.error = null
        }
        if (contra2.isEmpty()) {
            contra2_layot.error = getString(R.string.campo_requerido)
            band = false

        } else {
            contra2_layot.error = null
            band = true

        }

        if (contra1 == contra2){
            if (band== true) {
                if (validarContra()) {
                    //Toast.makeText(this, "entro ", Toast.LENGTH_SHORT).show()
                  //  currentUser = mAuth!!.currentUser
                    val user = FirebaseAuth.getInstance().currentUser
                    correo = user!!.email!!
                    //Toast.makeText(this, "entro : $correo ", Toast.LENGTH_SHORT).show()

                    // correo = currentUser!!.getEmail()!!
                    bundle.putString("fotoperfil", filePath.toString())
                    bundle.putString("clave", "registrar")
                    bundle.putString("nombre", nombre)
                    bundle.putString("apellidoP", apellidoP)
                    bundle.putString("apellidoM", apellidoM)
                    bundle.putString("telefono", telefono)
                    bundle.putString("g", genero)
                    bundle.putString("correo", correo)
                    bundle.putString("id", id)
                    bundle.putString("contra1", contra1)
                   // muestraOpciones()

                    val intent = Intent(this, MainActivityVerificarSMS::class.java)
                    intent.putExtras(bundle)
                    startActivity(intent)
                   finish()


                }else{
                    contra1_contendor.error = getString(R.string.contra_invalida)
                }

            }
        }else{
            contra2_layot.error = "Contraseña Incorrecta"
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

     private fun validarContra(): Boolean {
            val pattern = Pattern.compile(PASSWORD_PATTERN)
            val matcher = pattern.matcher(contra1)
            return matcher.matches()
        }

    override fun onStart() {
        super.onStart()
        currentUser = mAuth!!.currentUser
        val user = FirebaseAuth.getInstance().currentUser
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            if (currentUser != null) {

                correo = currentUser!!.email!!
                val name = currentUser!!.displayName
                val email = currentUser!!.email
                val photoUrl = currentUser!!.photoUrl
                val uid = currentUser!!.uid
                val foto = photoUrl.toString()

                val database = FirebaseDatabase.getInstance()
                val databaseReference = database.getReference("UsuariosR").child(telefono)
                val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
                //val usuario = usuarios()

                //val u = usuarios(uid, email, name, foto, currentDateTimeString)
                databaseReference.child("MisDatos").setValue(usuarios(telefono.toString(), email.toString(), name.toString(), foto.toString(), currentDateTimeString.toString())) { error, ref -> //txtprueba.setText(uid + "latitud " + latitud + " longitud " + longitud);
                  //  Toast.makeText(this@Registro, "Bienvenido $name", Toast.LENGTH_SHORT) .show()
                }


            }else{
                muestraOpciones()
            }



        } else {
            Toast.makeText(this@Registro, "No hay conexion a Internet", Toast.LENGTH_LONG)
                .show()
        }





    }


}