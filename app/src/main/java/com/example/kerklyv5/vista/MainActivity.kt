package com.example.kerklyv5.vista

/*import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest*/
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.kerklyv5.R
import com.example.kerklyv5.SolicitarServicio
import com.example.kerklyv5.controlador.MainActivityControlador
import com.example.kerklyv5.controlador.Notificacion
import com.example.kerklyv5.extras.IntroSliderActivity
import com.example.kerklyv5.interfaces.AceptarPresupuestoNormalInterface
import com.example.kerklyv5.interfaces.DeviceIDInterfaceBotonSinRegistro
import com.example.kerklyv5.modelo.Cliente
import com.example.kerklyv5.url.Url
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_main.*
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.BufferedReader
import java.io.InputStreamReader


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
    //video fondo
    protected lateinit var vv_fondo: VideoView
    protected lateinit var mMediaPlayer: MediaPlayer
    protected var mCurrentVideoPosition: Int = 0
    private lateinit var ivLogo: ImageView
    private lateinit var animation: Animation
    private lateinit var animation2: Animation
    private lateinit var animation3: Animation
    private lateinit var animation4: Animation
    private lateinit var boton: Button
    private lateinit var epicDialog2: Dialog
    private lateinit var seguir: Button
    private lateinit var buttonclosed: ImageButton
    private lateinit var btn_pruebaSinRegistro: MaterialButton



    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        controlador = MainActivityControlador()

        id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

      // controlador.verificarSesion(id, this)


        //fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
            vv_fondo = findViewById<VideoView>(R.id.vv_fondo)


//fondo de video
        val uri = Uri.parse("android.resource://"
                +packageName
                +"/"
                +R.raw.fondokerklyy)
        vv_fondo.setVideoURI(uri)
        vv_fondo.start()
        vv_fondo.setOnPreparedListener { mp ->
            mMediaPlayer = mp
            mMediaPlayer.isLooping = true
            if(mCurrentVideoPosition != 0){
                mMediaPlayer.seekTo(mCurrentVideoPosition)
                mMediaPlayer.start()
                mMediaPlayer.setVolume(0F, 0F);
            }
        }



        editUsuario = findViewById(R.id.input_user)


        boton = findViewById(R.id.button)
        btn_pruebaSinRegistro = findViewById(R.id.btn_pruebaSinRegistro)

        deviceIDBoton()

        ivLogo = findViewById(R.id.logokerkly)
        editContra = findViewById(R.id.input_password)
        layoutUsuario = findViewById(R.id.textnputUser)
        layoutContra = findViewById(R.id.textnputPassword)
        dialog = Dialog(this)


        //animaciones
        animation = AnimationUtils.loadAnimation(this, R.anim.advanced_inicio)
        ivLogo.startAnimation(animation)

        animation2 = AnimationUtils.loadAnimation(this, R.anim.from_right_0)
        layoutUsuario.startAnimation(animation2)

        animation3 = AnimationUtils.loadAnimation(this, R.anim.from_left_advanced)
        layoutContra.startAnimation(animation3)

        animation4 = AnimationUtils.loadAnimation(this, R.anim.from_right_0)
        boton.startAnimation(animation4)


