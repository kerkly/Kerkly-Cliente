package com.example.kerklyv5.vista

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kerklyv5.R
import com.example.kerklyv5.controlador.Login
import com.example.kerklyv5.express.PedirServicioExpress
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main_verificar_sms.*
import java.util.concurrent.TimeUnit

class MainActivityVerificarSMS : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    var storedVerificationId: String? = ""
    var TAG2= "MainActivityVerificarSMS"
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private val sms = 0
    lateinit var numeroNoregistrado: String
    lateinit var numeroRegistar: String
    lateinit var clave: String
    var claveNoRE = "sinRegistro"
    var claveRe = "registrar"
    var numeroSMS = ""
    private lateinit var dialog: Dialog

    var Callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            Log.d(TAG2, "onVerificationCompleted:$credential")

           // signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {

            Log.w(TAG2, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {

            } else if (e is FirebaseTooManyRequestsException) {

            }


        }


        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d(TAG2, "onCodeSent:$verificationId")


            storedVerificationId = verificationId
            resendToken = token
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_verificar_sms)
        val intent1 = getIntent()
        clave =  intent1.getStringExtra("clave")!!
        auth = Firebase.auth
        numeroIngresado()
        botonSMS.setOnClickListener{
            verificarcodigoSMS()
        }

        dialog = Dialog(this)
    }

    fun numeroIngresado(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            //El permiso no está aceptado.
            requestPermission(this)

        } else {
            val area: String= "+52"
            if (clave.equals(claveNoRE)){
                val intent1 = getIntent()
                numeroNoregistrado = intent1.getStringExtra("Teléfono No Registrado")!!
                numeroSMS = area+numeroNoregistrado
                sms(numeroSMS)
                Toast.makeText(this, "numero no registrado", Toast.LENGTH_LONG).show()
            }else{
                if (clave.equals(claveRe)){
                    val intent1 = getIntent()
                    numeroRegistar = intent1.getStringExtra("telefono")!!
                    numeroSMS = area+numeroRegistar
                    sms(numeroSMS)
                    Toast.makeText(this, "numero por registrar", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    fun sms(numero: String){
        //Toast.makeText(this, numero.toString(), Toast.LENGTH_SHORT).show()
        val opciones = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(numero)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(Callbacks)
            .build()
        auth.setLanguageCode("es")
        PhoneAuthProvider.verifyPhoneNumber(opciones)
        // baseDatos.push().setValue(num)
        AlertDialog.Builder(this)
            .setTitle("Recibir SMS")
            .setMessage("En un momento le llegará el SMS.")
            .setPositiveButton(android.R.string.ok,
                DialogInterface.OnClickListener { dialog, which ->
                    //botón OK pulsado
                })
            .setNegativeButton(android.R.string.cancel,
                DialogInterface.OnClickListener { dialog, which ->
                    //botón cancel pulsado
                })
            .show()
    }

    private fun verificarcodigoSMS() {
        if(edit_VerificarSMS.text!!.isEmpty()){
            Toast.makeText(this, "Ingrese el codigo", Toast.LENGTH_SHORT).show()
        }else {
            var b = Bundle()
            val codigo = edit_VerificarSMS.text.toString()
            val credential = PhoneAuthProvider
                .getCredential(storedVerificationId!!, codigo)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG2, "signInWithCredbtial:success")
                        val user = task.result?.user
                        AlertDialog.Builder(this)
                            .setTitle("Verificación de Código")
                            .setMessage("Código Verficado")
                            .setPositiveButton(android.R.string.ok,
                                DialogInterface.OnClickListener { dialog, which ->
                                    //botón OK pulsado
                                    if (clave.equals(claveNoRE)){
                                        val i = Intent(this, PedirServicioExpress::class.java)
                                        b.putString("Teléfono No Registrado", numeroNoregistrado)
                                        i.putExtras(b)
                                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(i)
                                        finish()
                                    }else{
                                        val intent1 = getIntent()
                                        if (clave.equals(claveRe)){

                                            val correo =  intent1.getStringExtra("correo")!!
                                            val nombre = intent1.getStringExtra("nombre")!!
                                            val apellidoP = intent1.getStringExtra("apellidoP")!!
                                            val apellidoM = intent1.getStringExtra("apellidoM")!!
                                            val telefono = intent1.getStringExtra("telefono")!!
                                            val g = intent1.getStringExtra("g")!!
                                            val contra1 = intent1.getStringExtra("contra1")!!
                                            val id = intent1.getStringExtra("id")!!
                                            var login = Login()
                                            login.InsertarMysql(correo,nombre,apellidoP,apellidoM,telefono,g,
                                                contra1, "0", id)

                                           dialog2()
                                        }
                                    }


                                })
                            .setNegativeButton(android.R.string.cancel,
                                DialogInterface.OnClickListener { dialog, which ->
                                    //botón cancel pulsado
                                })
                            .show()
                    } else {
                        Log.w(
                            TAG2, "signInWithCredbtial:failure",
                            task.exception
                        )
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {

                        }
                        AlertDialog.Builder(this)
                            .setTitle("Verificación de Código")
                            .setMessage("El código es incorrecto")
                            .setPositiveButton(android.R.string.ok,
                                DialogInterface.OnClickListener { dialog, which ->
                                    //botón OK pulsado
                                })
                            .setNegativeButton(android.R.string.cancel,
                                DialogInterface.OnClickListener { dialog, which ->
                                    //botón cancel pulsado
                                })
                            .show()
                    }
                }
        }
    }

    private fun requestPermission(contexto: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(contexto,
                Manifest.permission.RECEIVE_SMS)) {
            //El usuario ya ha rechazado el permiso anteriormente, debemos informarle que vaya a ajustes.

            AlertDialog.Builder(contexto)
                .setTitle("Alerta")
                .setMessage("Ve a configuracion y verifica los permisos de la aplicacion")
                .setPositiveButton(android.R.string.ok,
                    DialogInterface.OnClickListener { dialog, which ->

                    })
                .setNegativeButton(android.R.string.cancel,
                    DialogInterface.OnClickListener { dialog, which ->
                        //botón cancel pulsado
                    })
                .show()
        } else {
            //El usuario nunca ha aceptado ni rechazado, así que le pedimos que acepte el permiso.
            ActivityCompat.requestPermissions(contexto,
                arrayOf(Manifest.permission.RECEIVE_SMS),
                sms)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            sms -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //El usuario ha aceptado el permiso, no tiene porqué darle de nuevo al botón, podemos lanzar la funcionalidad desde aquí.
                } else {
                    //El usuario ha rechazado el permiso, podemos desactivar la funcionalidad o mostrar una vista/diálogo.
                    showDialogAlertSimple();

                }
                return
            }
            else -> {
                // Este else lo dejamos por si sale un permiso que no teníamos controlado.
            }
        }
    }

    fun showDialogAlertSimple() {
        AlertDialog.Builder(this)
            .setTitle("Recibir SMS")
            .setMessage("Diríjase a la configuracion para establecer el permiso.")
            .setPositiveButton(android.R.string.ok,
                DialogInterface.OnClickListener { dialog, which ->
                    //botón OK pulsado
                })
            .setNegativeButton(android.R.string.cancel,
                DialogInterface.OnClickListener { dialog, which ->
                    //botón cancel pulsado
                })
            .show()
    }
    fun dialog2(){
        dialog.setContentView(R.layout.cuenta_creada)
        dialog.show()
    }

    fun iniciarSesion (view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}