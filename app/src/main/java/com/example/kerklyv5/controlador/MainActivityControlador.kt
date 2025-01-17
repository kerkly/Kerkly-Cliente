package com.example.kerklyv5.controlador


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.parser.IntegerParser
import com.example.kerklyv5.R
import com.example.kerklyv5.SolicitarServicio
import com.example.kerklyv5.express.PedirServicioExpress
import com.example.kerklyv5.modelo.Cliente
import com.example.kerklyv5.interfaces.*
import com.example.kerklyv5.modelo.serial.ModeloIntentos
import com.example.kerklyv5.url.Url
import com.example.kerklyv5.vista.MainActivity
import com.example.kerklyv5.vista.MainActivityVerificarSMS
import com.example.kerklyv5.vista.Registro
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.OkClient
import retrofit.client.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.concurrent.TimeUnit


class MainActivityControlador {
    private var b = Bundle()
    private lateinit var nombre: String
    private lateinit var apellidoP: String
    private lateinit var apellidoM: String
    private var intentos = 1
    private lateinit var tel: String


    fun verficiarUsuario(usuario: Cliente, contexto: AppCompatActivity) {
        val Url = Url().url
//ProgressDialogFragment.showProgressBar(contexto)
        val adapter = RestAdapter.Builder()
            .setEndpoint(Url)
            .build()
        val api: LoginInterface = adapter.create(LoginInterface::class.java)
        api.VerficarUsuario(usuario.getUsuario(),
            usuario.getContra(),
            object : Callback<Response?> {
                @SuppressLint("SuspiciousIndentation")
                override fun success(t: Response?, response: Response?) {
                    var entrada: BufferedReader? =  null

                  //  ProgressDialogFragment.hideProgressBar(contexto)

                    var Respuesta = ""
                    try {
                        entrada = BufferedReader(InputStreamReader(t?.body?.`in`()))
                        Respuesta = entrada.readLine()
                        Toast.makeText(contexto,Respuesta,Toast.LENGTH_SHORT).show()


                    }catch (e: Exception){
                        e.printStackTrace()
                    }

                  var Res = "Bienvenido"
                    if (Res.equals(Respuesta)){
                        //Toast.makeText(contexto, "$Respuesta", Toast.LENGTH_SHORT).show()
                        val  intent = Intent(contexto, SolicitarServicio::class.java)
                        intent.putExtra("Telefono", usuario.getUsuario())
                        intent.putExtra("PresupuestoListo", false)
                        contexto.startActivity(intent)
                        contexto.finish()
                    }else{
                        Toast.makeText(contexto, "$Respuesta", Toast.LENGTH_SHORT).show()
                    }


                }

                override fun failure(error: RetrofitError) {
                    showMensaje(error.toString(), contexto)
                //    Toast.makeText(contexto, "error $error" , Toast.LENGTH_SHORT).show()
                }

            }
        )
    }


