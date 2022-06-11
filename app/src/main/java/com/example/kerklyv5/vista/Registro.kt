package com.example.kerklyv5.vista

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
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
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.kerklyv5.R
import com.example.kerklyv5.url.Url
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_registro.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


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

    //subir foto de perfil
    var iv: ImageView? = null
    var bitmap: Bitmap? = null
    var PICK_IMAGE_REQUEST = 1
    var filePath: Uri? = null
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

        iv = findViewById(R.id.Animacion)
        subirFotoPerfil.setOnClickListener {
            showFileChooser()

        }
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
                iv!!.setImageDrawable(roundedDrawable)

             //  enviarTodoFoto(imagen,"goku@gmail.com","Luis","salazar","Luis","7471503417","h","Alfredo@0599","0","dssdfad")
                System.out.println("AQui la imagen" +filePath.toString())
            } catch (e: IOException) {
                e.printStackTrace()
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
        var nombre: String = editNombre.text.toString()
        var apellidoP: String = editApellidoP.text.toString()
        var apellidoM: String = editApellidoM.text.toString()
        var telefono: String = editTelefono.text.toString()
        var genero: String = spinner.selectedItem.toString()

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

        if (!band) {

            val bundle = Bundle()

            bundle.putString("fotoperfil", filePath.toString())
            bundle.putString("Nombre", nombre)
            bundle.putString("Apellido Paterno", apellidoP)
            bundle.putString("Apellido Materno", apellidoM)
            bundle.putString("Teléfono", telefono)
            bundle.putString("Género", genero)

            val intent = Intent(this, Correo::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

   /* private fun enviarTodoFoto(fotoPerfil: String?, Correo: String, Nombre: String, Apellido_Paterno: String, Apellido_Materno: String, telefonoCliente: String, generoCliente: String, Contrasena: String, fue_NoRegistrado: String, deviceID: String) {
        val ROOT_URL = Url().url
        //Mostrar el diálogo de progreso
        val loading = ProgressDialog.show(this, "Registrando...", "Espere por favor...", false, false)
        val stringRequest: StringRequest = object : StringRequest(
            Request.Method.POST, ROOT_URL+"/LoginFoto.php",
            object : Response.Listener<String?> {
                override fun onResponse(s: String?) {
                    //Descartar el diálogo de progreso
                    loading.dismiss()
                    //Mostrando el mensaje de la respuesta
                    Toast.makeText(this@Registro, s, Toast.LENGTH_LONG).show()
                    System.out.println("error aqui 1 $s")
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(volleyError: VolleyError) {
                    //Descartar el diálogo de progreso
                    loading.dismiss()

                    //Showing toast
                    Toast.makeText(this@Registro, volleyError.message.toString(), Toast.LENGTH_LONG).show()
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


                params["Correo"] = Correo
                params["Nombre"] = Nombre
                params["Apellido_Paterno"] = Apellido_Paterno
                params["Apellido_Materno"] = Apellido_Materno
                params["telefonoCliente"] = telefonoCliente
                params["generoCliente"] = generoCliente
                params["Contrasena"] = Contrasena
                params["fue_NoRegistrado"] = fue_NoRegistrado
                params["deviceID"] = deviceID

                //Parámetros de retorno
                return params
            }
        }

        //Creación de una cola de solicitudes
        val requestQueue = Volley.newRequestQueue(this)

        //Agregar solicitud a la cola
        requestQueue.add(stringRequest)
    }*/
}