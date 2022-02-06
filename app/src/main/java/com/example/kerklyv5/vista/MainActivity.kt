package com.example.kerklyv5.vista

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
/*import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest*/
import com.example.kerklyv5.R
import com.example.kerklyv5.SolicitarServicio
import com.example.kerklyv5.controlador.*
import com.example.kerklyv5.modelo.Cliente
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


/*
* Validar credenciales de acceso -listo
* Crear cuenta                   -listo
* Restablecer la contraseña      -listo
* Prueba sin registro            -listo
*
* */

class MainActivity : AppCompatActivity() {
    private lateinit var editUsuario: TextInputEditText
    private lateinit var editContra: TextInputEditText
    private lateinit var layoutUsuario: TextInputLayout
    private lateinit var layoutContra: TextInputLayout
    private lateinit var usuario: Cliente
    private lateinit var editTelefono: TextInputEditText
    private lateinit var layoutTelefono: TextInputLayout
    private lateinit var dialog: Dialog
    private lateinit var barra: ProgressDialog
    private lateinit var controlador: MainActivityControlador
    private lateinit var layout_nombre: TextInputLayout
    private lateinit var layout_ap: TextInputLayout
    private lateinit var layout_am: TextInputLayout
    private lateinit var edit_nombre: TextInputEditText
    private lateinit var edit_ap: TextInputEditText
    private lateinit var edit_am: TextInputEditText
    private lateinit var id: String

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        controlador = MainActivityControlador()

        id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        controlador.verificarSesion(id, this)


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editUsuario = findViewById(R.id.input_user)
        editContra = findViewById(R.id.input_password)
        layoutUsuario = findViewById(R.id.textnputUser)
        layoutContra = findViewById(R.id.textnputPassword)
        dialog = Dialog(this)




        //  val constraints = Constraints.Builder().setRequiresCharging(true).build()

        //val uploadWork: WorkRequest = OneTimeWorkRequestBuilder<PresupuestoWorker>().setConstraints(constraints).build()
        //WorkManager.getInstance(this).enqueue(uploadWork)
        //startService(Intent(this,PresupuestoService::class.java))
        //startService(Intent(this, PresupuestoServicio::class.java))
    }

    fun click (view: View) {
        val usuario = editUsuario.text.toString()

        if(usuario.isEmpty()) {
            layoutUsuario.error = getText(R.string.campo_requerido)
        } else {
            layoutUsuario.error = null
        }

        val contra = editContra.text.toString()

        if (contra.isEmpty()) {
            layoutContra.error = getText(R.string.campo_requerido)
        } else {
            layoutContra.error = null
        }

        if (!usuario.isEmpty() && !contra.isEmpty()) {
            if (usuario.length != 10) {
                layoutUsuario.error = getText(R.string.telefono_error)

            } else {
                layoutUsuario.error = null

                //val b = Bundle()
                // b.putString("Telefono", usuario)
                // val i = Intent(this, SolicitarServicio::class.java)
                // i.putExtras(b)
                val u = Cliente(editUsuario.text.toString(), editContra.text.toString())
                controlador.verficiarUsuario(u, this)
                //startActivity(i)
            }
        }
        //  usuario = Cliente(editUsuario.text.toString(), editContra.text.toString())
        // controlador.verficiarUsuario(usuario, this)

    }

    @SuppressLint("StaticFieldLeak")
    inner class RetreiveFeedTask: AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String? {
            //controlador.getNombreNoR(usuario)
            controlador.pruebaRegistrarNumero(usuario,
                applicationContext, layoutTelefono)
            return null
        }

        override fun onPostExecute(result: String?) {
            barra.dismiss()
        }

    }


    fun crearCuenta(view: View) {
        val intent = Intent(this, Registro::class.java)
        startActivity(intent)
    }

    fun restablecerContrasenia (view: View) {
        val intent = Intent(this, RecuperarCuenta::class.java)
        startActivity(intent)
    }

    fun servicioExpress (view: View) {
        var notificacion = Notificacion(this)
        dialog.setContentView(R.layout.telefono_no_registrado_confirmar)
        dialog.show()
    }


    //modificar este metodo par que solo ingrese el numero
    fun aceptarNoRegistrado (view: View) {
        editTelefono = dialog.findViewById(R.id.edit_telefonoNoRegistrado)
        layoutTelefono = dialog.findViewById(R.id.layoutTelefonoNoRegistrado)

        var band = false

        if (!controlador.verificarNumeroTelNoR(this)) {
            editTelefono.isEnabled = true
            usuario = Cliente(editTelefono.text.toString())

            if (usuario.getTelefonoNoR().isEmpty()) {
                layoutTelefono.error = getString(R.string.campo_requerido)
            } else {
                layoutTelefono.error = null
                if (usuario.getTelefonoNoR().length != 10) {
                    layoutTelefono.error = getText(R.string.telefono_error)
                } else {
                    layoutTelefono.error = null
                }

            }
        } else {
            editTelefono.isEnabled = false
        }

        if (!(usuario.getTelefonoNoR().isEmpty())) {
            barra = ProgressDialog.show(this, "", "Ingresando...")
            val task = RetreiveFeedTask()
            task.execute()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }
}