    fun pruebaRegistrarNumero(usuario: Cliente, contexto: Activity, layoutTelefono: TextInputLayout,progress: Dialog) {
        //verificamos primero si el usuario ya se encuentra registrado
      //  Toast.makeText(, " entroo ", Toast.LENGTH_SHORT).show()
        val ROOT_URL = Url().url
        val adapter = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api: EntrarSinRegistroInterface = adapter.create(EntrarSinRegistroInterface::class.java)
        api.sinRegistro(usuario.getTelefonoNoR(),
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
                  // showMensaje(" $entrada", contexto)
                    var cadena: String = "El numero ya existe"
                    if (cadena == entrada) {
                        layoutTelefono.error = contexto.getText(R.string.numeroRegistrado_error)
                        return
                    } else {

                        //si entrada es igual al numero ingresado, significa que el numero ya esta registrado en las tablas de cliente no registrado
                        if (entrada == usuario.getTelefonoNoR()){
                            //verificar intentos
                            val ROOT_URL = Url().url
                            val gson = GsonBuilder().setLenient().create()
                            val interceptor = HttpLoggingInterceptor()
                            interceptor.level = HttpLoggingInterceptor.Level.BODY
                            val client: OkHttpClient = OkHttpClient.Builder().build()

                            val retrofit = Retrofit.Builder()
                                .baseUrl("$ROOT_URL/")
                                .client(client)
                                .addConverterFactory(GsonConverterFactory.create(gson))
                                .build()
                            val get = retrofit.create(EntrarSinRegistroInterface::class.java)
                            val call = get.verificarClienteNoR(usuario.getTelefonoNoR())
                            call?.enqueue(object : retrofit2.Callback<List<ModeloIntentos?>?> {

                                override fun onResponse(call: Call<List<ModeloIntentos?>?>, response: retrofit2.Response<List<ModeloIntentos?>?>) {
                                    val postList: ArrayList<ModeloIntentos> = response.body() as ArrayList<ModeloIntentos>
                                      //  val nom =  postList.get(0).nombre_noR
                                        val intentos1 = postList.get(0).numIntentos
                                        //  Toast.makeText(contexto, "Bienvenido $nom  intentos = $intentos1", Toast.LENGTH_LONG).show()

                                        if(intentos1 == 3){
                                            Toast.makeText(contexto, "${R.string.SeAcabaronLasPruebas}", Toast.LENGTH_LONG).show()
                                            layoutTelefono.error = null
                                            val intent  = Intent(contexto, PedirServicioExpress::class.java)
                                            b.putString("Teléfono No Registrado", usuario.getTelefonoNoR())
                                            b.putString("numIntentos", intentos1.toString())
                                            intent.putExtras(b)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            contexto.startActivity(intent)
                                            progress.dismiss()
                                        }else{
                                            layoutTelefono.error = null
                                            val i = Intent(contexto, PedirServicioExpress::class.java)
                                            b.putString("Teléfono No Registrado", usuario.getTelefonoNoR())
                                            b.putString("numIntentos", intentos1.toString())
                                            i.putExtras(b)
                                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            contexto.startActivity(i)
                                            progress.dismiss()
                                        }
                                }
                                override fun onFailure(call: Call<List<ModeloIntentos?>?>, t: Throwable) {
                                    //   Toast.makeText(contexto, " ${t.toString()}", Toast.LENGTH_SHORT).show()
                                    Log.d("error del retrofit", t.toString())
                                    progress.dismiss()
                                }
                            })
                        }else{
                            //usuario Nuevo Agreado
                            //verificarNumero
                            val i = Intent(contexto, MainActivityVerificarSMS::class.java)
                            b.putString("clave", "sinRegistro")
                            b.putString("Teléfono No Registrado", usuario.getTelefonoNoR())
                            i.putExtras(b)
                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            contexto.startActivity(i)
                            progress.dismiss()

                        }
                    }
                }

                override fun failure(error: RetrofitError?) {
                    println("error $error")
                    showMensaje(error.toString(),contexto)

                }

            })
    }

    private fun showMensaje(mensaje:String,context: Context){
        Toast.makeText(context,mensaje,Toast.LENGTH_SHORT).show()
    }

    fun verificarSesion(id: String, contexto: AppCompatActivity) {
        val ROOT_URL = Url().url
        val adapter = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api = adapter.create(VerificarSesionInterface::class.java)
        api.sesionAbierta(id,
            object : retrofit.Callback<retrofit.client.Response?> {
                override fun success(
                    t: retrofit.client.Response?,
                    response2: retrofit.client.Response?
                ) {
                    println("id $id")
                    var reader: BufferedReader? = null
                    var output = ""
                    try {
                        reader = BufferedReader(InputStreamReader(t?.body?.`in`()))

                        output = reader.readLine()
                        //  System.out.println("id $id pantalla inicio $output")

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                        if (output.equals("0")) {
                            //System.out.println("entro en linea 233")
                            val intent = Intent(contexto, MainActivity::class.java)
                           // intent.putExtra("Telefono", output)
                            contexto.startActivity(intent)
                            contexto.finish()
                        } else {
                            //System.out.println("entro en linea 256 $output")
                            val intent = Intent(contexto, SolicitarServicio::class.java)
                            intent.putExtra("Telefono", output)
                            intent.putExtra("PresupuestoListo", false)
                            contexto.startActivity(intent)
                            contexto.finish()
                        }

                }

                override fun failure(error: RetrofitError?) {
                    println("error244 $id $error")
                    Toast.makeText(contexto, "Problemas de conexion... por favor intente mas tarde", Toast.LENGTH_SHORT).show()
                   // contexto.finish()
                }
            }
        )
    }

}