//si es la primera vez que se inicia la app, se envia al IntroSlider
     //si no, se mantiene en la ventana actual
        if(isFirstTime()){
            startActivity(Intent(this@MainActivity, IntroSliderActivity::class.java))
            finish();
        }else{
        }

        //  val constraints = Constraints.Builder().setRequiresCharging(true).build()
        //val uploadWork: WorkRequest = OneTimeWorkRequestBuilder<PresupuestoWorker>().setConstraints(constraints).build()
        //WorkManager.getInstance(this).enqueue(uploadWork)
        //startService(Intent(this,PresupuestoService::class.java))
        //startService(Intent(this, PresupuestoServicio::class.java))
    }

    fun deviceIDBoton() {
        var enteroBandera = 3
        val ROOT_URL = Url().url
        val adaptar = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()

        val api = adaptar.create(DeviceIDInterfaceBotonSinRegistro ::class.java)

        api.mensaje(id,
            object : Callback<Response?> {
                override fun success(t: Response?, response: Response?) {

                    var entrada: BufferedReader? =  null
                    var Respuesta = ""
                    try {
                        entrada = BufferedReader(InputStreamReader(t?.body?.`in`()))
                        Respuesta = entrada.readLine().trim()
                        System.out.println("respuesta de id: $Respuesta")
                        Log.d("id", id)
                        //Toast.makeText(applicationContext, id, Toast.LENGTH_SHORT).show()

                        enteroBandera = Respuesta.toInt()
                        if ( enteroBandera == 1) {
                            btn_pruebaSinRegistro.visibility = View.GONE
                        } else {
                            btn_pruebaSinRegistro.visibility = View.VISIBLE

                        }

                       // Toast.makeText(applicationContext, enteroBandera.toString(), Toast.LENGTH_SHORT).show()

                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }

                override fun failure(error: RetrofitError?) {
                    print(error.toString())
                }

            }
        )
        Log.d("respuesta", enteroBandera.toString())

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
                 //b.putString("Telefono", usuario)
                 //val i = Intent(this, SolicitarServicio::class.java)
                //i.putExtras(b)


                val u = Cliente(editUsuario.text.toString(), editContra.text.toString())
                controlador.verficiarUsuario(u, this)


            }
        }
    }


    inner class RetreiveFeedTask: AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String? {
            //controlador.getNombreNoR(usuario)
            controlador.pruebaRegistrarNumero(usuario,
                this@MainActivity, layoutTelefono)
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
        //var notificacion = Notificacion(this)
        dialog.setContentView(R.layout.telefono_no_registrado_confirmar)
        dialog.show()
    }


    //modificar este metodo par que solo ingrese el numero
    fun aceptarNoRegistrado (view: View) {

        editTelefono = epicDialog2.findViewById(R.id.edit_telefonoNoRegistrado)
        layoutTelefono = epicDialog2.findViewById(R.id.layoutTelefonoNoRegistrado)

            usuario = Cliente(editTelefono.text.toString())

            if (usuario.getTelefonoNoR().isEmpty()) {
                 layoutTelefono.error = getString(R.string.campo_requerido)
            } else {
                layoutTelefono.error = null
                if (usuario.getTelefonoNoR().length != 10) {
                    layoutTelefono.error = getText(R.string.telefono_error)
                } else {
                    layoutTelefono.error = null
                    if (!(usuario.getTelefonoNoR().isEmpty())) {
                        barra = ProgressDialog.show(this, "", "Ingresando...")
                       // Toast.makeText(this, "entro", Toast.LENGTH_SHORT).show()
                        val task = RetreiveFeedTask()
                        task.execute()
                    }
                }

            }
    }


    fun mostrarSalir(view: View) {
        //var notificacion = Notificacion(this)
        epicDialog2 = Dialog(this)
        epicDialog2.setContentView(R.layout.about2)
        buttonclosed = epicDialog2.findViewById<View>(R.id.buttonclosed) as ImageButton
        buttonclosed.setOnClickListener(View.OnClickListener { epicDialog2.dismiss() })
        epicDialog2.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        epicDialog2.show()
        requestPermission(this)
    }
    private val sms = 0

    private fun requestPermission(contexto: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(contexto,
                Manifest.permission.RECEIVE_SMS)) {
            //El usuario ya ha rechazado el permiso anteriormente, debemos informarle que vaya a ajustes.

            AlertDialog.Builder(contexto)
                .setTitle("Alerta")
                .setMessage("Ve a configuracion y verifica los permisos de la aplicacion")
                .setPositiveButton(android.R.string.ok,
                    DialogInterface.OnClickListener { dialog, which ->
                        //botón OK pulsado
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    private fun isFirstTime(): Boolean {
        val preferences = getPreferences(MODE_PRIVATE)
        val ranBefore = preferences.getBoolean("RanBefore", false)
        if (!ranBefore) {
            // first time
            val editor = preferences.edit()
            editor.putBoolean("RanBefore", true)
            editor.commit()
        }
        return !ranBefore
    }


    override fun onPause() {
        super.onPause()
        mCurrentVideoPosition = mMediaPlayer.currentPosition
        vv_fondo.pause()
    }



    override fun onResume() {
        super.onResume()
        vv_fondo.start()

    }


    
}